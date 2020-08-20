/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.common.arguments

import org.jetbrains.kotlin.common.arguments.copyBean
import java.io.Serializable
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class Freezable {
    protected open inner class FreezableVar<T>(private var value: T) : ReadWriteProperty<Any, T>, Serializable {
        override fun getValue(thisRef: Any, property: KProperty<*>) = value

        override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
            if (frozen) throw IllegalStateException("Instance of ${this::class} is frozen")
            this.value = value
        }
    }

    protected inner class NullableStringFreezableVar(value: String?) : FreezableVar<String?>(value) {
        private val defaultValue = value

        override fun setValue(thisRef: Any, property: KProperty<*>, value: String?) {
            super.setValue(thisRef, property, if (value.isNullOrEmpty()) defaultValue else value)
        }
    }

    private var frozen: Boolean = false

    private fun getInstanceWithFreezeStatus(value: Boolean) = if (value == frozen) this else copyBean(this).apply { frozen = value }

    fun frozen() = getInstanceWithFreezeStatus(true)
    fun unfrozen() = getInstanceWithFreezeStatus(false)
}