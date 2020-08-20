/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.config.common;

public enum ExitCode {
    OK(0),
    COMPILATION_ERROR(1),
    INTERNAL_ERROR(2),
    SCRIPT_EXECUTION_ERROR(3);

    private final int code;

    ExitCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
