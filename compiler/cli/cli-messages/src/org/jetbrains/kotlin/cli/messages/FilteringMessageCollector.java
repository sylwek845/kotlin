/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.messages;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class FilteringMessageCollector implements MessageCollector {
    private final MessageCollector messageCollector;
    private final Predicate<CompilerMessageSeverity> decline;

    public FilteringMessageCollector(@NotNull MessageCollector messageCollector, @NotNull Predicate<CompilerMessageSeverity> decline) {
        this.messageCollector = messageCollector;
        this.decline = decline;
    }

    @Override
    public void clear() {
        messageCollector.clear();
    }

    @Override
    public void report(@NotNull CompilerMessageSeverity severity, @NotNull String message, @Nullable CompilerMessageSourceLocation location) {
        if (!decline.test(severity)) {
            messageCollector.report(severity, message, location);
        }
    }

    @Override
    public boolean hasErrors() {
        return messageCollector.hasErrors();
    }
}
