package com.example.seleniumtemplate.demo.theinternet.assertions;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.seleniumtemplate.demo.theinternet.pages.MultipleWindowsPage;
import java.util.Objects;

public final class MultipleWindowsAssertions {

    private final MultipleWindowsPage page;

    public MultipleWindowsAssertions(MultipleWindowsPage page) {
        this.page = Objects.requireNonNull(page, "page must not be null");
    }

    public void headingIs(String expected) {
        Objects.requireNonNull(expected, "expected must not be null");
        String actual = page.heading();
        assertThat(actual)
                .as("multiple windows heading should be <%s> but was <%s>", expected, actual)
                .isEqualTo(expected);
    }
}
