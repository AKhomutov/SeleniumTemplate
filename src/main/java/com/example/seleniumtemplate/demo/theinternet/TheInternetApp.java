package com.example.seleniumtemplate.demo.theinternet;

import com.example.seleniumtemplate.core.ApplicationManager;
import com.example.seleniumtemplate.core.ui.AlertActions;
import com.example.seleniumtemplate.core.ui.WindowActions;
import com.example.seleniumtemplate.demo.theinternet.pages.DropdownPage;
import com.example.seleniumtemplate.demo.theinternet.pages.DynamicControlsPage;
import com.example.seleniumtemplate.demo.theinternet.pages.DynamicLoadingPage;
import com.example.seleniumtemplate.demo.theinternet.pages.JavaScriptAlertsPage;
import com.example.seleniumtemplate.demo.theinternet.pages.LoginPage;
import com.example.seleniumtemplate.demo.theinternet.pages.MultipleWindowsPage;
import com.example.seleniumtemplate.demo.theinternet.pages.SecureAreaPage;
import java.net.URI;
import java.util.Objects;

public final class TheInternetApp {

    private final AuthenticationHelper authenticationHelper;
    private final DynamicControlsPage dynamicControlsPage;
    private final DynamicLoadingPage dynamicLoadingPage;
    private final DropdownPage dropdownPage;
    private final JavaScriptAlertsPage javaScriptAlertsPage;
    private final MultipleWindowsPage multipleWindowsPage;
    private final AlertActions alertActions;
    private final WindowActions windowActions;

    public TheInternetApp(ApplicationManager applicationManager, URI baseUrl) {
        Objects.requireNonNull(applicationManager, "applicationManager must not be null");
        TheInternetSettings settings = new TheInternetSettings(baseUrl);

        LoginPage loginPage = new LoginPage(
                applicationManager.session(),
                applicationManager.ui(),
                applicationManager.waits(),
                settings);
        SecureAreaPage secureAreaPage = new SecureAreaPage(
                applicationManager.session(),
                applicationManager.ui(),
                applicationManager.waits());
        this.dynamicControlsPage = new DynamicControlsPage(
                applicationManager.session(),
                applicationManager.ui(),
                applicationManager.waits(),
                settings);
        this.dynamicLoadingPage = new DynamicLoadingPage(
                applicationManager.session(),
                applicationManager.ui(),
                applicationManager.waits(),
                settings);
        this.dropdownPage = new DropdownPage(
                applicationManager.session(),
                applicationManager.ui(),
                applicationManager.waits(),
                settings);
        this.javaScriptAlertsPage = new JavaScriptAlertsPage(
                applicationManager.session(),
                applicationManager.ui(),
                applicationManager.waits(),
                settings);
        this.multipleWindowsPage = new MultipleWindowsPage(
                applicationManager.session(),
                applicationManager.ui(),
                applicationManager.waits(),
                settings);
        this.alertActions = applicationManager.alerts();
        this.windowActions = applicationManager.windows();

        this.authenticationHelper = new AuthenticationHelper(loginPage, secureAreaPage);
    }

    public AuthenticationHelper auth() {
        return authenticationHelper;
    }

    public DynamicControlsPage dynamicControls() {
        return dynamicControlsPage;
    }

    public DynamicLoadingPage dynamicLoading() {
        return dynamicLoadingPage;
    }

    public DropdownPage dropdown() {
        return dropdownPage;
    }

    public JavaScriptAlertsPage javaScriptAlerts() {
        return javaScriptAlertsPage;
    }

    public MultipleWindowsPage multipleWindows() {
        return multipleWindowsPage;
    }

    public AlertActions alerts() {
        return alertActions;
    }

    public WindowActions windows() {
        return windowActions;
    }
}
