package dev.resmali.highlighter;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SmaliHighlightingColorsTest {
    @Test
    public void textAttributeKeysAreNamespacedByPluginId() {
        for (TextAttributesKey key : SmaliHighlightingColors.getAllKeys()) {
            assertTrue(
                    "Text attribute key is not namespaced: " + key.getExternalName(),
                    key.getExternalName().startsWith("dev.resmali."));
        }
    }
}
