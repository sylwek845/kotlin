/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.idea.asJava

import com.intellij.psi.PsiManager
import com.intellij.psi.util.CachedValue
import org.jetbrains.kotlin.asJava.LightClassBuilder
import org.jetbrains.kotlin.asJava.LightClassGenerationSupport
import org.jetbrains.kotlin.asJava.builder.LightClassDataHolder
import org.jetbrains.kotlin.asJava.classes.*
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.BindingContext

class FirLightClassGenerationSupport : LightClassGenerationSupport() {
    override fun createDataHolderForClass(classOrObject: KtClassOrObject, builder: LightClassBuilder): LightClassDataHolder.ForClass = TODO()

    override fun createDataHolderForFacade(files: Collection<KtFile>, builder: LightClassBuilder): LightClassDataHolder.ForFacade = TODO()

    override fun createDataHolderForScript(script: KtScript, builder: LightClassBuilder): LightClassDataHolder.ForScript = TODO()

    override fun resolveToDescriptor(declaration: KtDeclaration): DeclarationDescriptor? = TODO()

    override fun analyze(element: KtElement): BindingContext = TODO()

    override fun analyzeAnnotation(element: KtAnnotationEntry): AnnotationDescriptor? = TODO()

    override fun analyzeWithContent(element: KtClassOrObject): BindingContext = TODO()

    override fun getUltraLightClassSupport(element: KtElement): KtUltraLightSupport = TODO()

    override val useUltraLightClasses: Boolean = true

    override fun createUltraLightClass(element: KtClassOrObject): KtLightClass? {

        if (!useUltraLightClasses) return null

        if (element.shouldNotBeVisibleAsLightClass()) {
            return null
        }

        return when {
            element is KtObjectDeclaration && element.isObjectLiteral() -> TODO()

            element.safeIsLocal() -> TODO()

            (element.hasModifier(KtTokens.INLINE_KEYWORD)) -> TODO()

            else -> FirLightClassForSourceDeclaration(element)
        }
    }

    override fun createUltraLightClassForFacade(
        manager: PsiManager,
        facadeClassFqName: FqName,
        lightClassDataCache: CachedValue<LightClassDataHolder.ForFacade>,
        files: Collection<KtFile>
    ): KtLightClass? = TODO()

    override fun createUltraLightClassForScript(script: KtScript): KtLightClass? = TODO()
}