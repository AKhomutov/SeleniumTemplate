package com.example.seleniumtemplate.support.reporting;

import com.example.seleniumtemplate.core.api.ApiExchangeFilter;
import com.example.seleniumtemplate.core.db.DatabaseDiagnostics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Collects available failure diagnostics without depending on TestBase lifecycle.
 * Individual attachment failures are logged and swallowed so the original test failure remains.
 */
public final class FailureDiagnostics {

    private static final Logger log = LoggerFactory.getLogger(FailureDiagnostics.class);

    private FailureDiagnostics() {
    }

    public static void attachAvailable() {
        attachSafely("UI", UiFailureAttachments::attachIfPresent);
        attachSafely("API", ApiFailureAttachments::attachIfPresent);
        attachSafely("DB", DbFailureAttachments::attachIfPresent);
    }

    public static void clearThreadState() {
        ApiExchangeFilter.clear();
        DatabaseDiagnostics.clear();
    }

    private static void attachSafely(String channel, Runnable action) {
        try {
            action.run();
        } catch (RuntimeException | Error ex) {
            log.warn("Failed to attach {} failure diagnostics: {}", channel, ex.toString());
        }
    }
}
