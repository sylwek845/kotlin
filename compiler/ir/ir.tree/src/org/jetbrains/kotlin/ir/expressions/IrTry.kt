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

import org.jetbrains.kotlin.ir.IrElementBase
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor
import org.jetbrains.kotlin.utils.SmartList

class IrTry(
    override val startOffset: Int,
    override val endOffset: Int,
    override var type: IrType,
) : IrExpression() {
    constructor(
        startOffset: Int,
        endOffset: Int,
        type: IrType,
        tryResult: IrExpression,
        catches: List<IrCatch>,
        finallyExpression: IrExpression?
    ) : this(startOffset, endOffset, type) {
        this.tryResult = tryResult
        this.catches.addAll(catches)
        this.finallyExpression = finallyExpression
    }

    lateinit var tryResult: IrExpression

    val catches: MutableList<IrCatch> = SmartList()

    var finallyExpression: IrExpression? = null

    override fun <R, D> accept(visitor: IrElementVisitor<R, D>, data: D): R {
        return visitor.visitTry(this, data)
    }

    override fun <D> acceptChildren(visitor: IrElementVisitor<Unit, D>, data: D) {
        tryResult.accept(visitor, data)
        catches.forEach { it.accept(visitor, data) }
        finallyExpression?.accept(visitor, data)
    }

    override fun <D> transformChildren(transformer: IrElementTransformer<D>, data: D) {
        tryResult = tryResult.transform(transformer, data)
        catches.forEachIndexed { i, irCatch ->
            catches[i] = irCatch.transform(transformer, data)
        }
        finallyExpression = finallyExpression?.transform(transformer, data)
    }
}

class IrCatch(
    override val startOffset: Int,
    override val endOffset: Int,
    var catchParameter: IrVariable,
) : IrElementBase() {
    constructor(
        startOffset: Int,
        endOffset: Int,
        catchParameter: IrVariable,
        result: IrExpression
    ) : this(startOffset, endOffset, catchParameter) {
        this.result = result
    }

    lateinit var result: IrExpression

    override fun <R, D> accept(visitor: IrElementVisitor<R, D>, data: D): R {
        return visitor.visitCatch(this, data)
    }

    override fun <D> transform(transformer: IrElementTransformer<D>, data: D): IrCatch =
        super.transform(transformer, data) as IrCatch

    override fun <D> acceptChildren(visitor: IrElementVisitor<Unit, D>, data: D) {
        catchParameter.accept(visitor, data)
        result.accept(visitor, data)
    }

    override fun <D> transformChildren(transformer: IrElementTransformer<D>, data: D) {
        catchParameter = catchParameter.transform(transformer, data) as IrVariable
        result = result.transform(transformer, data)
    }
}
