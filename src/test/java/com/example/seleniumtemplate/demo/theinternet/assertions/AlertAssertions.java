package com.example.seleniumtemplate.demo.theinternet.assertions;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.seleniumtemplate.core.ui.AlertActions;
import java.util.Objects;

public final class AlertAssertions {

    private final AlertActions alerts;

    public AlertAssertions(AlertActions alerts) {
        this.alerts = Objects.requireNonNull(alerts, "alerts must not be null");
    }

    public void textIs(String expected) {
        Objects.requireNonNull(expected, "expected must not be null");
        String actual = alerts.text();
        assertThat(actual)
                .as("browser alert text should be <%s> but was <%s>", expected, actual)
                .isEqualTo(expected);
    }

    public void isPresent() {
        assertThat(alerts.isPresent())
                .as("browser alert should be present")
                .isTrue();
    }

    public void isAbsent() {
        assertThat(alerts.isPresent())
                .as("browser alert should be absent")
                .isFalse();
    }
}
