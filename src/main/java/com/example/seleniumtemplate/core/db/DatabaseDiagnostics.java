package com.example.seleniumtemplate.core.db;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Per-thread capture of the latest failed DB operation for failure diagnostics.
 * Does not depend on Allure; the reporting layer reads and clears this state.
 */
public final class DatabaseDiagnostics {

    private static final ThreadLocal<@Nullable Failure> LAST = new ThreadLocal<>();

    private DatabaseDiagnostics() {
    }

    public static void record(Failure failure) {
        LAST.set(Objects.requireNonNull(failure, "failure must not be null"));
    }

    public static @Nullable Failure lastFailure() {
        return LAST.get();
    }

    public static void clear() {
        LAST.remove();
    }

    public record Failure(
            String operation,
            String sql,
            @Nullable String sqlState,
            int errorCode,
            long elapsedMillis) {

        public Failure {
            Objects.requireNonNull(operation, "operation must not be null");
            Objects.requireNonNull(sql, "sql must not be null");
        }

        public String render() {
            return "operation=" + operation
                    + "\nsql=" + sql
                    + "\nSQLState=" + sqlState
                    + "\nerrorCode=" + errorCode
                    + "\nelapsedMillis=" + elapsedMillis;
        }
    }
}
