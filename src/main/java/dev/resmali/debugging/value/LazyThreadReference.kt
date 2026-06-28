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
import com.sun.jdi.IncompatibleThreadStateException
import com.sun.jdi.InvalidTypeException
import com.sun.jdi.MonitorInfo
import com.sun.jdi.ObjectReference
import com.sun.jdi.StackFrame
import com.sun.jdi.ThreadGroupReference
import com.sun.jdi.ThreadReference
import com.sun.jdi.Value
import dev.resmali.psi.impl.SmaliMethod

class LazyThreadReference(method: SmaliMethod, project: Project, registerNumber: Int, registerName: String, type: String) :
    LazyObjectReference<ThreadReference>(method, project, registerNumber, registerName, type), ThreadReference {
    @Throws(IncompatibleThreadStateException::class)
    override fun currentContendedMonitor(): ObjectReference? {
        return getValue().currentContendedMonitor()
    }

    @Throws(InvalidTypeException::class, ClassNotLoadedException::class, IncompatibleThreadStateException::class)
    override fun forceEarlyReturn(value: Value?) {
        getValue().forceEarlyReturn(value)
    }

    @Throws(IncompatibleThreadStateException::class)
    override fun frame(index: Int): StackFrame? {
        return getValue().frame(index)
    }

    @Throws(IncompatibleThreadStateException::class)
    override fun frameCount(): Int {
        return getValue().frameCount()
    }

    @Throws(IncompatibleThreadStateException::class)
    override fun frames(): MutableList<StackFrame>? {
        return getValue().frames()
    }

    @Throws(IncompatibleThreadStateException::class)
    override fun frames(start: Int, length: Int): MutableList<StackFrame>? {
        return getValue().frames(start, length)
    }

    override fun interrupt() {
        getValue().interrupt()
    }

    override fun isAtBreakpoint(): Boolean {
        return getValue().isAtBreakpoint
    }

    override fun isSuspended(): Boolean {
        return getValue().isSuspended
    }

    override fun name(): String? {
        return getValue().name()
    }

    @Throws(IncompatibleThreadStateException::class)
    override fun ownedMonitors(): MutableList<ObjectReference>? {
        return getValue().ownedMonitors()
    }

    @Throws(IncompatibleThreadStateException::class)
    override fun ownedMonitorsAndFrames(): MutableList<MonitorInfo>? {
        return getValue().ownedMonitorsAndFrames()
    }

    @Throws(IncompatibleThreadStateException::class)
    override fun popFrames(frame: StackFrame?) {
        getValue().popFrames(frame)
    }

    override fun resume() {
        getValue().resume()
    }

    override fun status(): Int {
        return getValue().status()
    }

    @Throws(InvalidTypeException::class)
    override fun stop(throwable: ObjectReference?) {
        getValue().stop(throwable)
    }

    override fun suspend() {
        getValue().suspend()
    }

    override fun suspendCount(): Int {
        return getValue().suspendCount()
    }

    override fun threadGroup(): ThreadGroupReference? {
        return getValue().threadGroup()
    }
}
