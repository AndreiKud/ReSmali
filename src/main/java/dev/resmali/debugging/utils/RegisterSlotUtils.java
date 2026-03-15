package dev.resmali.debugging.utils;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.sun.jdi.VirtualMachine;
import dev.resmali.psi.impl.SmaliMethod;

public class RegisterSlotUtils {

    public static int mapForVirtualMachine(final VirtualMachine vm, final SmaliMethod smaliMethod, final int register) {
        if (vm.version().equals("1.5.0")) {
            return mapRegisterForDalvik(smaliMethod, register);
        } else if (vm.version().equals("0") || vm.version().equals("8")) {
            // Newer versions of art (P+? I think) use an openjdk jvmti implementation, that doesn't need any register
            // remapping
            return register;
        } else {
            // For older versions of art
            return mapRegisterForArt(smaliMethod, register);
        }
    }

    private static int mapRegisterForArt(final SmaliMethod smaliMethod, final int register) {
        return ApplicationManager.getApplication().runReadAction((Computable<Integer>) () -> {
            int totalRegisters = smaliMethod.getRegisterCount();
            int parameterRegisters = smaliMethod.getParameterRegisterCount();

            // For ART, the parameter registers are rotated to the front
            if (register >= (totalRegisters - parameterRegisters)) {
                return register - (totalRegisters - parameterRegisters);
            }
            return register + parameterRegisters;
        });
    }

    private static int mapRegisterForDalvik(final SmaliMethod smaliMethod, final int register) {
        return ApplicationManager.getApplication().runReadAction((Computable<Integer>) () -> {
            if (smaliMethod.getModifierList().hasModifierProperty("static")) {
                return register;
            }

            int totalRegisters = smaliMethod.getRegisterCount();
            int parameterRegisters = smaliMethod.getParameterRegisterCount();

            // For dalvik, p0 is mapped to register 1, and register 0 is mapped to register 1000
            if (register == (totalRegisters - parameterRegisters)) {
                return 0;
            }
            if (register == 0) {
                return 1000;
            }
            return register;
        });
    }
}
