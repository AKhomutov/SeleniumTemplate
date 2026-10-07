package com.example.seleniumtemplate.demo.theinternet.assertions;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.seleniumtemplate.demo.theinternet.pages.DynamicControlsPage;
import java.util.Objects;

public final class DynamicControlsAssertions {

    private final DynamicControlsPage page;

    public DynamicControlsAssertions(DynamicControlsPage page) {
        this.page = Objects.requireNonNull(page, "page must not be null");
    }

    public void checkboxIsPresent() {
        assertThat(page.isCheckboxPresent())
                .as("checkbox should be present")
                .isTrue();
    }

    public void checkboxIsAbsent() {
        assertThat(page.isCheckboxPresent())
                .as("checkbox should be absent")
                .isFalse();
    }

    public void inputIsEnabled() {
        assertThat(page.isInputEnabled())
                .as("input should be enabled")
                .isTrue();
    }

    public void inputIsDisabled() {
        assertThat(page.isInputEnabled())
                .as("input should be disabled")
                .isFalse();
    }

    public void inputValueIs(String expected) {
        Objects.requireNonNull(expected, "expected must not be null");
        String actual = page.inputValue();
        assertThat(actual)
                .as("input value should be <%s> but was <%s>", expected, actual)
                .isEqualTo(expected);
    }

    public void messageContains(String expected) {
        Objects.requireNonNull(expected, "expected must not be null");
        String actual = page.message();
        assertThat(actual)
                .as("status message should contain <%s> but was <%s>", expected, actual)
                .contains(expected);
    }
}
