package com.example.seleniumtemplate.demo.theinternet.assertions;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.seleniumtemplate.demo.theinternet.pages.DropdownPage;
import java.util.Objects;

public final class DropdownAssertions {

    private final DropdownPage page;

    public DropdownAssertions(DropdownPage page) {
        this.page = Objects.requireNonNull(page, "page must not be null");
    }

    public void selectedOptionIs(String expected) {
        Objects.requireNonNull(expected, "expected must not be null");
        String actual = page.selectedText();
        assertThat(actual)
                .as("selected dropdown option should be <%s> but was <%s>", expected, actual)
                .isEqualTo(expected);
    }
}
