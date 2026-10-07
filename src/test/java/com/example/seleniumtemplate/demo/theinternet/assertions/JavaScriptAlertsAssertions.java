package com.example.seleniumtemplate.demo.theinternet.assertions;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.seleniumtemplate.demo.theinternet.pages.JavaScriptAlertsPage;
import java.util.Objects;

public final class JavaScriptAlertsAssertions {

    private final JavaScriptAlertsPage page;

    public JavaScriptAlertsAssertions(JavaScriptAlertsPage page) {
        this.page = Objects.requireNonNull(page, "page must not be null");
    }

    public void resultIs(String expected) {
        Objects.requireNonNull(expected, "expected must not be null");
        String actual = page.result();
        assertThat(actual)
                .as("javascript alerts result should be <%s> but was <%s>", expected, actual)
                .isEqualTo(expected);
    }

    public void resultContains(String expected) {
        Objects.requireNonNull(expected, "expected must not be null");
        String actual = page.result();
        assertThat(actual)
                .as("javascript alerts result should contain <%s> but was <%s>", expected, actual)
                .contains(expected);
    }
}
