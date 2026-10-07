package com.example.seleniumtemplate.support;

import com.example.seleniumtemplate.core.ApplicationContext;
import com.example.seleniumtemplate.core.ApplicationManager;
import com.example.seleniumtemplate.core.config.TestConfig;
import com.example.seleniumtemplate.core.config.ConfigLoader;
import java.net.URI;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public abstract class TestBase {

    private static final TestConfig CONFIG = ConfigLoader.load();

    @BeforeMethod(alwaysRun = true)
    public void setUpApplication() {
        ApplicationManager applicationManager = ApplicationManager.start(
                CONFIG.framework().browser(),
                CONFIG.framework().waits());
        try {
            ApplicationContext.bind(applicationManager);
        } catch (RuntimeException ex) {
            try {
                applicationManager.stop();
            } catch (RuntimeException stopEx) {
                ex.addSuppressed(stopEx);
            }
            throw ex;
        }
    }

    @AfterMethod(alwaysRun = true)
    public void tearDownApplication() {
        ApplicationManager applicationManager = ApplicationContext.currentOrNull();
        try {
            if (applicationManager != null) {
                applicationManager.stop();
            }
        } finally {
            ApplicationContext.clear();
        }
    }

    protected static TestConfig config() {
        return CONFIG;
    }

    protected static URI webBaseUrl() {
        return CONFIG.environment().webBaseUrl();
    }
}
