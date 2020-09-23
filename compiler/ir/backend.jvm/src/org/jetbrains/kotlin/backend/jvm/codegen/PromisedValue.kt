/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.jvm.codegen

import org.jetbrains.kotlin.backend.jvm.JvmLoweredDeclarationOrigin
import org.jetbrains.kotlin.backend.jvm.ir.eraseTypeParameters
import org.jetbrains.kotlin.backend.jvm.ir.erasedUpperBound
import org.jetbrains.kotlin.backend.jvm.lower.inlineclasses.InlineClassAbi
import org.jetbrains.kotlin.codegen.AsmUtil
import org.jetbrains.kotlin.codegen.StackValue
import org.jetbrains.kotlin.ir.descriptors.toIrBasedKotlinType
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.resolve.jvm.AsmTypes
import org.jetbrains.org.objectweb.asm.Label
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter

// A value that may not have been fully constructed yet. The ability to "roll back" code generation
// is useful for certain optimizations.
abstract class PromisedValue(val codegen: ExpressionCodegen, val type: Type, val irType: IrType) {
    // If this value is immaterial, construct an object on the top of the stack. This
    // must always be done before generating other values or emitting raw bytecode.
    open fun materializeAt(target: Type, irTarget: IrType) = coerceOnStack(target, irTarget)

    protected fun coerceOnStack(target: Type, irTarget: IrType) {
        val erasedSourceType = irType.eraseTypeParameters()
        val erasedTargetType = irTarget.eraseTypeParameters()
        val isFromTypeInlineClass = erasedSourceType.classOrNull!!.owner.isInline
        val isToTypeInlineClass = erasedTargetType.classOrNull!!.owner.isInline

        // Boxing and unboxing kotlin.Result leads to CCE in generated code
        val doNotCoerceKotlinResultInContinuation =
            (codegen.irFunction.parentAsClass.origin == JvmLoweredDeclarationOrigin.CONTINUATION_CLASS ||
                    codegen.irFunction.parentAsClass.origin == JvmLoweredDeclarationOrigin.SUSPEND_LAMBDA)
                    && (irType.isKotlinResult() || irTarget.isKotlinResult())

        // Coerce inline classes
        if ((isFromTypeInlineClass || isToTypeInlineClass) && !doNotCoerceKotlinResultInContinuation) {
            val isFromTypeUnboxed = isFromTypeInlineClass && typeMapper.mapType(erasedSourceType.unboxed) == type
            val isToTypeUnboxed = isToTypeInlineClass && typeMapper.mapType(erasedTargetType.unboxed) == target

            when {
                isFromTypeUnboxed && !isToTypeUnboxed -> {
                    StackValue.boxInlineClass(erasedSourceType.toIrBasedKotlinType(), mv)
                    return
                }

                !isFromTypeUnboxed && isToTypeUnboxed -> {
                    StackValue.unboxInlineClass(type, erasedTargetType.toIrBasedKotlinType(), mv)
                    return
                }
            }
        }

        if (type != target) {
            StackValue.coerce(type, target, mv)
        }
    }

    abstract fun discard()

    val mv: InstructionAdapter
        get() = codegen.mv

    val typeMapper: IrTypeMapper
        get() = codegen.typeMapper
}

// A value that *has* been fully constructed.
class MaterialValue(codegen: ExpressionCodegen, type: Type, irType: IrType) : PromisedValue(codegen, type, irType) {
    override fun discard() {
        if (type !== Type.VOID_TYPE)
            AsmUtil.pop(mv, type)
    }
}

// A value that can be branched on. JVM has certain branching instructions which can be used
// to optimize these.
abstract class BooleanValue(codegen: ExpressionCodegen, type: Type, irType: IrType) : PromisedValue(codegen, type, irType) {
    constructor(codegen: ExpressionCodegen) : this(codegen, Type.BOOLEAN_TYPE, codegen.context.irBuiltIns.booleanType)

    abstract fun jumpIfFalse(target: Label)
    abstract fun jumpIfTrue(target: Label)

    override fun materializeAt(target: Type, irTarget: IrType) {
        val const0 = Label()
        val end = Label()
        jumpIfFalse(const0)
        mv.iconst(1)
        mv.goTo(end)
        mv.mark(const0)
        mv.iconst(0)
        mv.mark(end)
        if (Type.BOOLEAN_TYPE != target) {
            StackValue.coerce(Type.BOOLEAN_TYPE, target, mv)
        }
    }
}

class ConstantValue(codegen: ExpressionCodegen, val value: Any?, irType: IrType) : BooleanValue(codegen, value.asmType(), irType) {
    override fun materializeAt(target: Type, irTarget: IrType) {
        when (value) {
            is Boolean -> mv.iconst(if (value) 1 else 0)
            is Char -> mv.iconst(value.toInt())
            is Long -> mv.lconst(value)
            is Float -> mv.fconst(value)
            is Double -> mv.dconst(value)
            is Number -> mv.iconst(value.toInt())
            else -> mv.aconst(value)
        }
        // `null` is of any reference type.
        if (value != null || AsmUtil.isPrimitive(target)) {
            coerceOnStack(target, irTarget)
        }
    }

    override fun discard() {
        // Add something for the line number to be attached to.
        mv.nop()
    }

    override fun jumpIfFalse(target: Label) = if (isDefaultValueForType(type, value)) mv.goTo(target) else mv.nop()
    override fun jumpIfTrue(target: Label) = if (!isDefaultValueForType(type, value)) mv.goTo(target) else mv.nop()

    companion object {
        private fun Any?.asmType(): Type = when (this) {
            is Boolean -> Type.BOOLEAN_TYPE
            is Byte -> Type.BYTE_TYPE
            is Char -> Type.CHAR_TYPE
            is Long -> Type.LONG_TYPE
            is Short -> Type.SHORT_TYPE
            is Float -> Type.FLOAT_TYPE
            is Double -> Type.DOUBLE_TYPE
            is Number -> Type.INT_TYPE
            is String -> AsmTypes.JAVA_STRING_TYPE
            else -> AsmTypes.OBJECT_TYPE
        }

        /**
         * Returns true if the given constant value is the JVM's default value for the given type.
         * See: https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-2.html#jvms-2.3
         */
        fun isDefaultValueForType(type: Type, value: Any?): Boolean =
            when (type) {
                Type.BOOLEAN_TYPE -> value is Boolean && !value
                Type.CHAR_TYPE -> value is Char && value.toInt() == 0
                Type.BYTE_TYPE, Type.SHORT_TYPE, Type.INT_TYPE, Type.LONG_TYPE -> value is Number && value.toLong() == 0L
                // Must use `equals` for these two to differentiate between +0.0 and -0.0:
                Type.FLOAT_TYPE -> value is Number && value.toFloat().equals(0.0f)
                Type.DOUBLE_TYPE -> value is Number && value.toDouble().equals(0.0)
                else -> !AsmUtil.isPrimitive(type) && value == null
            }
    }
}

fun PromisedValue.coerceToBoolean(): BooleanValue =
    when (this) {
        is BooleanValue -> this
        else -> object : BooleanValue(codegen) {
            override fun jumpIfFalse(target: Label) =
                this@coerceToBoolean.materializeAt(Type.BOOLEAN_TYPE, codegen.context.irBuiltIns.booleanType).also { mv.ifeq(target) }

            override fun jumpIfTrue(target: Label) =
                this@coerceToBoolean.materializeAt(Type.BOOLEAN_TYPE, codegen.context.irBuiltIns.booleanType).also { mv.ifne(target) }

            override fun discard() {
                this@coerceToBoolean.discard()
            }
        }
    }


fun PromisedValue.materializedAt(target: Type, irTarget: IrType): MaterialValue {
    materializeAt(target, irTarget)
    return MaterialValue(codegen, target, irTarget)
}

fun PromisedValue.materialized(): MaterialValue =
    materializedAt(type, irType)

fun PromisedValue.materializedAt(irTarget: IrType): MaterialValue =
    materializedAt(typeMapper.mapType(irTarget), irTarget)

fun PromisedValue.materializedAtBoxed(irTarget: IrType): MaterialValue =
    materializedAt(typeMapper.boxType(irTarget), irTarget)

fun PromisedValue.materialize() {
    materializeAt(type, irType)
}

fun PromisedValue.materializeAt(irTarget: IrType) {
    materializeAt(typeMapper.mapType(irTarget), irTarget)
}

fun PromisedValue.materializeAtBoxed(irTarget: IrType) {
    materializeAt(typeMapper.boxType(irTarget), irTarget)
}

val IrType.unboxed: IrType
    get() = InlineClassAbi.getUnderlyingType(erasedUpperBound)

// A Non-materialized value of Unit type that is only materialized through coercion.
val ExpressionCodegen.unitValue: PromisedValue
    get() = MaterialValue(this, Type.VOID_TYPE, context.irBuiltIns.unitType)
