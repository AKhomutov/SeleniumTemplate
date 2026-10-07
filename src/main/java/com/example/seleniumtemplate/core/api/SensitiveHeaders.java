package com.example.seleniumtemplate.core.api;

import java.util.Locale;
import java.util.Set;
import org.jspecify.annotations.Nullable;

final class SensitiveHeaders {

    private static final Set<String> NAMES = Set.of(
            "authorization",
            "cookie",
            "set-cookie",
            "x-api-key");

    private SensitiveHeaders() {
    }

    static boolean isSensitive(@Nullable String headerName) {
        if (headerName == null) {
            return false;
        }
        return NAMES.contains(headerName.toLowerCase(Locale.ROOT));
    }

    static String maskedValue(@Nullable String headerName, @Nullable String value) {
        if (isSensitive(headerName)) {
            return "***";
        }
        return value == null ? "" : value;
    }
}
