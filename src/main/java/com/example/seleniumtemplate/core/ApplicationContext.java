package com.example.seleniumtemplate.core;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

public final class ApplicationContext {

    private static final ThreadLocal<ApplicationManager> CURRENT = new ThreadLocal<>();

    private ApplicationContext() {
    }

    public static void bind(ApplicationManager applicationManager) {
        if (CURRENT.get() != null) {
            throw new IllegalStateException(
                    "ApplicationManager is already bound to the current test thread");
        }
        CURRENT.set(Objects.requireNonNull(
                applicationManager, "applicationManager must not be null"));
    }

    public static ApplicationManager current() {
        ApplicationManager applicationManager = CURRENT.get();
        if (applicationManager == null) {
            throw new IllegalStateException(
                    "No ApplicationManager is bound to the current test thread");
        }
        return applicationManager;
    }

    public static @Nullable ApplicationManager currentOrNull() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.remove();
    }
}
