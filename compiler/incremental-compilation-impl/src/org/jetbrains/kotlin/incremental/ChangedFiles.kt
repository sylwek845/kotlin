/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.incremental

import java.io.File
import java.io.Serializable

sealed class ChangedFiles : Serializable {
    class Known(val modified: List<File>, val removed: List<File>) : ChangedFiles()
    class Unknown : ChangedFiles()

    companion object {
        const val serialVersionUID: Long = 0
    }
}
