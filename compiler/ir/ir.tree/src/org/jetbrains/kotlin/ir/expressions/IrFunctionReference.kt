/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.expressions

import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor
import org.jetbrains.kotlin.name.Name

class IrFunctionReference(
    override val startOffset: Int,
    override val endOffset: Int,
    override var type: IrType,
    override val symbol: IrFunctionSymbol,
    typeArgumentsCount: Int,
    override val valueArgumentsCount: Int,
    val reflectionTarget: IrFunctionSymbol? = symbol,
    override val origin: IrStatementOrigin? = null,
) : IrCallableReference<IrFunctionSymbol>(typeArgumentsCount) {
    @ObsoleteDescriptorBasedAPI
    constructor(
        startOffset: Int,
        endOffset: Int,
        type: IrType,
        symbol: IrFunctionSymbol,
        typeArgumentsCount: Int,
        reflectionTarget: IrFunctionSymbol?,
        origin: IrStatementOrigin? = null
    ) : this(
        startOffset, endOffset,
        type,
        symbol,
        typeArgumentsCount,
        symbol.descriptor.valueParameters.size,
        reflectionTarget,
        origin
    )

    override val referencedName: Name
        get() = symbol.owner.name

    private val argumentsByParameterIndex: Array<IrExpression?> = arrayOfNulls(valueArgumentsCount)

    override fun getValueArgument(index: Int): IrExpression? {
        if (index >= valueArgumentsCount) {
            throw AssertionError("$this: No such value argument slot: $index")
        }
        return argumentsByParameterIndex[index]
    }

    override fun putValueArgument(index: Int, valueArgument: IrExpression?) {
        if (index >= valueArgumentsCount) {
            throw AssertionError("$this: No such value argument slot: $index")
        }
        argumentsByParameterIndex[index] = valueArgument
    }

    override fun removeValueArgument(index: Int) {
        argumentsByParameterIndex[index] = null
    }

    override fun <R, D> accept(visitor: IrElementVisitor<R, D>, data: D): R =
        visitor.visitFunctionReference(this, data)

    override fun <D> acceptChildren(visitor: IrElementVisitor<Unit, D>, data: D) {
        super.acceptChildren(visitor, data)
        argumentsByParameterIndex.forEach { it?.accept(visitor, data) }
    }

    override fun <D> transformChildren(transformer: IrElementTransformer<D>, data: D) {
        super.transformChildren(transformer, data)
        argumentsByParameterIndex.forEachIndexed { i, irExpression ->
            argumentsByParameterIndex[i] = irExpression?.transform(transformer, data)
        }
    }
}

val IrFunctionReference.isWithReflection: Boolean
    get() = reflectionTarget != null

val IrFunctionReference.isAdapterWithReflection: Boolean
    get() = reflectionTarget != null && reflectionTarget != symbol
