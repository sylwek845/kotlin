/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.config.common

val KOTLIN_COMPILER_ENVIRONMENT_KEEPALIVE_PROPERTY = "kotlin.environment.keepalive"


fun String?.toBooleanLenient(): Boolean? = when (this?.toLowerCase()) {
    null -> false
    in listOf("", "yes", "true", "on", "y") -> true
    in listOf("no", "false", "off", "n") -> false
    else -> null
}
