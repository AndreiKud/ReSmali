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
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import dev.resmali.psi.impl.SmaliMethodPrototype
import dev.resmali.psi.stub.SmaliMethodPrototypeStub
import java.io.IOException

class SmaliMethodPrototypeElementType
private constructor() : SmaliStubElementType<SmaliMethodPrototypeStub, SmaliMethodPrototype>("METHOD_PROTOTYPE") {
    override fun createPsi(node: ASTNode): SmaliMethodPrototype {
        return SmaliMethodPrototype(node)
    }

    override fun createPsi(stub: SmaliMethodPrototypeStub): SmaliMethodPrototype {
        return SmaliMethodPrototype(stub)
    }

    override fun createStub(psi: SmaliMethodPrototype, parentStub: StubElement<*>): SmaliMethodPrototypeStub {
        val returnType = psi.returnTypeElement
        var returnSmaliTypeName: String? = null
        if (returnType != null) {
            returnSmaliTypeName = returnType.smaliName
        }

        return SmaliMethodPrototypeStub(parentStub, returnSmaliTypeName)
    }

    @Throws(IOException::class)
    override fun serialize(stub: SmaliMethodPrototypeStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.returnSmaliTypeName)
    }

    @Throws(IOException::class)
    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): SmaliMethodPrototypeStub {
        return SmaliMethodPrototypeStub(parentStub, deserializeNullableString(dataStream))
    }

    override fun indexStub(stub: SmaliMethodPrototypeStub, sink: IndexSink) {
    }

    companion object {
        val INSTANCE: SmaliMethodPrototypeElementType = SmaliMethodPrototypeElementType()
    }
}
