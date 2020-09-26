/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.ir.expressions

import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor

class IrCall(
    override val startOffset: Int,
    override val endOffset: Int,
    override var type: IrType,
    override val symbol: IrSimpleFunctionSymbol,
    typeArgumentsCount: Int,
    valueArgumentsCount: Int,
    override val origin: IrStatementOrigin? = null,
    val superQualifierSymbol: IrClassSymbol? = null,
) : IrFunctionAccessExpression(typeArgumentsCount, valueArgumentsCount) {

    @ObsoleteDescriptorBasedAPI
    constructor(
        startOffset: Int,
        endOffset: Int,
        type: IrType,
        symbol: IrSimpleFunctionSymbol,
        origin: IrStatementOrigin? = null,
        superQualifierSymbol: IrClassSymbol? = null
    ) : this(
        startOffset, endOffset, type, symbol, symbol.descriptor.typeParametersCount, symbol.descriptor.valueParameters.size,
        origin, superQualifierSymbol
    )

    @ObsoleteDescriptorBasedAPI
    constructor(
        startOffset: Int,
        endOffset: Int,
        type: IrType,
        symbol: IrSimpleFunctionSymbol,
        typeArgumentsCount: Int,
        origin: IrStatementOrigin? = null,
        superQualifierSymbol: IrClassSymbol? = null
    ) : this(
        startOffset, endOffset, type, symbol, typeArgumentsCount, symbol.descriptor.valueParameters.size,
        origin, superQualifierSymbol
    )

    override fun <R, D> accept(visitor: IrElementVisitor<R, D>, data: D): R =
        visitor.visitCall(this, data)
}
