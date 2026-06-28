package dev.resmali.debugging.utils

import com.android.tools.smali.dexlib2.analysis.AnalyzedInstruction
import com.android.tools.smali.dexlib2.analysis.RegisterType
import com.intellij.debugger.engine.evaluation.CodeFragmentFactoryContextWrapper
import com.intellij.debugger.engine.evaluation.CodeFragmentKind
import com.intellij.debugger.engine.evaluation.DefaultCodeFragmentFactory
import com.intellij.debugger.engine.evaluation.TextWithImportsImpl
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.openapi.util.Key
import com.intellij.psi.JavaCodeFragment
import com.intellij.psi.JavaRecursiveElementVisitor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLocalVariable
import com.intellij.psi.util.PsiMatchers
import dev.resmali.SmaliFileType
import dev.resmali.debugging.value.LazyValue
import dev.resmali.psi.impl.SmaliInstruction
import dev.resmali.psi.impl.SmaliMethod
import dev.resmali.util.NameUtils
import dev.resmali.util.PsiUtil

object RegistersContext {
    val SMALI_LAZY_VALUES_KEY: Key<MutableList<LazyValue<*>>> = Key.create("_smali_register_value_key_")

    fun wrap(project: Project, originalContext: PsiElement?): PsiElement? {
        if (originalContext == null) return null
        if (project.isDefault) {
            return originalContext
        }
        return ApplicationManager.getApplication().runReadAction(
            Computable {
                val lazyValues = mutableListOf<LazyValue<*>>()
                var currentInstruction = PsiUtil.searchBackward(
                    originalContext,
                    PsiMatchers.hasClass(SmaliInstruction::class.java),
                    PsiMatchers.hasClass(SmaliMethod::class.java),
                ) as? SmaliInstruction

                if (currentInstruction == null) {
                    currentInstruction = PsiUtil.searchForward(
                        originalContext,
                        PsiMatchers.hasClass(SmaliInstruction::class.java),
                        PsiMatchers.hasClass(SmaliMethod::class.java),
                    ) as? SmaliInstruction
                    if (currentInstruction == null) {
                        return@Computable originalContext
                    }
                }

                val containingMethod = currentInstruction.parentMethod
                val analyzedInstruction = currentInstruction.analyzedInstruction ?: return@Computable originalContext

                val totalRegisters = containingMethod.registerCount
                val paramRegisters = containingMethod.parameterRegisterCount
                val firstParamRegistersIdx = totalRegisters - paramRegisters
                val registerMap = mutableMapOf<String, String>()
                val variablesText = StringBuilder()
                populateVisibleRegisters(
                    registerMap,
                    variablesText,
                    analyzedInstruction,
                    totalRegisters,
                    firstParamRegistersIdx,
                )
                if (variablesText.isEmpty()) {
                    return@Computable originalContext
                }

                val textWithImports = TextWithImportsImpl(
                    CodeFragmentKind.CODE_BLOCK,
                    variablesText.toString(),
                    "",
                    SmaliFileType,
                )
                val codeFragment: JavaCodeFragment = checkNotNull(
                    DefaultCodeFragmentFactory.getInstance().createPsiCodeFragmentImpl(textWithImports, originalContext, project),
                )

                codeFragment.accept(
                    object : JavaRecursiveElementVisitor() {
                        override fun visitLocalVariable(variable: PsiLocalVariable) {
                            val name = variable.name
                            if (registerMap.containsKey(name)) {
                                var registerNumber = name.substring(1).toInt()
                                if (name.first() == 'p') {
                                    registerNumber += firstParamRegistersIdx
                                }
                                val lazyValue: LazyValue<*> = LazyValue.create(
                                    containingMethod, project, registerNumber, name, checkNotNull(registerMap[name]),
                                )
                                variable.putUserData(CodeFragmentFactoryContextWrapper.LABEL_VARIABLE_VALUE_KEY, lazyValue)
                                lazyValues.add(lazyValue)
                            }
                        }
                    },
                )

                val offset = variablesText.length - 1
                val newContext = codeFragment.findElementAt(offset)
                if (newContext != null) {
                    newContext.putUserData(SMALI_LAZY_VALUES_KEY, lazyValues)
                    return@Computable newContext
                }
                originalContext
            },
        )
    }

    private fun populateVisibleRegisters(
        registerMap: MutableMap<String, String>,
        variablesText: StringBuilder,
        analyzedInstruction: AnalyzedInstruction,
        registersCount: Int,
        firstParameterRegister: Int,
    ) {
        for (i in 0..<registersCount) {
            val parameterRegisterNumber = i - firstParameterRegister

            val registerType = analyzedInstruction.getPreInstructionRegisterType(i)
            val (javaType, smaliType) = when (registerType.category) {
                RegisterType.UNKNOWN, RegisterType.UNINIT, RegisterType.CONFLICTED, RegisterType.LONG_HI, RegisterType.DOUBLE_HI -> continue
                RegisterType.NULL, RegisterType.ONE, RegisterType.INTEGER -> "int" to "I"
                RegisterType.BOOLEAN -> "boolean" to "Z"
                RegisterType.BYTE, RegisterType.POS_BYTE -> "byte" to "B"
                RegisterType.SHORT, RegisterType.POS_SHORT -> "short" to "S"
                RegisterType.CHAR -> "char" to "C"
                RegisterType.FLOAT -> "float" to "F"
                RegisterType.LONG_LO -> "long" to "J"
                RegisterType.DOUBLE_LO -> "double" to "D"
                RegisterType.UNINIT_REF, RegisterType.UNINIT_THIS, RegisterType.REFERENCE -> {
                    val smaliType = checkNotNull(registerType.type).type
                    NameUtils.smaliToJavaType(smaliType) to smaliType
                }
                else -> continue
            }

            variablesText.append("$javaType v$i;\n")
            registerMap["v$i"] = smaliType
            if (parameterRegisterNumber >= 0) {
                variablesText.append("$javaType p$parameterRegisterNumber;\n")
                registerMap["p$parameterRegisterNumber"] = smaliType
            }
        }
    }
}
