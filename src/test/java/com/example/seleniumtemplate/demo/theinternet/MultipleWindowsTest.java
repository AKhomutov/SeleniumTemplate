package com.example.seleniumtemplate.demo.theinternet;

import com.example.seleniumtemplate.core.ApplicationContext;
import com.example.seleniumtemplate.core.ui.WindowActions;
import com.example.seleniumtemplate.demo.theinternet.assertions.MultipleWindowsAssertions;
import com.example.seleniumtemplate.demo.theinternet.pages.MultipleWindowsPage;
import com.example.seleniumtemplate.support.TestBase;
import java.util.Set;
import org.testng.annotations.Test;

/**
 * External demo against the-internet.herokuapp.com.
 * Not part of the default deterministic suite; run via testng-external.xml.
 */
public class MultipleWindowsTest extends TestBase {

    @Test(groups = "external")
    public void openingNewWindowSwitchesAndReturnsToParent() {
        TheInternetApp app = theInternet();
        MultipleWindowsPage page = app.multipleWindows();
        WindowActions windows = app.windows();
        MultipleWindowsAssertions verify = new MultipleWindowsAssertions(page);

        page.open();
        verify.headingIs("Opening a new window");

        String parent = windows.currentHandle();
        Set<String> before = windows.handles();

        page.openNewWindow();
        windows.switchToNewWindow(before);

        verify.headingIs("New Window");

        windows.closeCurrent();
        windows.switchTo(parent);

        verify.headingIs("Opening a new window");
    }

    private static TheInternetApp theInternet() {
        return new TheInternetApp(ApplicationContext.current(), webBaseUrl());
    }
}
