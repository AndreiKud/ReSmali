/*
 * Copyright 2000-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.resmali

import com.intellij.lang.Language
import com.intellij.openapi.fileEditor.impl.LoadTextUtil
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.CharsetToolkit
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.impl.DebugUtil
import com.intellij.psi.impl.PsiFileFactoryImpl
import com.intellij.psi.impl.source.PsiFileImpl
import com.intellij.psi.stubs.SerializationManagerImpl
import com.intellij.psi.stubs.SerializerNotFoundException
import com.intellij.testFramework.LightVirtualFile
import com.intellij.testFramework.TestDataFile
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase
import junit.framework.TestCase
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets

/**
 * A test case for parsing tests.
 * 
 * This was originally based on com.intellij.testFramework.ParsingTestCase, but was modified
 * to use the LightJavaCodeInsightFixtureTestCase base class, which provides more functionality
 */
abstract class LightCodeInsightParsingTestCase(
    dataPath: String, private val myFileExt: String,
    private val myLanguage: Language,
) : LightJavaCodeInsightFixtureTestCase() {
    private val myFullDataPath: String = "$testDataPath/$dataPath"

    private fun includeRanges(): Boolean {
        return false
    }

    private fun showWhitespaces(): Boolean {
        return true
    }

    private fun checkAllPsiRoots(): Boolean {
        return true
    }

    protected fun doTest(checkResult: Boolean) {
        val name = getTestName(false)
        try {
            val text = loadFile("$name.$myFileExt")
            val f = createPsiFile(name, text)

            if (f is PsiFileImpl) {
                // Also want to test stub serialization/deserialization
                val stubTree = f.calcStubTree()

                val baos = ByteArrayOutputStream()
                SerializationManagerImpl.getInstanceEx().serialize(stubTree.root, baos)
                val bais = ByteArrayInputStream(baos.toByteArray())
                SerializationManagerImpl.getInstanceEx().deserialize(bais)
            }

            ensureParsed(f)
            TestCase.assertEquals(
                "light virtual file text mismatch", text,
                (f.virtualFile as LightVirtualFile).content.toString(),
            )
            assertEquals("virtual file text mismatch", text, LoadTextUtil.loadText(f.virtualFile))
            TestCase.assertEquals("doc text mismatch", text, f.viewProvider.document.text)
            TestCase.assertEquals("psi text mismatch", text, f.text)
            if (checkResult) {
                checkResult(name, f)
            } else {
                toParseTreeText(f, showWhitespaces(), includeRanges())
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch (e: SerializerNotFoundException) {
            throw RuntimeException(e)
        }
    }

    private fun createPsiFile(name: String?, text: String): PsiFile {
        return createFile("$name.$myFileExt", text)
    }

    private fun createFile(name: String, text: String): PsiFile {
        val virtualFile = LightVirtualFile(name, myLanguage, text)
        virtualFile.charset = StandardCharsets.UTF_8
        return createFile(virtualFile)
    }

    private fun createFile(virtualFile: LightVirtualFile): PsiFile {
        return checkNotNull(
            (PsiFileFactory.getInstance(project) as PsiFileFactoryImpl).trySetupPsiForFile(
                virtualFile, myLanguage, true, false,
            ),
        )
    }

    @Throws(IOException::class)
    protected fun checkResult(@TestDataFile targetDataName: String?, file: PsiFile) {
        doCheckResult(myFullDataPath, file, checkAllPsiRoots(), targetDataName, showWhitespaces(), includeRanges())
    }

    @Throws(IOException::class)
    protected fun loadFile(@TestDataFile name: String): String {
        return doLoadFile(myFullDataPath, name)
    }

    companion object {
        @Throws(IOException::class)
        fun doCheckResult(
            myFullDataPath: String?,
            file: PsiFile,
            checkAllPsiRoots: Boolean,
            targetDataName: String?,
            showWhitespaces: Boolean,
            printRanges: Boolean,
        ) {
            val provider = file.viewProvider
            val languages = provider.languages

            if (!checkAllPsiRoots || languages.size == 1) {
                doCheckResult(
                    myFullDataPath, "$targetDataName.txt", toParseTreeText(file, showWhitespaces, printRanges).trim { it <= ' ' },
                )
                return
            }

            for (language in languages) {
                val root = provider.getPsi(language)
                val expectedName = "$targetDataName.${language.id}.txt"
                doCheckResult(
                    myFullDataPath, expectedName, toParseTreeText(checkNotNull(root), showWhitespaces, printRanges).trim { it <= ' ' },
                )
            }
        }

        @Throws(IOException::class)
        fun doCheckResult(fullPath: String?, targetDataName: String, text: String) {
            val expectedFileName = fullPath + File.separatorChar + targetDataName
            assertSameLinesWithFile(expectedFileName, text)
        }

        protected fun toParseTreeText(file: PsiElement, showWhitespaces: Boolean, printRanges: Boolean): String {
            return DebugUtil.psiToString(file, showWhitespaces, printRanges)
        }

        @Throws(IOException::class)
        private fun doLoadFile(myFullDataPath: String?, name: String): String {
            return FileUtil.loadFile(File(myFullDataPath, name), CharsetToolkit.UTF8, true).trim { it <= ' ' }
        }

        fun ensureParsed(file: PsiFile) {
            file.accept(
                object : PsiElementVisitor() {
                    override fun visitElement(element: PsiElement) {
                        element.acceptChildren(this)
                    }
                },
            )
        }
    }
}
