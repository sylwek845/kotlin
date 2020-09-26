/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.expressions

import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrFieldSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor

class IrSetField(
    override val startOffset: Int,
    override val endOffset: Int,
    override val symbol: IrFieldSymbol,
    override var type: IrType,
    override val origin: IrStatementOrigin? = null,
    override val superQualifierSymbol: IrClassSymbol? = null,
) : IrFieldAccessExpression() {
    constructor(
        startOffset: Int, endOffset: Int,
        symbol: IrFieldSymbol,
        receiver: IrExpression?,
        value: IrExpression,
        type: IrType,
        origin: IrStatementOrigin? = null,
        superQualifierSymbol: IrClassSymbol? = null
    ) : this(startOffset, endOffset, symbol, type, origin, superQualifierSymbol) {
        this.receiver = receiver
        this.value = value
    }

    lateinit var value: IrExpression

    override fun <R, D> accept(visitor: IrElementVisitor<R, D>, data: D): R {
        return visitor.visitSetField(this, data)
    }

    override fun <D> acceptChildren(visitor: IrElementVisitor<Unit, D>, data: D) {
        receiver?.accept(visitor, data)
        value.accept(visitor, data)
    }

    override fun <D> transformChildren(transformer: IrElementTransformer<D>, data: D) {
        receiver = receiver?.transform(transformer, data)
        value = value.transform(transformer, data)
    }
}
