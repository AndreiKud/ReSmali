/*
 * Copyright 2015, Google Inc.
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
package dev.resmali

import com.intellij.openapi.roots.JavaProjectRootsUtil
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.refactoring.MultiFileTestCase
import com.intellij.refactoring.PackageWrapper
import com.intellij.refactoring.move.moveClassesOrPackages.AutocreatingSingleSourceRootMoveDestination
import com.intellij.refactoring.move.moveClassesOrPackages.MoveClassesOrPackagesProcessor

class ClassMoveTest : MultiFileTestCase() {
    override fun getTestDataPath(): String {
        return "testData"
    }

    override fun getTestRoot(): String {
        return "/classMove/"
    }

    fun testBasicFromNoPackage() {
        doTest("blah", "my")
    }

    fun testBasicToNoPackage() {
        doTest("my.blah", "")
    }

    private fun doTest(oldQualifiedName: String, newPackage: String) {
        doTest { _, _ -> doMove(oldQualifiedName, newPackage) }
    }

    @Throws(Exception::class)
    private fun doMove(oldQualifiedName: String, newPackage: String) {
        val testClass = myJavaFacade.findClass(oldQualifiedName, GlobalSearchScope.allScope(project))

        val contentSourceRoots = JavaProjectRootsUtil.getSuitableDestinationSourceRoots(project)

        MoveClassesOrPackagesProcessor(
            project, arrayOf(testClass),
            AutocreatingSingleSourceRootMoveDestination(
                PackageWrapper(psiManager, newPackage),
                contentSourceRoots.first(),
            ),
            false, false, null,
        ).run()
    }
}
