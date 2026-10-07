package com.example.seleniumtemplate.ui;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.seleniumtemplate.core.ApplicationContext;
import com.example.seleniumtemplate.core.ApplicationManager;
import com.example.seleniumtemplate.core.ui.AlertActions;
import com.example.seleniumtemplate.support.TestBase;
import java.net.URL;
import java.util.Objects;
import org.openqa.selenium.By;
import org.testng.annotations.Test;

public class AlertActionsReliabilityTest extends TestBase {

    @Test
    public void alertPresentTextAndAcceptUpdatesResult() {
        ApplicationManager app = ApplicationContext.current();
        openFixture("fixtures/alerts.html");
        AlertActions alerts = app.alerts();

        assertThat(alerts.isPresent()).isFalse();

        app.ui().click(By.id("alert-btn"));
        alerts.waitUntilPresent();

        assertThat(alerts.isPresent()).isTrue();
        assertThat(alerts.text()).isEqualTo("I am a JS Alert");

        alerts.accept();

        assertThat(alerts.isPresent()).isFalse();
        assertThat(app.ui().text(By.id("result")))
                .isEqualTo("You successfully clicked an alert");
    }

    @Test
    public void confirmDismissUpdatesResult() {
        ApplicationManager app = ApplicationContext.current();
        openFixture("fixtures/alerts.html");
        AlertActions alerts = app.alerts();

        app.ui().click(By.id("confirm-btn"));
        alerts.waitUntilPresent();

        assertThat(alerts.text()).isEqualTo("I am a JS Confirm");
        alerts.dismiss();

        assertThat(alerts.isPresent()).isFalse();
        assertThat(app.ui().text(By.id("result"))).isEqualTo("You clicked: Cancel");
    }

    @Test
    public void promptTypeAndAcceptUpdatesResult() {
        ApplicationManager app = ApplicationContext.current();
        openFixture("fixtures/alerts.html");
        AlertActions alerts = app.alerts();

        app.ui().click(By.id("prompt-btn"));
        alerts.waitUntilPresent();

        assertThat(alerts.text()).isEqualTo("I am a JS prompt");
        alerts.type("Selenium");
        alerts.accept();

        assertThat(alerts.isPresent()).isFalse();
        assertThat(app.ui().text(By.id("result"))).isEqualTo("You entered: Selenium");
    }

    @Test
    public void isPresentIsFalseWhenNoAlert() {
        ApplicationManager app = ApplicationContext.current();
        openFixture("fixtures/alerts.html");

        assertThat(app.alerts().isPresent()).isFalse();
    }

    private void openFixture(String resourcePath) {
        URL resource = Objects.requireNonNull(
                getClass().getClassLoader().getResource(resourcePath),
                "Missing classpath fixture: " + resourcePath);
        ApplicationContext.current().driver().get(resource.toString());
    }
}
