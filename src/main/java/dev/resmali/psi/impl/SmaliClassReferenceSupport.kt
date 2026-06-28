package dev.resmali.psi.impl

import com.intellij.psi.JavaResolveResult
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiSubstitutor
import com.intellij.psi.infos.CandidateInfo

internal fun PsiReference.isReferenceToClass(element: PsiElement): Boolean {
    return element is PsiClass && element.manager.areElementsEquivalent(element, resolve())
}

internal fun PsiReference.advancedClassResolve(): JavaResolveResult {
    return resolve()?.let { CandidateInfo(it, PsiSubstitutor.EMPTY) } ?: JavaResolveResult.EMPTY
}

internal fun PsiReference.multiClassResolve(): Array<JavaResolveResult> {
    return resolve()?.let { arrayOf(CandidateInfo(it, PsiSubstitutor.EMPTY)) } ?: JavaResolveResult.EMPTY_ARRAY
}
