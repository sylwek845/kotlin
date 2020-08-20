/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.common.arguments;

public interface K2JsArgumentConstants {
    String CALL = "call";
    String NO_CALL = "noCall";

    String MODULE_PLAIN = "plain";
    String MODULE_AMD = "amd";
    String MODULE_COMMONJS = "commonjs";
    String MODULE_UMD = "umd";

    String SOURCE_MAP_SOURCE_CONTENT_ALWAYS = "always";
    String SOURCE_MAP_SOURCE_CONTENT_NEVER = "never";
    String SOURCE_MAP_SOURCE_CONTENT_INLINING = "inlining";
}
