package com.example.seleniumtemplate.support.reporting;

import com.example.seleniumtemplate.core.api.ApiExchangeFilter;
import io.qameta.allure.Allure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ApiFailureAttachments {

    private static final Logger log = LoggerFactory.getLogger(ApiFailureAttachments.class);

    private ApiFailureAttachments() {
    }

    static void attachIfPresent() {
        ApiExchangeFilter.Exchange exchange = ApiExchangeFilter.lastExchange();
        if (exchange == null) {
            return;
        }
        try {
            Allure.addAttachment("API exchange", "text/plain", exchange.render());
        } catch (RuntimeException ex) {
            log.warn("API exchange attachment skipped: {}", ex.toString());
        }
    }
}
