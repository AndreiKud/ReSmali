package dev.resmali.debugging.utils

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Computable
import com.sun.jdi.VirtualMachine
import dev.resmali.psi.impl.SmaliMethod

object RegisterSlotUtils {
    fun mapForVirtualMachine(vm: VirtualMachine, smaliMethod: SmaliMethod, register: Int): Int {
        return when (vm.version()) {
            "1.5.0" -> mapRegisterForDalvik(
                smaliMethod,
                register,
            )
            // Newer ART versions use an OpenJDK JVMTI implementation that does not need register remapping.
            "0", "8" -> register
            else -> mapRegisterForArt(smaliMethod, register)
        }
    }

    private fun mapRegisterForArt(smaliMethod: SmaliMethod, register: Int): Int {
        return ApplicationManager.getApplication().runReadAction(
            Computable {
                val totalRegisters = smaliMethod.registerCount
                val parameterRegisters = smaliMethod.parameterRegisterCount

                // For ART, the parameter registers are rotated to the front
                if (register >= (totalRegisters - parameterRegisters)) {
                    return@Computable register - (totalRegisters - parameterRegisters)
                }
                register + parameterRegisters
            },
        )
    }

    private fun mapRegisterForDalvik(smaliMethod: SmaliMethod, register: Int): Int {
        return ApplicationManager.getApplication().runReadAction(
            Computable {
                if (smaliMethod.modifierList.hasModifierProperty("static")) {
                    return@Computable register
                }
                val totalRegisters = smaliMethod.registerCount
                val parameterRegisters = smaliMethod.parameterRegisterCount

                // For dalvik, p0 is mapped to register 1, and register 0 is mapped to register 1000
                if (register == (totalRegisters - parameterRegisters)) {
                    return@Computable 0
                }
                if (register == 0) {
                    return@Computable 1000
                }
                register
            },
        )
    }
}
