package com.example.seleniumtemplate;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.seleniumtemplate.core.ApplicationContext;
import com.example.seleniumtemplate.core.ApplicationManager;
import com.example.seleniumtemplate.support.TestBase;
import org.testng.annotations.Test;

public class BrowserSmokeTest extends TestBase {

    @Test
    public void startsBrowserNavigatesAndExposesDriverThroughApplicationManager() {
        ApplicationManager app = ApplicationContext.current();

        app.driver().get("data:text/html,<html><head><title>Smoke</title></head><body>ok</body></html>");

        assertThat(app.driver().getTitle()).isEqualTo("Smoke");
        assertThat(app.session().driver()).isSameAs(app.driver());
    }
}
