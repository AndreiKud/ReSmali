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
import dev.resmali.psi.impl.SmaliAnnotation
import dev.resmali.psi.stub.SmaliAnnotationStub
import java.io.IOException

class SmaliAnnotationElementType private constructor() : SmaliStubElementType<SmaliAnnotationStub, SmaliAnnotation>("ANNOTATION") {
    override fun createPsi(stub: SmaliAnnotationStub): SmaliAnnotation {
        return SmaliAnnotation(stub)
    }

    override fun createPsi(node: ASTNode): SmaliAnnotation {
        return SmaliAnnotation(node)
    }

    override fun createStub(psi: SmaliAnnotation, parentStub: StubElement<*>?): SmaliAnnotationStub {
        return SmaliAnnotationStub(parentStub, psi.smaliName)
    }

    @Throws(IOException::class)
    override fun serialize(stub: SmaliAnnotationStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.annotationSmaliTypeName)
    }

    @Throws(IOException::class)
    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): SmaliAnnotationStub {
        return SmaliAnnotationStub(parentStub, deserializeNullableString(dataStream))
    }

    override fun indexStub(stub: SmaliAnnotationStub, sink: IndexSink) {
    }

    companion object {
        val INSTANCE: SmaliAnnotationElementType = SmaliAnnotationElementType()
    }
}
