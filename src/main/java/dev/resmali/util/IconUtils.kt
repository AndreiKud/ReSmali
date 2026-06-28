package dev.resmali.util

import com.android.tools.smali.dexlib2.AccessFlags
import com.intellij.ui.RowIcon
import com.intellij.util.PlatformIcons
import dev.resmali.psi.iface.SmaliModifierListOwner
import javax.swing.Icon

object IconUtils {
    fun getElementIcon(modifierListOwner: SmaliModifierListOwner, leftIcon: Icon?): Icon {
        val accessFlags = modifierListOwner.modifierList?.accessFlags ?: 0
        val rightIcon = getAccessibilityIcon(accessFlags)
        return RowIcon(leftIcon, rightIcon)
    }

    private fun getAccessibilityIcon(accessFlags: Int): Icon {
        return when {
            AccessFlags.PUBLIC.isSet(accessFlags) -> PlatformIcons.PUBLIC_ICON
            AccessFlags.PRIVATE.isSet(accessFlags) -> PlatformIcons.PRIVATE_ICON
            AccessFlags.PROTECTED.isSet(accessFlags) -> PlatformIcons.PROTECTED_ICON
            else -> PlatformIcons.PACKAGE_LOCAL_ICON
        }
    }
}
