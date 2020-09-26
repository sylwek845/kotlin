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

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrElementBase
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor
import org.jetbrains.kotlin.utils.SmartList

interface IrVarargElement : IrElement

class IrVararg(
    override val startOffset: Int,
    override val endOffset: Int,
    override var type: IrType,
    var varargElementType: IrType
) : IrExpression() {
    constructor(
        startOffset: Int,
        endOffset: Int,
        type: IrType,
        varargElementType: IrType,
        elements: List<IrVarargElement>
    ) : this(startOffset, endOffset, type, varargElementType) {
        this.elements.addAll(elements)
    }

    val elements: MutableList<IrVarargElement> = SmartList()

    fun addElement(varargElement: IrVarargElement) {
        elements.add(varargElement)
    }

    fun putElement(i: Int, element: IrVarargElement) {
        elements[i] = element
    }

    override fun <R, D> accept(visitor: IrElementVisitor<R, D>, data: D): R {
        return visitor.visitVararg(this, data)
    }

    override fun <D> acceptChildren(visitor: IrElementVisitor<Unit, D>, data: D) {
        elements.forEach { it.accept(visitor, data) }
    }

    override fun <D> transformChildren(transformer: IrElementTransformer<D>, data: D) {
        elements.forEachIndexed { i, irVarargElement ->
            elements[i] = irVarargElement.transform(transformer, data) as IrVarargElement
        }
    }
}

class IrSpreadElement(
    override val startOffset: Int,
    override val endOffset: Int,
    var expression: IrExpression,
) : IrElementBase(), IrVarargElement {
    override fun <R, D> accept(visitor: IrElementVisitor<R, D>, data: D): R {
        return visitor.visitSpreadElement(this, data)
    }

    override fun <D> transform(transformer: IrElementTransformer<D>, data: D): IrElement =
        accept(transformer, data) as IrSpreadElement

    override fun <D> acceptChildren(visitor: IrElementVisitor<Unit, D>, data: D) {
        expression.accept(visitor, data)
    }

    override fun <D> transformChildren(transformer: IrElementTransformer<D>, data: D) {
        expression = expression.transform(transformer, data)
    }
}
