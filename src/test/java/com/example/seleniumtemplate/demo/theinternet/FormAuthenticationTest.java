package com.example.seleniumtemplate.demo.theinternet;

import com.example.seleniumtemplate.core.ApplicationContext;
import com.example.seleniumtemplate.demo.theinternet.assertions.AuthenticationAssertions;
import com.example.seleniumtemplate.support.TestBase;
import org.testng.annotations.Test;

/**
 * External demo against the-internet.herokuapp.com.
 * Not part of the default deterministic suite; run via testng-external.xml.
 */
public class FormAuthenticationTest extends TestBase {

    private static final String VALID_USERNAME = "tomsmith";
    private static final String VALID_PASSWORD = "SuperSecretPassword!";

    @Test(groups = "external")
    public void successfulLoginShowsSecureAreaAndSuccessFlash() {
        TheInternetApp app = theInternet();
        AuthenticationAssertions verify = new AuthenticationAssertions(app.auth());

        app.auth().login(VALID_USERNAME, VALID_PASSWORD);

        verify.userIsLoggedIn();
        verify.flashContains("You logged into a secure area!");
    }

    @Test(groups = "external")
    public void invalidLoginKeepsUserLoggedOutAndShowsErrorFlash() {
        TheInternetApp app = theInternet();
        AuthenticationAssertions verify = new AuthenticationAssertions(app.auth());

        app.auth().login("invalid", "credentials");

        verify.userIsLoggedOut();
        verify.flashContains("invalid");
    }

    @Test(groups = "external")
    public void logoutReturnsToLoginAndShowsLogoutFlash() {
        TheInternetApp app = theInternet();
        AuthenticationAssertions verify = new AuthenticationAssertions(app.auth());

        app.auth().login(VALID_USERNAME, VALID_PASSWORD);
        app.auth().logout();

        verify.userIsLoggedOut();
        verify.flashContains("You logged out of the secure area!");
    }

    private static TheInternetApp theInternet() {
        return new TheInternetApp(ApplicationContext.current(), webBaseUrl());
    }
}
