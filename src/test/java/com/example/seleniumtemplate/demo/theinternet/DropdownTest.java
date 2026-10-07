package com.example.seleniumtemplate.demo.theinternet;

import com.example.seleniumtemplate.core.ApplicationContext;
import com.example.seleniumtemplate.demo.theinternet.assertions.DropdownAssertions;
import com.example.seleniumtemplate.demo.theinternet.pages.DropdownPage;
import com.example.seleniumtemplate.support.TestBase;
import org.testng.annotations.Test;

/**
 * External demo against the-internet.herokuapp.com.
 * Not part of the default deterministic suite; run via testng-external.xml.
 */
public class DropdownTest extends TestBase {

    @Test(groups = "external")
    public void selectingByVisibleTextChoosesOption1() {
        TheInternetApp app = theInternet();
        DropdownPage page = app.dropdown();
        DropdownAssertions verify = new DropdownAssertions(page);

        page.open();

        page.selectByText("Option 1");

        verify.selectedOptionIs("Option 1");
    }

    @Test(groups = "external")
    public void selectingByIndexChoosesOption2() {
        TheInternetApp app = theInternet();
        DropdownPage page = app.dropdown();
        DropdownAssertions verify = new DropdownAssertions(page);

        page.open();

        // DOM: 0=placeholder (disabled), 1=Option 1, 2=Option 2
        page.selectByIndex(2);

        verify.selectedOptionIs("Option 2");
    }

    private static TheInternetApp theInternet() {
        return new TheInternetApp(ApplicationContext.current(), webBaseUrl());
    }
}
