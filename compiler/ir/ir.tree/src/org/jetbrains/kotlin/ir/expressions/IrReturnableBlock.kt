/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.expressions

import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrReturnTarget
import org.jetbrains.kotlin.ir.declarations.IrSymbolOwner
import org.jetbrains.kotlin.ir.symbols.IrFileSymbol
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrReturnableBlockSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor

class IrReturnableBlock(
    override val startOffset: Int,
    override val endOffset: Int,
    override var type: IrType,
    override val symbol: IrReturnableBlockSymbol,
    override val origin: IrStatementOrigin? = null,
    val inlineFunctionSymbol: IrFunctionSymbol? = null,
) : IrBlock(), IrSymbolOwner, IrReturnTarget {
    @ObsoleteDescriptorBasedAPI
    override val descriptor: FunctionDescriptor
        get() = symbol.descriptor

    constructor(
        startOffset: Int,
        endOffset: Int,
        type: IrType,
        symbol: IrReturnableBlockSymbol,
        origin: IrStatementOrigin?,
        statements: List<IrStatement>,
        inlineFunctionSymbol: IrFunctionSymbol? = null
    ) : this(startOffset, endOffset, type, symbol, origin, inlineFunctionSymbol) {
        this.statements.addAll(statements)
    }

    init {
        symbol.bind(this)
    }

    override fun <D> acceptChildren(visitor: IrElementVisitor<Unit, D>, data: D) {
        statements.forEach { it.accept(visitor, data) }
    }

    override fun <D> transformChildren(transformer: IrElementTransformer<D>, data: D) {
        statements.forEachIndexed { i, irStatement ->
            statements[i] = irStatement.transform(transformer, data) as IrStatement
        }
    }
}

@Suppress("unused") // Used in kotlin-native
val IrReturnableBlock.sourceFileSymbol: IrFileSymbol?
    get() = inlineFunctionSymbol?.owner?.file?.symbol
