package com.example.seleniumtemplate.ui;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.seleniumtemplate.core.ApplicationManager;
import com.example.seleniumtemplate.core.browser.BrowserOptions;
import com.example.seleniumtemplate.ui.support.RecordingWebDriver;
import org.testng.annotations.Test;

public class ApplicationManagerUiWiringTest {

    @Test
    public void createsUiActionsAndUiWaitsBoundToOwnBrowserSession() {
        RecordingWebDriver driver = new RecordingWebDriver();

        ApplicationManager app = ApplicationManager.start(
                BrowserOptions.localChrome(),
                _ -> driver);

        try {
            assertThat(app.ui()).isNotNull();
            assertThat(app.waits()).isNotNull();
            assertThat(app.alerts()).isNotNull();
            assertThat(app.windows()).isNotNull();
            assertThat(app.driver()).isSameAs(driver);
            assertThat(app.session().driver()).isSameAs(driver);
        } finally {
            app.stop();
        }
    }
}
