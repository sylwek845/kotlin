/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.messages;

import org.jetbrains.annotations.NotNull;
import java.util.EnumSet;

public enum CompilerMessageSeverity {
    EXCEPTION,
    ERROR,
    // Unlike a normal warning, a strong warning is not discarded when there are compilation errors.
    // Use it for problems related to configuration, not the diagnostics
    STRONG_WARNING,
    WARNING,
    INFO,
    LOGGING,
    /**
     * Source to output files mapping messages (e.g A.kt->A.class).
     * It is needed for incremental compilation.
     */
    OUTPUT;

    public static final EnumSet<CompilerMessageSeverity> VERBOSE = EnumSet.of(LOGGING);

    public boolean isError() {
        return this == EXCEPTION || this == ERROR;
    }

    public boolean isWarning() {
        return this == STRONG_WARNING || this == WARNING;
    }

    @NotNull
    public String getPresentableName() {
        switch (this) {
            case EXCEPTION:
                return "exception";
            case ERROR:
                return "error";
            case STRONG_WARNING:
            case WARNING:
                return "warning";
            case INFO:
                return "info";
            case LOGGING:
                return "logging";
            case OUTPUT:
                return "output";
            default:
                throw new UnsupportedOperationException("Unknown severity: " + this);
        }
    }
}
