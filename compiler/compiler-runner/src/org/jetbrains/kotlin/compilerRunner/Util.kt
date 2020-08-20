/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.compilerRunner

import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.nio.charset.StandardCharsets

import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.text.StringFactory

@Throws(IOException::class)
fun loadTextAndClose(stream: InputStream): String {
    return loadTextAndClose((InputStreamReader(stream, StandardCharsets.UTF_8) as Reader))
}

@Throws(IOException::class)
fun loadTextAndClose(reader: Reader): String {
    return reader.use { reader ->
        StringFactory.createShared(FileUtil.adaptiveLoadText(reader))
    }
}