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
package dev.resmali.psi.stub.element

import com.intellij.lang.ASTNode
import com.intellij.psi.impl.java.stubs.index.JavaStubIndexKeys
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import dev.resmali.psi.SmaliStubElementTypes
import dev.resmali.psi.impl.SmaliClass
import dev.resmali.psi.index.smaliClassNameKey
import dev.resmali.psi.stub.SmaliClassStatementStub
import dev.resmali.psi.stub.SmaliClassStub
import java.io.IOException

class SmaliClassElementType private constructor() : SmaliStubElementType<SmaliClassStub, SmaliClass>("CLASS") {
    override fun createPsi(stub: SmaliClassStub): SmaliClass {
        return SmaliClass(stub)
    }

    override fun createPsi(node: ASTNode): SmaliClass {
        return SmaliClass(node)
    }

    override fun createStub(psi: SmaliClass, parentStub: StubElement<*>?): SmaliClassStub {
        return SmaliClassStub(parentStub)
    }

    @Throws(IOException::class)
    override fun serialize(stub: SmaliClassStub, dataStream: StubOutputStream) {
    }

    @Throws(IOException::class)
    override fun deserialize(ignoredDataStream: StubInputStream, parentStub: StubElement<*>?): SmaliClassStub {
        return SmaliClassStub(parentStub)
    }

    override fun indexStub(stub: SmaliClassStub, sink: IndexSink) {
        val smaliClassStatementStub = stub.findChildStubByElementType(SmaliStubElementTypes.CLASS_STATEMENT) as? SmaliClassStatementStub
        if (smaliClassStatementStub != null) {
            val qualifiedName = smaliClassStatementStub.qualifiedName
            if (qualifiedName != null) {
                sink.occurrence(smaliClassNameKey, qualifiedName)
            }

            val shortName = smaliClassStatementStub.name
            if (shortName != null) {
                sink.occurrence(JavaStubIndexKeys.CLASS_SHORT_NAMES, shortName)
            }
        }
    }

    companion object {
        val INSTANCE: SmaliClassElementType = SmaliClassElementType()
    }
}
