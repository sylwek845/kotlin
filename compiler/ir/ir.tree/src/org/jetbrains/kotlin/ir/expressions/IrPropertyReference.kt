/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.expressions

import org.jetbrains.kotlin.ir.symbols.IrFieldSymbol
import org.jetbrains.kotlin.ir.symbols.IrPropertySymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor
import org.jetbrains.kotlin.name.Name

class IrPropertyReference(
    override val startOffset: Int,
    override val endOffset: Int,
    override var type: IrType,
    override val symbol: IrPropertySymbol,
    typeArgumentsCount: Int,
    val field: IrFieldSymbol?,
    val getter: IrSimpleFunctionSymbol?,
    val setter: IrSimpleFunctionSymbol?,
    override val origin: IrStatementOrigin? = null,
) : IrCallableReference<IrPropertySymbol>(typeArgumentsCount) {
    override val valueArgumentsCount: Int
        get() = 0

    override val referencedName: Name
        get() = symbol.owner.name

    private fun throwNoValueArguments(): Nothing =
        throw UnsupportedOperationException("Property reference $symbol has no value arguments")

    override fun getValueArgument(index: Int): IrExpression? = throwNoValueArguments()

    override fun putValueArgument(index: Int, valueArgument: IrExpression?): Unit = throwNoValueArguments()

    override fun removeValueArgument(index: Int): Unit = throwNoValueArguments()

    override fun <R, D> accept(visitor: IrElementVisitor<R, D>, data: D): R =
        visitor.visitPropertyReference(this, data)
}
