/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
package org.jetbrains.kotlin.cli.messages

import com.google.common.collect.LinkedHashMultimap
import com.google.common.collect.Multimap
import java.util.Comparator
import kotlin.jvm.javaClass

open class GroupingMessageCollector(private val delegate: MessageCollector, private val treatWarningsAsErrors: Boolean) : MessageCollector {
    // Note that the key in this map can be null
    private val groupedMessages: Multimap<CompilerMessageSourceLocation, Message> = LinkedHashMultimap.create()
    override fun clear() {
        groupedMessages.clear()
    }

    override fun report(
        severity: CompilerMessageSeverity,
        message: String,
        location: CompilerMessageSourceLocation?
    ) {
        if (CompilerMessageSeverity.VERBOSE.contains(severity)) {
            delegate.report(severity, message, location)
        } else {
            groupedMessages.put(location, Message(severity, message, location))
        }
    }

    override fun hasErrors(): Boolean {
        return hasExplicitErrors() || treatWarningsAsErrors && hasWarnings()
    }

    private fun hasExplicitErrors(): Boolean {
        return groupedMessages.entries().stream().anyMatch { entry -> entry.value.severity.isError() }
    }

    private fun hasWarnings(): Boolean {
        return groupedMessages.entries().stream().anyMatch { entry -> entry.value.severity.isWarning() }
    }

    fun flush() {
        val hasExplicitErrors = hasExplicitErrors()
        if (treatWarningsAsErrors && !hasExplicitErrors && hasWarnings()) {
            report(CompilerMessageSeverity.ERROR, "warnings found and -Werror specified", null)
        }
        val sortedKeys: List<CompilerMessageSourceLocation> = groupedMessages.keySet().sortedWith(
            Comparator.nullsFirst(
                CompilerMessageLocationComparator.INSTANCE
            )
        )
        for (location in sortedKeys) {
            for (message in groupedMessages.get(location)) {
                if (!hasExplicitErrors || message.severity.isError || message.severity == CompilerMessageSeverity.STRONG_WARNING) {
                    delegate.report(message.severity, message.message, message.location)
                }
            }
        }
        groupedMessages.clear()
    }

    private class CompilerMessageLocationComparator : Comparator<CompilerMessageSourceLocation> {
        // First, output all messages without any location information. Then, only those with the file path.
        // Next, all messages with the file path and the line number. Next, all messages with file path, line number and column number.
        //
        // Example of the order of compiler messages:
        //
        // error: bad classpath
        // foo.kt: error: bad file
        // foo.kt:42: error: bad line
        // foo.kt:42:43: error: bad character
        override fun compare(o1: CompilerMessageSourceLocation, o2: CompilerMessageSourceLocation): Int {
            if (o1.column == -1 && o2.column != -1) return -1
            if (o1.column != -1 && o2.column == -1) return 1
            if (o1.line == -1 && o2.line != -1) return -1
            return if (o1.line != -1 && o2.line == -1) 1 else o1.path.compareTo(o2.path)
        }

        companion object {
            val INSTANCE = CompilerMessageLocationComparator()
        }
    }

    internal class Message internal constructor(
        internal val severity: CompilerMessageSeverity,
        internal val message: String,
        internal val location: CompilerMessageSourceLocation?
    ) {
        override fun equals(o: Any?): Boolean {
            if (this === o) return true
            if (o == null || javaClass != o.javaClass) return false
            val other = o as Message
            if (location != other.location) return false
            if (message != other.message) return false
            return severity == other.severity
        }

        override fun hashCode(): Int {
            var result = severity.hashCode()
            result = 31 * result + message.hashCode()
            result = 31 * result + (location?.hashCode() ?: 0)
            return result
        }

        override fun toString(): String {
            return "[" + severity + "] " + message + if (location != null) " (at $location)" else " (no location)"
        }
    }
}