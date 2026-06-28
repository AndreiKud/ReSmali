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
import com.sun.jdi.ArrayReference
import com.sun.jdi.ClassNotLoadedException
import com.sun.jdi.InvalidTypeException
import com.sun.jdi.Value
import dev.resmali.psi.impl.SmaliMethod

class LazyArrayReference(method: SmaliMethod, project: Project, registerNumber: Int, registerName: String, type: String) :
    LazyObjectReference<ArrayReference>(method, project, registerNumber, registerName, type), ArrayReference {
    override fun getValue(index: Int): Value? {
        return getValue().getValue(index)
    }

    override fun getValues(): MutableList<Value>? {
        return getValue().values
    }

    override fun getValues(index: Int, length: Int): MutableList<Value>? {
        return getValue().getValues(index, length)
    }

    override fun length(): Int {
        return getValue().length()
    }

    @Throws(InvalidTypeException::class, ClassNotLoadedException::class)
    override fun setValue(index: Int, value: Value?) {
        getValue().setValue(index, value)
    }

    @Throws(InvalidTypeException::class, ClassNotLoadedException::class)
    override fun setValues(index: Int, values: MutableList<out Value?>?, srcIndex: Int, length: Int) {
        getValue().setValues(index, values, srcIndex, length)
    }

    @Throws(InvalidTypeException::class, ClassNotLoadedException::class)
    override fun setValues(values: MutableList<out Value?>?) {
        getValue().setValues(values)
    }
}
