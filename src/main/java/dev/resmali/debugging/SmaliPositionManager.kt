/*
 * Copyright 2014, Google Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *     * Neither the name of Google Inc. nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package dev.resmali.debugging

import com.intellij.debugger.NoDataException
import com.intellij.debugger.PositionManager
import com.intellij.debugger.SourcePosition
import com.intellij.debugger.engine.DebugProcess
import com.intellij.debugger.requests.ClassPrepareRequestor
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Computable
import com.intellij.openapi.util.ThrowableComputable
import com.intellij.psi.search.GlobalSearchScope
import com.sun.jdi.Location
import com.sun.jdi.ReferenceType
import com.sun.jdi.request.ClassPrepareRequest
import dev.resmali.psi.impl.SmaliFile
import dev.resmali.psi.index.findSmaliClasses

class SmaliPositionManager(private val debugProcess: DebugProcess) : PositionManager {
    @Throws(NoDataException::class)
    fun getSourcePosition(
        declaringType: String, methodName: String?, methodSignature: String?,
        codeIndex: Int,
    ): SourcePosition? {
        val classes = ApplicationManager.getApplication().runReadAction(
            Computable {
                findSmaliClasses(
                    declaringType,
                    debugProcess.project,
                    GlobalSearchScope.projectScope(debugProcess.project),
                )
            },
        )

        if (classes.isNotEmpty()) {
            val smaliClass = classes.iterator().next()

            // TODO: make an index for this?
            for (smaliMethod in smaliClass.methods) {
                if (smaliMethod.name == methodName && smaliMethod.methodPrototype.text == methodSignature) {
                    return smaliMethod.getSourcePositionForCodeOffset(codeIndex * 2)
                }
            }
        }

        throw NoDataException.INSTANCE
    }

    @Throws(NoDataException::class)
    override fun getSourcePosition(location: Location?): SourcePosition? {
        if (location == null) {
            throw NoDataException.INSTANCE
        }

        return getSourcePosition(
            location.declaringType().name(), location.method().name(),
            location.method().signature(), location.codeIndex().toInt(),
        )
    }

    @Throws(NoDataException::class)
    override fun getAllClasses(classPosition: SourcePosition): MutableList<ReferenceType> {
        return ApplicationManager.getApplication().runReadAction(
            ThrowableComputable<MutableList<ReferenceType>, NoDataException> {
                debugProcess.virtualMachineProxy.classesByName(getClassFromPosition(classPosition))
            },
        )
    }

    @Throws(NoDataException::class)
    private fun getClassFromPosition(position: SourcePosition): String {
        val smaliFile = getSmaliFile(position) ?: throw NoDataException.INSTANCE
        return smaliFile.psiClass?.qualifiedName ?: throw NoDataException.INSTANCE
    }

    @Throws(NoDataException::class)
    override fun locationsOfLine(
        type: ReferenceType,
        position: SourcePosition,
    ): MutableList<Location> {
        if (ApplicationManager.getApplication().runReadAction<SmaliFile?> { getSmaliFile(position) } == null) {
            throw NoDataException.INSTANCE
        }

        val locations = ArrayList<Location>(1)

        ApplicationManager.getApplication().runReadAction {
            val typeName = type.name()
            val classes = findSmaliClasses(
                typeName, debugProcess.project,
                GlobalSearchScope.projectScope(debugProcess.project),
            )

            if (classes.isNotEmpty()) {
                val smaliClass = classes.iterator().next()

                val location = smaliClass.getLocationForSourcePosition(type, position)

                if (location != null) {
                    locations.add(location)
                }
            }
        }
        return locations
    }

    @Throws(NoDataException::class)
    override fun createPrepareRequest(
        requestor: ClassPrepareRequestor,
        position: SourcePosition,
    ): ClassPrepareRequest? {
        val className = ApplicationManager.getApplication().runReadAction(
            ThrowableComputable<String, NoDataException> { getClassFromPosition(position) },
        )
        return debugProcess.requestsManager.createClassPrepareRequest(
            { debuggerProcess, referenceType ->
                requestor.processClassPrepare(debuggerProcess, referenceType)
            },
            className,
        )
    }

    private fun getSmaliFile(position: SourcePosition): SmaliFile? {
        val sourceFile = position.file
        if (sourceFile is SmaliFile) {
            return sourceFile
        }

        val element = position.elementAt ?: return null

        val containingFile = element.containingFile
        if (containingFile is SmaliFile) {
            return containingFile
        }
        return null
    }
}
