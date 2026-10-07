package com.example.seleniumtemplate.demo.theinternet;

import com.example.seleniumtemplate.demo.theinternet.pages.LoginPage;
import com.example.seleniumtemplate.demo.theinternet.pages.SecureAreaPage;
import java.util.Objects;

public final class AuthenticationHelper {

    private final LoginPage loginPage;
    private final SecureAreaPage secureAreaPage;

    AuthenticationHelper(LoginPage loginPage, SecureAreaPage secureAreaPage) {
        this.loginPage = Objects.requireNonNull(loginPage, "loginPage must not be null");
        this.secureAreaPage = Objects.requireNonNull(secureAreaPage, "secureAreaPage must not be null");
    }

    public void login(String username, String password) {
        loginPage.open();
        loginPage.waitUntilOpen();
        loginPage.login(username, password);
        loginPage.waitForAuthenticationResult();
    }

    public void logout() {
        secureAreaPage.waitUntilOpen();
        secureAreaPage.logout();
        loginPage.waitUntilOpen();
    }

    public boolean isLoggedIn() {
        return secureAreaPage.isOpen();
    }

    public String flashMessage() {
        if (secureAreaPage.isOpen()) {
            return secureAreaPage.flashMessage();
        }
        return loginPage.flashMessage();
    }
}
