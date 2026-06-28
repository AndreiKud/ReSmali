package dev.resmali.structureView

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.NavigatablePsiElement
import dev.resmali.psi.impl.SmaliClass
import dev.resmali.psi.impl.SmaliFile
import java.util.ArrayList
import java.util.Objects

class SmaliStructureViewElement(private val element: NavigatablePsiElement) : StructureViewTreeElement, SortableTreeElement {
    override fun getValue(): Any {
        return element
    }

    override fun navigate(requestFocus: Boolean) {
        element.navigate(requestFocus)
    }

    override fun canNavigate(): Boolean {
        return element.canNavigate()
    }

    override fun canNavigateToSource(): Boolean {
        return element.canNavigateToSource()
    }

    override fun getAlphaSortKey(): String {
        val name = element.name
        return name.orEmpty()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as SmaliStructureViewElement
        return element == that.element
    }

    override fun hashCode(): Int {
        return Objects.hash(element)
    }

    override fun getPresentation(): ItemPresentation {
        val presentation = element.presentation
        return presentation ?: PresentationData()
    }

    override fun getChildren(): Array<TreeElement> {
        return when (val element = element) {
            is SmaliFile -> element.classes.map { SmaliStructureViewElement(it) as TreeElement }.toTypedArray()
            is SmaliClass -> {
                val fields = element.fields
                val methods = element.methods

                val treeElements = ArrayList<TreeElement>(fields.size + methods.size)
                for (field in fields) {
                    treeElements.add(SmaliStructureViewElement(field))
                }
                for (method in methods) {
                    treeElements.add(SmaliStructureViewElement(method))
                }
                treeElements.toTypedArray()
            }
            else -> emptyArray()
        }
    }
}
