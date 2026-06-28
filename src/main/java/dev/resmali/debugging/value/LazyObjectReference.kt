/*
 * Copyright 2016, Google Inc.
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
package dev.resmali.debugging.value

import com.intellij.openapi.project.Project
import com.sun.jdi.ClassNotLoadedException
import com.sun.jdi.Field
import com.sun.jdi.IncompatibleThreadStateException
import com.sun.jdi.InvalidTypeException
import com.sun.jdi.InvocationException
import com.sun.jdi.Method
import com.sun.jdi.ObjectReference
import com.sun.jdi.ReferenceType
import com.sun.jdi.ThreadReference
import com.sun.jdi.Value
import dev.resmali.psi.impl.SmaliMethod

open class LazyObjectReference<T : ObjectReference>(
    method: SmaliMethod,
    project: Project,
    registerNumber: Int,
    registerName: String,
    type: String,
) : LazyValue<T>(method, project, registerNumber, registerName, type), ObjectReference {
    override fun disableCollection() {
        getValue().disableCollection()
    }

    override fun referenceType(): ReferenceType? {
        return getValue().referenceType()
    }

    override fun getValue(sig: Field?): Value? {
        return getValue().getValue(sig)
    }

    override fun getValues(fields: MutableList<out Field?>?): MutableMap<Field?, Value?>? {
        return getValue().getValues(fields)
    }

    @Throws(InvalidTypeException::class, ClassNotLoadedException::class)
    override fun setValue(field: Field?, value: Value?) {
        getValue().setValue(field, value)
    }

    @Throws(
        InvalidTypeException::class, ClassNotLoadedException::class, IncompatibleThreadStateException::class, InvocationException::class,
    )
    override fun invokeMethod(thread: ThreadReference?, method: Method?, arguments: MutableList<out Value?>?, options: Int): Value? {
        return getValue().invokeMethod(thread, method, arguments, options)
    }

    override fun enableCollection() {
        getValue().enableCollection()
    }

    override fun isCollected(): Boolean {
        return getValue().isCollected
    }

    override fun uniqueID(): Long {
        return getValue().uniqueID()
    }

    @Throws(IncompatibleThreadStateException::class)
    override fun waitingThreads(): MutableList<ThreadReference>? {
        return getValue().waitingThreads()
    }

    @Throws(IncompatibleThreadStateException::class)
    override fun owningThread(): ThreadReference? {
        return getValue().owningThread()
    }

    @Throws(IncompatibleThreadStateException::class)
    override fun entryCount(): Int {
        return getValue().entryCount()
    }

    override fun referringObjects(maxReferrers: Long): MutableList<ObjectReference>? {
        return getValue().referringObjects(maxReferrers)
    }
}
