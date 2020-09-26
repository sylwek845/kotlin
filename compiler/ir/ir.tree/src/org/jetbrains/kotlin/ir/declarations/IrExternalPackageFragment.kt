/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.declarations

import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.impl.EmptyPackageFragmentDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.impl.IrExternalPackageFragmentImpl
import org.jetbrains.kotlin.ir.symbols.IrExternalPackageFragmentSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrExternalPackageFragmentSymbolImpl
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedContainerSource
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedMemberDescriptor

class IrExternalPackageFragment(
    override val symbol: IrExternalPackageFragmentSymbol,
    override val fqName: FqName,
) : IrPackageFragment() {
    override val startOffset: Int
        get() = UNDEFINED_OFFSET

    override val endOffset: Int
        get() = UNDEFINED_OFFSET

    init {
        symbol.bind(this)
    }

    @ObsoleteDescriptorBasedAPI
    override val packageFragmentDescriptor: PackageFragmentDescriptor
        get() = symbol.descriptor

    override val declarations: MutableList<IrDeclaration> = ArrayList()

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    val containerSource: DeserializedContainerSource?
        get() = (symbol.descriptor as? DeserializedMemberDescriptor)?.containerSource

    override fun <R, D> accept(visitor: IrElementVisitor<R, D>, data: D): R =
        visitor.visitExternalPackageFragment(this, data)

    override fun <D> acceptChildren(visitor: IrElementVisitor<Unit, D>, data: D) {
        declarations.forEach { it.accept(visitor, data) }
    }

    override fun <D> transformChildren(transformer: IrElementTransformer<D>, data: D) {
        declarations.forEachIndexed { i, irDeclaration ->
            declarations[i] = irDeclaration.transform(transformer, data) as IrDeclaration
        }
    }

    companion object {
        fun createEmptyExternalPackageFragment(module: ModuleDescriptor, fqName: FqName): IrExternalPackageFragment =
            IrExternalPackageFragmentImpl(
                IrExternalPackageFragmentSymbolImpl(EmptyPackageFragmentDescriptor(module, fqName)), fqName
            )
    }
}
