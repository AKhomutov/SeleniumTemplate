package com.example.seleniumtemplate.ui;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.seleniumtemplate.core.ApplicationContext;
import com.example.seleniumtemplate.core.ApplicationManager;
import com.example.seleniumtemplate.support.TestBase;
import java.net.URL;
import java.util.Objects;
import org.openqa.selenium.By;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

public class UiBrowserReliabilityTest extends TestBase {

    @Test
    public void clickByIndexWaitsUntilCollectionGrows() {
        ApplicationManager app = ApplicationContext.current();
        openFixture("fixtures/indexed-click.html");

        app.ui().click(By.cssSelector("#list .item"), 2);

        assertThat(app.ui().text(By.id("result"))).isEqualTo("clicked-2");
        assertThat(app.ui().count(By.cssSelector("#list .item"))).isEqualTo(3);
    }

    @Test
    public void steadyWaitsUntilElementGeometryStopsChanging() {
        ApplicationManager app = ApplicationContext.current();
        openFixture("fixtures/steady-element.html");

        WebElement steady = app.waits().steady(By.id("box"));
        Rectangle first = steady.getRect();
        Rectangle second = app.waits().steady(By.id("box")).getRect();

        assertThat(app.ui().text(By.id("status"))).isEqualTo("stopped");
        assertThat(first.getX()).isEqualTo(second.getX());
        assertThat(first.getY()).isEqualTo(second.getY());
        assertThat(first.getWidth()).isEqualTo(second.getWidth());
        assertThat(first.getHeight()).isEqualTo(second.getHeight());
    }

    @Test
    public void enabledWaitsUntilElementBecomesEnabled() {
        ApplicationManager app = ApplicationContext.current();
        openFixture("fixtures/enabled-disabled.html");

        WebElement enabled = app.waits().enabled(By.id("becomes-enabled"));

        assertThat(enabled.isEnabled()).isTrue();
    }

    @Test
    public void disabledWaitsUntilElementBecomesDisabled() {
        ApplicationManager app = ApplicationContext.current();
        openFixture("fixtures/enabled-disabled.html");

        WebElement disabled = app.waits().disabled(By.id("becomes-disabled"));

        assertThat(disabled.isEnabled()).isFalse();
    }

    @Test
    public void presentFindsHiddenElementThenVisibleWaitsUntilShown() {
        ApplicationManager app = ApplicationContext.current();
        openFixture("fixtures/present-vs-visible.html");

        By finish = By.id("hidden-finish");

        assertThat(app.ui().count(finish)).isEqualTo(1);
        WebElement presentBeforeStart = app.waits().present(finish);
        assertThat(presentBeforeStart.isDisplayed()).isFalse();

        app.ui().click(By.id("hidden-start"));

        WebElement visible = app.waits().visible(finish);
        assertThat(visible.isDisplayed()).isTrue();
        assertThat(app.ui().text(finish).trim()).isEqualTo("Hello World!");
    }

    @Test
    public void deferredElementIsAbsentUntilPresentAndVisibleWaitsComplete() {
        ApplicationManager app = ApplicationContext.current();
        openFixture("fixtures/present-vs-visible.html");

        By finish = By.id("deferred-finish");

        assertThat(app.ui().count(finish)).isEqualTo(0);

        app.ui().click(By.id("deferred-start"));

        app.waits().present(finish);
        WebElement visible = app.waits().visible(finish);

        assertThat(app.ui().count(finish)).isEqualTo(1);
        assertThat(visible.isDisplayed()).isTrue();
        assertThat(app.ui().text(finish).trim()).isEqualTo("Hello World!");
    }

    @Test
    public void selectByTextAndIndexUpdateSelectedText() {
        ApplicationManager app = ApplicationContext.current();
        openFixture("fixtures/dropdown.html");

        By dropdown = By.id("dropdown");

        assertThat(app.ui().selectedText(dropdown)).isEqualTo("Please select an option");

        app.ui().selectByText(dropdown, "Option 1");
        assertThat(app.ui().selectedText(dropdown)).isEqualTo("Option 1");

        app.ui().selectByIndex(dropdown, 2);
        assertThat(app.ui().selectedText(dropdown)).isEqualTo("Option 2");
    }

    private void openFixture(String resourcePath) {
        URL resource = Objects.requireNonNull(
                getClass().getClassLoader().getResource(resourcePath),
                "Missing classpath fixture: " + resourcePath);
        ApplicationContext.current().driver().get(resource.toString());
    }
}
