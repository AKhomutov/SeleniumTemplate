package com.example.seleniumtemplate.support.reporting;

import com.example.seleniumtemplate.core.db.DatabaseDiagnostics;
import io.qameta.allure.Allure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class DbFailureAttachments {

    private static final Logger log = LoggerFactory.getLogger(DbFailureAttachments.class);

    private DbFailureAttachments() {
    }

    static void attachIfPresent() {
        DatabaseDiagnostics.Failure failure = DatabaseDiagnostics.lastFailure();
        if (failure == null) {
            return;
        }
        try {
            Allure.addAttachment("DB failure", "text/plain", failure.render());
        } catch (RuntimeException ex) {
            log.warn("DB failure attachment skipped: {}", ex.toString());
        }
    }
}
