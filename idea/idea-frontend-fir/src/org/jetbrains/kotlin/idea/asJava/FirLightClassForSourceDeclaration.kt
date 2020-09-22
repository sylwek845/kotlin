/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.idea.asJava

import com.intellij.openapi.util.Comparing
import com.intellij.psi.*
import com.intellij.psi.impl.InheritanceImplUtil
import com.intellij.psi.impl.PsiClassImplUtil
import com.intellij.psi.impl.PsiSuperMethodImplUtil
import com.intellij.psi.search.SearchScope
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.util.IncorrectOperationException
import org.jetbrains.annotations.NonNls
import org.jetbrains.kotlin.analyzer.KotlinModificationTrackerService
import org.jetbrains.kotlin.asJava.classes.*
import org.jetbrains.kotlin.asJava.elements.KtLightField
import org.jetbrains.kotlin.asJava.elements.KtLightIdentifier
import org.jetbrains.kotlin.asJava.elements.KtLightMethod
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.declarations.FirResolvePhase
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.fir.resolve.ScopeSession
import org.jetbrains.kotlin.fir.scopes.unsubstitutedScope
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.idea.fir.low.level.api.api.LowLevelFirApiFacade
import org.jetbrains.kotlin.idea.search.ifTrue
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.load.java.structure.LightClassOriginKind
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.debugText.getDebugText
import org.jetbrains.kotlin.psi.psiUtil.isObjectLiteral
import org.jetbrains.kotlin.psi.stubs.KotlinClassOrObjectStub
import javax.swing.Icon

open class FirLightClassForSourceDeclaration(private val classOrObject: KtClassOrObject) :
    FirLightClassBase(classOrObject.manager),
    StubBasedPsiElement<KotlinClassOrObjectStub<out KtClassOrObject>> {

    private val _modifierList: PsiModifierList? by lazyPub {
        FirLightClassModifierList(this, computeModifiers())
    }

    override fun getModifierList(): PsiModifierList? = _modifierList
    override fun getOwnFields(): List<KtLightField> = emptyList() //TODO()
    override fun getOwnMethods(): List<PsiMethod> = _ownMethods.value
    override fun isDeprecated(): Boolean = false //TODO()
    override fun getNameIdentifier(): KtLightIdentifier? = null //TODO()
    override fun getExtendsList(): PsiReferenceList? = null //TODO()
    override fun getImplementsList(): PsiReferenceList? = null //TODO()
    override fun getTypeParameterList(): PsiTypeParameterList? = null //TODO()
    override fun getTypeParameters(): Array<PsiTypeParameter> = emptyArray() //TODO()
    override fun getOwnInnerClasses(): List<PsiClass> = emptyList() //TODO()

    override fun getTextOffset() = kotlinOrigin.textOffset
    override fun getStartOffsetInParent() = kotlinOrigin.startOffsetInParent
    override fun isWritable() = kotlinOrigin.isWritable
    override val kotlinOrigin: KtClassOrObject = classOrObject

    private fun ownMethods(): List<KtLightMethod> {

        val result = mutableListOf<KtLightMethod>()

        val resolveState = LowLevelFirApiFacade.getResolveStateFor(classOrObject)

        fun KtNamedFunction.createMethods(result: MutableList<KtLightMethod>) {
            LowLevelFirApiFacade.withFirDeclaration(this, resolveState, FirResolvePhase.IMPLICIT_TYPES_BODY_RESOLVE) {
                if (it is FirSimpleFunction) {
                    result.add(FirLightMethodForFirNode(it, null, this@FirLightClassForSourceDeclaration))
                }
            }
        }

        fun KtProperty.createProperties(result: MutableList<KtLightMethod>) {
            LowLevelFirApiFacade.withFirDeclaration(this, resolveState, FirResolvePhase.IMPLICIT_TYPES_BODY_RESOLVE) {
                if (it is FirPropertySymbol) {

                }
            }
        }

        for (declaration in this.classOrObject.declarations) { // TODO .filterNot { it.isHiddenByDeprecation(support) }) {
            if (declaration.hasModifier(KtTokens.PRIVATE_KEYWORD) && isInterface) continue
            when (declaration) {
                is KtNamedFunction -> declaration.createMethods(result)
                is KtProperty -> declaration.createProperties(result)
            }
        }
//
//        for (parameter in propertyParameters()) {
//            result.addAll(
//                membersBuilder.propertyAccessors(
//                    parameter,
//                    parameter.isMutable,
//                    forceStatic = false,
//                    onlyJvmStatic = false,
//                    createAsAnnotationMethod = isAnnotationType
//                )
//            )
//        }
//
//        if (!isInterface) {
//            result.addAll(createConstructors())
//        }
//
//        this.classOrObject.companionObjects.firstOrNull()?.let { companion ->
//            for (declaration in companion.declarations.filterNot { isHiddenByDeprecation(it) }) {
//                when (declaration) {
//                    is KtNamedFunction ->
//                        if (isJvmStatic(declaration)) result.addAll(membersBuilder.createMethods(declaration, forceStatic = true))
//                    is KtProperty -> result.addAll(
//                        membersBuilder.propertyAccessors(
//                            declaration,
//                            declaration.isVar,
//                            forceStatic = false,
//                            onlyJvmStatic = true
//                        )
//                    )
//                }
//            }
//        }
//
//        addMethodsFromDataClass(result)
//        addDelegatesToInterfaceMethods(result)
//



        return result
    }

    private val _ownMethods: CachedValue<List<KtLightMethod>> = CachedValuesManager.getManager(project).createCachedValue(
        {
            CachedValueProvider.Result.create(
                ownMethods(),
                classOrObject.getExternalDependencies()
            )
        }, false
    )

    private val _containingFile: PsiFile by lazyPub {
        val containingClass =
            (!classOrObject.isTopLevel()).ifTrue { create(getOutermostClassOrObject(classOrObject)) } ?: this
        FirFakeFileImpl(classOrObject, containingClass)
    }

    override fun getContainingFile(): PsiFile? = _containingFile

    override fun getNavigationElement(): PsiElement = classOrObject

    override fun isEquivalentTo(another: PsiElement?): Boolean {
        return kotlinOrigin.isEquivalentTo(another) ||
                another is KtLightClassForSourceDeclaration && Comparing.equal(another.qualifiedName, qualifiedName)
    }

    override fun getElementIcon(flags: Int): Icon? =
        throw UnsupportedOperationException("This should be done by JetIconProvider")

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class.java != other::class.java) return false

        val aClass = other as FirLightClassForSourceDeclaration

        if (classOrObject != aClass.classOrObject) return false

        return true
    }

    override fun hashCode(): Int = classOrObject.hashCode()

    override fun getName(): String? = classOrObject.nameAsName?.asString()

    protected open fun computeModifiers(): Set<String> {
        val psiModifiers = hashSetOf<String>()

        for (tokenAndModifier in jetTokenToPsiModifier) {
            if (classOrObject.hasModifier(tokenAndModifier.first)) {
                psiModifiers.add(tokenAndModifier.second)
            }
        }

        if (classOrObject.hasModifier(KtTokens.PRIVATE_KEYWORD)) {
            // Top-level private class has PACKAGE_LOCAL visibility in Java
            // Nested private class has PRIVATE visibility
            psiModifiers.add(if (classOrObject.isTopLevel()) PsiModifier.PACKAGE_LOCAL else PsiModifier.PRIVATE)
        } else if (!psiModifiers.contains(PsiModifier.PROTECTED)) {
            psiModifiers.add(PsiModifier.PUBLIC)
        }

        // FINAL
        if (isAbstract() || isSealed()) {
            psiModifiers.add(PsiModifier.ABSTRACT)
        } else if (!(classOrObject.hasModifier(KtTokens.OPEN_KEYWORD) || (classOrObject is KtClass && classOrObject.isEnum()))) {
            psiModifiers.add(PsiModifier.FINAL)
        }

        if (!classOrObject.isTopLevel() && !classOrObject.hasModifier(KtTokens.INNER_KEYWORD)) {
            psiModifiers.add(PsiModifier.STATIC)
        }

        return psiModifiers
    }

    private fun isAbstract(): Boolean = classOrObject.hasModifier(KtTokens.ABSTRACT_KEYWORD) || isInterface
    private fun isSealed(): Boolean = classOrObject.hasModifier(KtTokens.SEALED_KEYWORD)
    override fun hasModifierProperty(@NonNls name: String): Boolean = modifierList?.hasModifierProperty(name) ?: false

    override fun isInterface(): Boolean =
        classOrObject is KtClass && (classOrObject.isInterface() || classOrObject.isAnnotation())

    override fun isAnnotationType(): Boolean =
        classOrObject is KtClass && classOrObject.isAnnotation()

    override fun isEnum(): Boolean =
        classOrObject is KtClass && classOrObject.isEnum()

    override fun hasTypeParameters(): Boolean =
        classOrObject is KtClass && classOrObject.typeParameters.isNotEmpty()

    override fun isValid(): Boolean = classOrObject.isValid

    override fun isInheritor(baseClass: PsiClass, checkDeep: Boolean): Boolean =
        InheritanceImplUtil.isInheritor(this, baseClass, checkDeep)

    @Throws(IncorrectOperationException::class)
    override fun setName(@NonNls name: String): PsiElement =
        throw IncorrectOperationException()

    override fun toString() =
        "${this::class.java.simpleName}:${classOrObject.getDebugText()}"

    override fun getUseScope(): SearchScope = kotlinOrigin.useScope
    override fun getElementType(): IStubElementType<out StubElement<*>, *>? = classOrObject.elementType
    override fun getStub(): KotlinClassOrObjectStub<out KtClassOrObject>? = classOrObject.stub

    override val originKind: LightClassOriginKind
        get() = LightClassOriginKind.SOURCE

    override fun getQualifiedName() = classOrObject.fqName?.asString()

    override fun getInterfaces(): Array<PsiClass> = PsiClassImplUtil.getInterfaces(this)
    override fun getSuperClass(): PsiClass? = PsiClassImplUtil.getSuperClass(this)
    override fun getSupers(): Array<PsiClass> = PsiClassImplUtil.getSupers(this)
    override fun getSuperTypes(): Array<PsiClassType> = PsiClassImplUtil.getSuperTypes(this)
    override fun getVisibleSignatures(): MutableCollection<HierarchicalMethodSignature> = PsiSuperMethodImplUtil.getVisibleSignatures(this)

    override fun getRBrace(): PsiElement? = null
    override fun getLBrace(): PsiElement? = null

    override fun getInitializers(): Array<PsiClassInitializer> = emptyArray()

    override fun getContainingClass(): PsiClass? {

        val containingBody = classOrObject.parent as? KtClassBody
        val containingClass = containingBody?.parent as? KtClassOrObject
        containingClass?.let { return create(it) }

        val containingBlock = classOrObject.parent as? KtBlockExpression
        val containingScript = containingBlock?.parent as? KtScript
        containingScript?.let { return KtLightClassForScript.create(it) }

        return null
    }

    override fun getParent(): PsiElement? = containingClass ?: containingFile

    override fun getScope(): PsiElement? = parent

    override fun isInheritorDeep(baseClass: PsiClass?, classToByPass: PsiClass?): Boolean =
        baseClass?.let { InheritanceImplUtil.isInheritorDeep(this, it, classToByPass) } ?: false

    override fun copy(): FirLightClassForSourceDeclaration =
        FirLightClassForSourceDeclaration(classOrObject.copy() as KtClassOrObject)

    companion object {
        private val jetTokenToPsiModifier = listOf(
            KtTokens.PUBLIC_KEYWORD to PsiModifier.PUBLIC,
            KtTokens.INTERNAL_KEYWORD to PsiModifier.PUBLIC,
            KtTokens.PROTECTED_KEYWORD to PsiModifier.PROTECTED,
            KtTokens.FINAL_KEYWORD to PsiModifier.FINAL
        )

        fun create(classOrObject: KtClassOrObject): FirLightClassForSourceDeclaration? =
            CachedValuesManager.getCachedValue(classOrObject) {
                CachedValueProvider.Result
                    .create(
                        createNoCache(classOrObject),
                        KotlinModificationTrackerService.getInstance(classOrObject.project).outOfBlockModificationTracker
                    )
            }

        fun createNoCache(classOrObject: KtClassOrObject): FirLightClassForSourceDeclaration? {
            val containingFile = classOrObject.containingFile
            if (containingFile is KtCodeFragment) {
                // Avoid building light classes for code fragments
                return null
            }

            if (classOrObject.shouldNotBeVisibleAsLightClass()) {
                return null
            }

            return when {
                classOrObject.isObjectLiteral() -> return null //TODO
                classOrObject.safeIsLocal() -> return null //TODO
                classOrObject.hasModifier(KtTokens.INLINE_KEYWORD) -> return null //TODO
                else -> FirLightClassForSourceDeclaration(classOrObject)
            }
        }
    }
}