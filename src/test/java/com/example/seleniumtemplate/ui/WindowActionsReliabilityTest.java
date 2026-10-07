package com.example.seleniumtemplate.ui;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.seleniumtemplate.core.ApplicationContext;
import com.example.seleniumtemplate.core.ApplicationManager;
import com.example.seleniumtemplate.core.ui.WindowActions;
import com.example.seleniumtemplate.support.TestBase;
import java.net.URL;
import java.util.Objects;
import java.util.Set;
import org.openqa.selenium.By;
import org.testng.annotations.Test;

public class WindowActionsReliabilityTest extends TestBase {

    private static final By HEADING = By.id("heading");
    private static final By OPEN_CHILD = By.id("open-child");

    @Test
    public void openChildSwitchesAndReturnsToParent() {
        ApplicationManager app = ApplicationContext.current();
        openFixture("fixtures/windows.html");
        WindowActions windows = app.windows();

        String parent = windows.currentHandle();
        Set<String> before = windows.handles();
        assertThat(windows.count()).isEqualTo(1);
        assertThat(app.driver().getTitle()).isEqualTo("Parent Window");
        assertThat(app.ui().text(HEADING)).isEqualTo("Parent Window");

        app.ui().click(OPEN_CHILD);
        windows.waitForCount(2);
        windows.switchToNewWindow(before);

        assertThat(windows.currentHandle()).isNotEqualTo(parent);
        assertThat(app.driver().getTitle()).isEqualTo("Child Window");
        assertThat(app.ui().text(HEADING)).isEqualTo("Child Window");

        windows.switchTo(parent);

        assertThat(windows.currentHandle()).isEqualTo(parent);
        assertThat(app.driver().getTitle()).isEqualTo("Parent Window");
        assertThat(app.ui().text(HEADING)).isEqualTo("Parent Window");
    }

    @Test
    public void closeChildLeavesParentUsable() {
        ApplicationManager app = ApplicationContext.current();
        openFixture("fixtures/windows.html");
        WindowActions windows = app.windows();

        String parent = windows.currentHandle();
        Set<String> before = windows.handles();

        app.ui().click(OPEN_CHILD);
        windows.switchToNewWindow(before);
        windows.closeCurrent();
        windows.switchTo(parent);

        assertThat(windows.count()).isEqualTo(1);
        assertThat(windows.currentHandle()).isEqualTo(parent);
        assertThat(app.driver().getTitle()).isEqualTo("Parent Window");
        assertThat(app.ui().text(HEADING)).isEqualTo("Parent Window");
    }

    @Test
    public void waitForCountSeesIncreaseFromOneToTwo() {
        ApplicationManager app = ApplicationContext.current();
        openFixture("fixtures/windows.html");
        WindowActions windows = app.windows();

        assertThat(windows.count()).isEqualTo(1);

        app.ui().click(OPEN_CHILD);
        windows.waitForCount(2);

        assertThat(windows.count()).isEqualTo(2);
    }

    private void openFixture(String resourcePath) {
        URL resource = Objects.requireNonNull(
                getClass().getClassLoader().getResource(resourcePath),
                "Missing classpath fixture: " + resourcePath);
        ApplicationContext.current().driver().get(resource.toString());
    }
}
