/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.common.arguments

import org.jetbrains.kotlin.cli.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.messages.MessageCollector
import org.jetbrains.kotlin.utils.Jsr305State
import org.jetbrains.kotlin.utils.ReportLevel

class Jsr305Parser(private val collector: MessageCollector) {
    fun parse(value: Array<String>?, supportCompatqualCheckerFrameworkAnnotations: String?): Jsr305State {
        var global: ReportLevel? = null
        var migration: ReportLevel? = null
        val userDefined = mutableMapOf<String, ReportLevel>()

        fun parseJsr305UnderMigration(item: String): ReportLevel? {
            val rawState = item.split(":").takeIf { it.size == 2 }?.get(1)
            return ReportLevel.findByDescription(rawState) ?: reportUnrecognizedJsr305(item).let { null }
        }

        value?.forEach { item ->
            when {
                item.startsWith("@") -> {
                    val (name, state) = parseJsr305UserDefined(item) ?: return@forEach
                    val current = userDefined[name]
                    if (current == null) {
                        userDefined[name] = state
                    } else if (current != state) {
                        reportDuplicateJsr305("@$name:${current.description}", item)
                        return@forEach
                    }
                }
                item.startsWith("under-migration") -> {
                    val state = parseJsr305UnderMigration(item)
                    if (migration == null) {
                        migration = state
                    } else if (migration != state) {
                        reportDuplicateJsr305("under-migration:${migration?.description}", item)
                        return@forEach
                    }
                }
                item == "enable" -> {
                    collector.report(
                        CompilerMessageSeverity.STRONG_WARNING,
                        "Option 'enable' for -Xjsr305 flag is deprecated. Please use 'strict' instead"
                    )
                    if (global != null) return@forEach

                    global = ReportLevel.STRICT
                }
                else -> {
                    if (global == null) {
                        global = ReportLevel.findByDescription(item)
                    } else if (global!!.description != item) {
                        reportDuplicateJsr305(global!!.description, item)
                        return@forEach
                    }
                }
            }
        }

        val enableCompatqualCheckerFrameworkAnnotations = when (supportCompatqualCheckerFrameworkAnnotations) {
            "enable" -> true
            "disable" -> false
            null -> null
            else -> {
                collector.report(
                    CompilerMessageSeverity.ERROR,
                    "Unrecognized -Xsupport-compatqual-checker-framework-annotations option: $supportCompatqualCheckerFrameworkAnnotations. Possible values are 'enable'/'disable'"
                )
                null
            }
        }

        val state = Jsr305State(
            global ?: ReportLevel.WARN, migration, userDefined,
            enableCompatqualCheckerFrameworkAnnotations =
            enableCompatqualCheckerFrameworkAnnotations
                    ?: Jsr305State.COMPATQUAL_CHECKER_FRAMEWORK_ANNOTATIONS_SUPPORT_DEFAULT_VALUE
        )
        return if (state == Jsr305State.DISABLED) Jsr305State.DISABLED else state
    }

    private fun reportUnrecognizedJsr305(item: String) {
        collector.report(CompilerMessageSeverity.ERROR, "Unrecognized -Xjsr305 value: $item")
    }

    private fun reportDuplicateJsr305(first: String, second: String) {
        collector.report(CompilerMessageSeverity.ERROR, "Conflict duplicating -Xjsr305 value: $first, $second")
    }

    private fun parseJsr305UserDefined(item: String): Pair<String, ReportLevel>? {
        val (name, rawState) = item.substring(1).split(":").takeIf { it.size == 2 } ?: run {
            reportUnrecognizedJsr305(item)
            return null
        }

        val state = ReportLevel.findByDescription(rawState) ?: run {
            reportUnrecognizedJsr305(item)
            return null
        }

        return name to state
    }
}
