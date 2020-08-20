/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.common.arguments

import org.jetbrains.kotlin.cli.common.arguments.Argument

class K2MetadataCompilerArguments : CommonCompilerArguments() {
    companion object {
        @JvmStatic private val serialVersionUID = 0L
    }

    @Argument(value = "-d", valueDescription = "<directory|jar>", description = "Destination for generated .kotlin_metadata files")
    var destination: String? by NullableStringFreezableVar(null)

    @Argument(
            value = "-classpath",
            shortName = "-cp",
            valueDescription = "<path>",
            description = "Paths where to find library .kotlin_metadata files"
    )
    var classpath: String? by NullableStringFreezableVar(null)

    @Argument(value = "-module-name", valueDescription = "<name>", description = "Name of the generated .kotlin_module file")
    var moduleName: String? by NullableStringFreezableVar(null)

    @Argument(
        value = "-Xjps",
        description = "Enable in JPS"
    )
    var enabledInJps: Boolean by FreezableVar(false)

    @Argument(
        value = "-Xfriend-paths",
        valueDescription = "<path>",
        description = "Paths to output directories for friend modules (whose internals should be visible)"
    )
    var friendPaths: Array<String>? by FreezableVar(null)

    @Argument(
        value = "-Xrefines-paths",
        valueDescription = "<path>",
        description = "Paths to output directories for refined modules (whose expects this module can actualize)"
    )
    var refinesPaths: Array<String>? by FreezableVar(null)
}
