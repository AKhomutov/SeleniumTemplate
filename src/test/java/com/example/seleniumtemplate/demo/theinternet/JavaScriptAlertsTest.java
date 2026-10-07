package com.example.seleniumtemplate.demo.theinternet;

import com.example.seleniumtemplate.core.ApplicationContext;
import com.example.seleniumtemplate.core.ui.AlertActions;
import com.example.seleniumtemplate.demo.theinternet.assertions.AlertAssertions;
import com.example.seleniumtemplate.demo.theinternet.assertions.JavaScriptAlertsAssertions;
import com.example.seleniumtemplate.demo.theinternet.pages.JavaScriptAlertsPage;
import com.example.seleniumtemplate.support.TestBase;
import org.testng.annotations.Test;

/**
 * External demo against the-internet.herokuapp.com.
 * Not part of the default deterministic suite; run via testng-external.xml.
 */
public class JavaScriptAlertsTest extends TestBase {

    @Test(groups = "external")
    public void acceptingJsAlertUpdatesResult() {
        TheInternetApp app = theInternet();
        JavaScriptAlertsPage page = app.javaScriptAlerts();
        AlertActions alerts = app.alerts();
        AlertAssertions verifyAlert = new AlertAssertions(alerts);
        JavaScriptAlertsAssertions verifyPage = new JavaScriptAlertsAssertions(page);

        page.open();
        page.triggerAlert();

        alerts.waitUntilPresent();
        verifyAlert.textIs("I am a JS Alert");

        alerts.accept();

        verifyPage.resultIs("You successfully clicked an alert");
    }

    @Test(groups = "external")
    public void dismissingJsConfirmUpdatesResult() {
        TheInternetApp app = theInternet();
        JavaScriptAlertsPage page = app.javaScriptAlerts();
        AlertActions alerts = app.alerts();
        AlertAssertions verifyAlert = new AlertAssertions(alerts);
        JavaScriptAlertsAssertions verifyPage = new JavaScriptAlertsAssertions(page);

        page.open();
        page.triggerConfirm();

        alerts.waitUntilPresent();
        verifyAlert.textIs("I am a JS Confirm");

        alerts.dismiss();

        verifyPage.resultIs("You clicked: Cancel");
    }

    @Test(groups = "external")
    public void typingIntoJsPromptUpdatesResult() {
        TheInternetApp app = theInternet();
        JavaScriptAlertsPage page = app.javaScriptAlerts();
        AlertActions alerts = app.alerts();
        AlertAssertions verifyAlert = new AlertAssertions(alerts);
        JavaScriptAlertsAssertions verifyPage = new JavaScriptAlertsAssertions(page);

        page.open();
        page.triggerPrompt();

        alerts.waitUntilPresent();
        verifyAlert.textIs("I am a JS prompt");

        alerts.type("Selenium");
        alerts.accept();

        verifyPage.resultContains("Selenium");
    }

    private static TheInternetApp theInternet() {
        return new TheInternetApp(ApplicationContext.current(), webBaseUrl());
    }
}
