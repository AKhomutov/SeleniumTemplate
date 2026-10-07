package com.example.seleniumtemplate;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.seleniumtemplate.core.ApplicationContext;
import com.example.seleniumtemplate.core.ApplicationManager;
import com.example.seleniumtemplate.core.browser.ExecutionMode;
import com.example.seleniumtemplate.support.TestBase;
import com.example.seleniumtemplate.ui.support.LocalHtmlFixture;
import java.net.URI;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Deterministic browser smoke for LOCAL and REMOTE profiles.
 * Not part of the default {@code ./gradlew test} suite — run via {@code testRemoteSmoke}.
 */
public class RemoteBrowserSmokeTest extends TestBase {

    private @Nullable LocalHtmlFixture htmlFixture;

    @BeforeClass
    public void startHtmlFixture() {
        htmlFixture = LocalHtmlFixture.start();
    }

    @AfterClass(alwaysRun = true)
    public void stopHtmlFixture() {
        LocalHtmlFixture fixture = htmlFixture;
        if (fixture != null) {
            fixture.close();
        }
    }

    @Test
    public void browserOpensFixturePageAndReadsTitle() {
        LocalHtmlFixture fixture = Objects.requireNonNull(htmlFixture, "htmlFixture");
        boolean remote = config().framework().browser().executionMode() == ExecutionMode.REMOTE;
        URI target = LocalHtmlFixture.browserReachablePageUri(fixture.hostLocalUri(), remote);

        ApplicationManager app = ApplicationContext.current();
        app.driver().get(target.toString());

        assertThat(app.driver().getTitle()).isEqualTo("Remote Smoke");
        assertThat(app.driver().findElement(By.id("marker")).getText()).isEqualTo("remote-smoke-ok");

        if (remote) {
            assertThat(app.driver()).isInstanceOf(RemoteWebDriver.class);
            assertThat(config().framework().browser().remoteUrl()).isNotNull();
        }
    }
}
