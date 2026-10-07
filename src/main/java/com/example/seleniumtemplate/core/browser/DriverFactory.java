package com.example.seleniumtemplate.core.browser;

import org.openqa.selenium.WebDriver;

@FunctionalInterface
public interface DriverFactory {

    WebDriver create(BrowserOptions options);
}
