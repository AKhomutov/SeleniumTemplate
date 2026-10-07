package com.example.seleniumtemplate.demo.theinternet;

import com.example.seleniumtemplate.core.ApplicationContext;
import com.example.seleniumtemplate.demo.theinternet.assertions.DynamicControlsAssertions;
import com.example.seleniumtemplate.demo.theinternet.pages.DynamicControlsPage;
import com.example.seleniumtemplate.support.TestBase;
import org.testng.annotations.Test;

/**
 * External demo against the-internet.herokuapp.com.
 * Not part of the default deterministic suite; run via testng-external.xml.
 */
public class DynamicControlsTest extends TestBase {

    @Test(groups = "external")
    public void removingCheckboxMakesItAbsentAndShowsGoneMessage() {
        TheInternetApp app = theInternet();
        DynamicControlsPage page = app.dynamicControls();
        DynamicControlsAssertions verify = new DynamicControlsAssertions(page);

        page.open();

        verify.checkboxIsPresent();

        page.removeCheckbox();

        verify.checkboxIsAbsent();
        verify.messageContains("It's gone!");
    }

    @Test(groups = "external")
    public void restoringCheckboxMakesItPresentAndShowsBackMessage() {
        TheInternetApp app = theInternet();
        DynamicControlsPage page = app.dynamicControls();
        DynamicControlsAssertions verify = new DynamicControlsAssertions(page);

        page.open();

        page.removeCheckbox();
        page.restoreCheckbox();

        verify.checkboxIsPresent();
        verify.messageContains("It's back!");
    }

    @Test(groups = "external")
    public void enablingInputAllowsTypingThenDisablingLocksItAgain() {
        TheInternetApp app = theInternet();
        DynamicControlsPage page = app.dynamicControls();
        DynamicControlsAssertions verify = new DynamicControlsAssertions(page);

        page.open();

        verify.inputIsDisabled();

        page.enableInput();

        verify.inputIsEnabled();

        page.typeIntoInput("Selenium");
        verify.inputValueIs("Selenium");

        page.disableInput();

        verify.inputIsDisabled();
        verify.messageContains("It's disabled!");
    }

    private static TheInternetApp theInternet() {
        return new TheInternetApp(ApplicationContext.current(), webBaseUrl());
    }
}
