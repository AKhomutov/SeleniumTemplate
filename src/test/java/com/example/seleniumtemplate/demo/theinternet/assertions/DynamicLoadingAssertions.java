package com.example.seleniumtemplate.demo.theinternet.assertions;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.seleniumtemplate.demo.theinternet.pages.DynamicLoadingPage;
import java.util.Objects;

public final class DynamicLoadingAssertions {

    private final DynamicLoadingPage page;

    public DynamicLoadingAssertions(DynamicLoadingPage page) {
        this.page = Objects.requireNonNull(page, "page must not be null");
    }

    public void resultIsPresent() {
        assertThat(page.isResultPresent())
                .as("result should be present in the DOM")
                .isTrue();
    }

    public void resultIsAbsent() {
        assertThat(page.isResultPresent())
                .as("result should be absent from the DOM")
                .isFalse();
    }

    public void resultIsVisible() {
        assertThat(page.isResultVisible())
                .as("result should be visible")
                .isTrue();
    }

    public void resultIsHidden() {
        assertThat(page.isResultVisible())
                .as("result should be hidden")
                .isFalse();
    }

    public void resultTextIs(String expected) {
        Objects.requireNonNull(expected, "expected must not be null");
        String actual = page.resultText();
        assertThat(actual)
                .as("result text should be <%s> but was <%s>", expected, actual)
                .isEqualTo(expected);
    }
}
