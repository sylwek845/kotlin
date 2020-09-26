/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.expressions

import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor

class IrConstructorCall(
    override val startOffset: Int,
    override val endOffset: Int,
    override var type: IrType,
    override val symbol: IrConstructorSymbol,
    typeArgumentsCount: Int,
    val constructorTypeArgumentsCount: Int,
    valueArgumentsCount: Int,
    override val origin: IrStatementOrigin? = null,
) : IrFunctionAccessExpression(typeArgumentsCount, valueArgumentsCount) {
    class ConstructorTypeArguments(internal val irConstructorCall: IrConstructorCall) : AbstractList<IrType?>() {
        override val size: Int
            get() = irConstructorCall.constructorTypeArgumentsCount

        override fun get(index: Int): IrType? =
            if (index >= size)
                throw IndexOutOfBoundsException("index: $index, size: $size")
            else
                irConstructorCall.getConstructorTypeArgument(index)
    }

    class ClassTypeArguments(internal val irConstructorCall: IrConstructorCall) : AbstractList<IrType?>() {
        override val size: Int
            get() = irConstructorCall.classTypeArgumentsCount

        override fun get(index: Int): IrType? =
            if (index >= size)
                throw IndexOutOfBoundsException("index: $index, size: $size")
            else
                irConstructorCall.getTypeArgument(index)
    }

    override fun <R, D> accept(visitor: IrElementVisitor<R, D>, data: D): R =
        visitor.visitConstructorCall(this, data)

    companion object {
        @ObsoleteDescriptorBasedAPI
        fun fromSymbolDescriptor(
            startOffset: Int,
            endOffset: Int,
            type: IrType,
            constructorSymbol: IrConstructorSymbol,
            origin: IrStatementOrigin? = null
        ): IrConstructorCallImpl {
            val constructorDescriptor = constructorSymbol.descriptor
            val classTypeParametersCount = constructorDescriptor.constructedClass.original.declaredTypeParameters.size
            val totalTypeParametersCount = constructorDescriptor.typeParameters.size
            val valueParametersCount = constructorDescriptor.valueParameters.size
            return IrConstructorCallImpl(
                startOffset, endOffset,
                type,
                constructorSymbol,
                typeArgumentsCount = totalTypeParametersCount,
                constructorTypeArgumentsCount = totalTypeParametersCount - classTypeParametersCount,
                valueArgumentsCount = valueParametersCount,
                origin = origin
            )
        }

        fun fromSymbolOwner(
            startOffset: Int,
            endOffset: Int,
            type: IrType,
            constructorSymbol: IrConstructorSymbol,
            classTypeParametersCount: Int,
            origin: IrStatementOrigin? = null
        ): IrConstructorCallImpl {
            val constructor = constructorSymbol.owner
            val constructorTypeParametersCount = constructor.typeParameters.size
            val totalTypeParametersCount = classTypeParametersCount + constructorTypeParametersCount
            val valueParametersCount = constructor.valueParameters.size

            return IrConstructorCallImpl(
                startOffset, endOffset,
                type,
                constructorSymbol,
                totalTypeParametersCount,
                constructorTypeParametersCount,
                valueParametersCount,
                origin
            )
        }

        fun fromSymbolOwner(
            startOffset: Int,
            endOffset: Int,
            type: IrType,
            constructorSymbol: IrConstructorSymbol,
            origin: IrStatementOrigin? = null
        ): IrConstructorCallImpl {
            val constructedClass = constructorSymbol.owner.parentAsClass
            val classTypeParametersCount = constructedClass.typeParameters.size
            return fromSymbolOwner(startOffset, endOffset, type, constructorSymbol, classTypeParametersCount, origin)
        }

        fun fromSymbolOwner(type: IrType, constructorSymbol: IrConstructorSymbol, origin: IrStatementOrigin? = null) =
            fromSymbolOwner(
                UNDEFINED_OFFSET, UNDEFINED_OFFSET, type, constructorSymbol, constructorSymbol.owner.parentAsClass.typeParameters.size,
                origin
            )
    }
}

fun IrConstructorCall.getConstructorTypeArgumentIndex(constructorTypeArgumentIndex: Int) =
    typeArgumentsCount - constructorTypeArgumentsCount + constructorTypeArgumentIndex

fun IrConstructorCall.getConstructorTypeArgument(index: Int): IrType? =
    getTypeArgument(getConstructorTypeArgumentIndex(index))

fun IrConstructorCall.putConstructorTypeArgument(index: Int, type: IrType?) {
    putTypeArgument(getConstructorTypeArgumentIndex(index), type)
}

operator fun IrConstructorCall.ConstructorTypeArguments.set(index: Int, type: IrType?) {
    if (index >= size) throw IndexOutOfBoundsException("index: $index, size: $size")
    irConstructorCall.putConstructorTypeArgument(index, type)
}

val IrConstructorCall.classTypeArgumentsCount: Int
    get() = typeArgumentsCount - constructorTypeArgumentsCount

fun IrConstructorCall.getClassTypeArgument(index: Int): IrType? =
    getTypeArgument(index)

fun IrConstructorCall.putClassTypeArgument(index: Int, type: IrType?) {
    putTypeArgument(index, type)
}

operator fun IrConstructorCall.ClassTypeArguments.set(index: Int, type: IrType?) {
    if (index >= size) throw IndexOutOfBoundsException("index: $index, size: $size")
    irConstructorCall.putClassTypeArgument(index, type)
}

fun IrConstructorCall.getConstructorTypeArguments() =
    IrConstructorCall.ConstructorTypeArguments(this)

fun IrConstructorCall.getClassTypeArguments() =
    IrConstructorCall.ClassTypeArguments(this)

var IrConstructorCall.outerClassReceiver: IrExpression?
    get() = dispatchReceiver
    set(value) {
        dispatchReceiver = value
    }
