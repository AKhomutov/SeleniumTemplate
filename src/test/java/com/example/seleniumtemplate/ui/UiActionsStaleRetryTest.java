package com.example.seleniumtemplate.ui;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.seleniumtemplate.core.browser.BrowserSession;
import com.example.seleniumtemplate.core.ui.UiActions;
import com.example.seleniumtemplate.core.ui.WaitPolicy;
import com.example.seleniumtemplate.ui.support.RecordingWebDriver;
import java.time.Duration;
import java.util.List;
import org.openqa.selenium.By;
import org.testng.annotations.Test;

public class UiActionsStaleRetryTest {

    private static final By ANY = By.id("target");
    private static final WaitPolicy FAST_POLICY =
            new WaitPolicy(Duration.ofSeconds(2), Duration.ofMillis(20));

    @Test
    public void clickRetriesAfterStaleAndClicksFreshElement() {
        RecordingWebDriver driver = new RecordingWebDriver();
        UiActions ui = new UiActions(new BrowserSession(driver), FAST_POLICY);

        ui.click(ANY);

        assertThat(driver.findElementCalls()).isGreaterThanOrEqualTo(2);
        assertThat(driver.clickCount()).isEqualTo(1);
    }

    @Test
    public void typeRetriesWholeClearAndSendKeysAfterStaleWithoutDuplication() {
        RecordingWebDriver driver = new RecordingWebDriver();
        UiActions ui = new UiActions(new BrowserSession(driver), FAST_POLICY);

        ui.type(ANY, "exact-value");

        assertThat(driver.findElementCalls()).isGreaterThanOrEqualTo(2);
        assertThat(driver.typedValue()).isEqualTo("exact-value");
        assertThat(ui.value(ANY)).isEqualTo("exact-value");
    }

    @Test
    public void textsReacquiresWholeCollectionAfterStaleOnOneElement() {
        RecordingWebDriver driver = new RecordingWebDriver();
        UiActions ui = new UiActions(new BrowserSession(driver), FAST_POLICY);

        List<String> texts = ui.texts(ANY);

        assertThat(driver.findElementsCalls()).isGreaterThanOrEqualTo(2);
        assertThat(texts).containsExactly("alpha", "beta");
    }
}
