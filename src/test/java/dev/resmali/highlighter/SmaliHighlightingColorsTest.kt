package dev.resmali.highlighter

import org.junit.Assert
import org.junit.Test

class SmaliHighlightingColorsTest {
    @Test
    fun textAttributeKeysAreNamespacedByPluginId() {
        for (key in SmaliHighlightingColors.allKeys) {
            Assert.assertTrue(
                "Text attribute key is not namespaced: ${key.externalName}",
                key.externalName.startsWith("dev.resmali."),
            )
        }
    }
}
