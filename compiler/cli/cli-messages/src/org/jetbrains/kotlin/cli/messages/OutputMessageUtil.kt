/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
package org.jetbrains.kotlin.cli.messages

import java.io.File
import java.io.PrintWriter
import java.io.Serializable
import java.io.StringWriter

object OutputMessageUtil {
    private const val SOURCE_FILES_PREFIX = "Sources:"
    private const val OUTPUT_FILES_PREFIX = "Output:"

    fun renderException(e: Throwable): String {
        val out = StringWriter()
        e.printStackTrace(PrintWriter(out))
        return out.toString()
    }

    @JvmStatic
    fun formatOutputMessage(sourceFiles: Collection<File?>, outputFile: File): String {
        return """
            $OUTPUT_FILES_PREFIX
            ${outputFile.path}
            $SOURCE_FILES_PREFIX
            ${sourceFiles.joinToString("\n")}
            """.trimIndent()
    }

    @JvmStatic
    fun parseOutputMessage(message: String): Output? {
        val strings = message.split("\n".toRegex()).toTypedArray()

        // Must have at least one line per prefix
        if (strings.size <= 2) return null
        if (OUTPUT_FILES_PREFIX != strings[0]) return null
        return if (SOURCE_FILES_PREFIX == strings[1]) {
            // Output:
            // Sources:
            // ...
            Output(parseSourceFiles(strings,2), null)
        } else {
            val outputFile = File(strings[1])
            if (SOURCE_FILES_PREFIX != strings[2]) null else Output(
                parseSourceFiles(
                    strings,
                    3
                ), outputFile
            )
        }
    }

    private fun parseSourceFiles(strings: Array<String>, start: Int): Collection<File> {
        val sourceFiles: MutableCollection<File> = arrayListOf()
        for (i in start until strings.size) {
            sourceFiles.add(File(strings[i]))
        }
        return sourceFiles
    }

    class Output(val sourceFiles: Collection<File>, val outputFile: File?) : Serializable {
        companion object {
            const val serialVersionUID = 0L
        }
    }
}