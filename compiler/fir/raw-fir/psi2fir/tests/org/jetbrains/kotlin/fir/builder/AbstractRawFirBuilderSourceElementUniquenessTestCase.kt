/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.builder

import com.intellij.psi.PsiElement
import junit.framework.Assert
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.declarations.FirFile
import org.jetbrains.kotlin.fir.realPsi
import org.jetbrains.kotlin.fir.render
import org.jetbrains.kotlin.fir.visitors.FirVisitor

abstract class AbstractRawFirBuilderSourceElementUniquenessTestCase : AbstractRawFirBuilderTestCase() {
    override fun doRawFirTest(filePath: String) {
        val file = createKtFile(filePath)
        val firFile = file.toFirFile(stubMode = false)
        val errorMappings = UniquenessCheckVisitor.collect(firFile)
        if (errorMappings.isNotEmpty()) {
            val message = buildString {
                appendLine("For the following elements duplicated source elements were found")

                errorMappings.forEach { (ktElement, firElements) ->
                    appendLine("${ktElement::class.simpleName} `${ktElement.text}`:")
                    firElements.forEach { (firElement, parent) ->
                        appendLine(
                            "   ${firElement.renderForTest()} with parent ${parent?.renderForTest()}"
                        )
                    }
                }
            }
            Assert.fail(message)
        }
    }

    private fun FirElement.renderForTest() =
        "${this::class.simpleName}@${System.identityHashCode(this)} `${render()}`"
}

private object UniquenessCheckVisitor : FirVisitor<Unit, UniquenessCheckVisitor.State>() {
    override fun visitElement(element: FirElement, data: State) {
        element.realPsi?.let { psi ->
            data.mappings.getOrPut(psi) { mutableListOf() }.add(element to data.parent)
        }
        element.acceptChildren(this, data.copy(parent = element))
    }

    data class State(val mappings: MutableMap<PsiElement, MutableList<Pair<FirElement, FirElement?>>>, val parent: FirElement?) {
        fun errorMappings() = mappings
            .mapValues { (_, value) -> value.distinctBy { it.first } }
            .filterValues { it.size > 1 }
    }

    fun collect(firFile: FirFile) =
        State(mutableMapOf(), parent = null).also { visitFile(firFile, it) }.errorMappings()
}