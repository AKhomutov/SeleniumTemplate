package com.example.seleniumtemplate.demo.theinternet;

import com.example.seleniumtemplate.core.ApplicationContext;
import com.example.seleniumtemplate.demo.theinternet.assertions.DynamicLoadingAssertions;
import com.example.seleniumtemplate.demo.theinternet.pages.DynamicLoadingPage;
import com.example.seleniumtemplate.support.TestBase;
import org.testng.annotations.Test;

/**
 * External demo against the-internet.herokuapp.com.
 * Not part of the default deterministic suite; run via testng-external.xml.
 */
public class DynamicLoadingTest extends TestBase {

    @Test(groups = "external")
    public void hiddenElementIsPresentBeforeStartAndBecomesVisibleAfterLoading() {
        TheInternetApp app = theInternet();
        DynamicLoadingPage page = app.dynamicLoading();
        DynamicLoadingAssertions verify = new DynamicLoadingAssertions(page);

        page.openHiddenElementExample();

        verify.resultIsPresent();
        verify.resultIsHidden();

        page.startLoading();
        page.waitUntilResultVisible();

        verify.resultIsPresent();
        verify.resultIsVisible();
        verify.resultTextIs("Hello World!");
    }

    @Test(groups = "external")
    public void deferredElementIsAbsentUntilLoadingThenPresentAndVisible() {
        TheInternetApp app = theInternet();
        DynamicLoadingPage page = app.dynamicLoading();
        DynamicLoadingAssertions verify = new DynamicLoadingAssertions(page);

        page.openDeferredElementExample();

        verify.resultIsAbsent();

        page.startLoading();

        page.waitUntilResultPresent();
        page.waitUntilResultVisible();

        verify.resultIsPresent();
        verify.resultIsVisible();
        verify.resultTextIs("Hello World!");
    }

    private static TheInternetApp theInternet() {
        return new TheInternetApp(ApplicationContext.current(), webBaseUrl());
    }
}
