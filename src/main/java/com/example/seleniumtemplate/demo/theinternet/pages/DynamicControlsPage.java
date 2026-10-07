package com.example.seleniumtemplate.demo.theinternet.pages;

import com.example.seleniumtemplate.core.browser.BrowserSession;
import com.example.seleniumtemplate.core.ui.UiActions;
import com.example.seleniumtemplate.core.ui.UiWaits;
import com.example.seleniumtemplate.demo.theinternet.TheInternetSettings;
import java.net.URI;
import java.util.Objects;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.jspecify.annotations.Nullable;

public final class DynamicControlsPage {

    // After Add the checkbox markup changes (#checkbox becomes the input itself),
    // so scope to the form and select the checkbox input rather than #checkbox.
    private static final By CHECKBOX = By.cssSelector("#checkbox-example input[type='checkbox']");
    private static final By CHECKBOX_TOGGLE = By.cssSelector("#checkbox-example button");
    private static final By INPUT = By.cssSelector("#input-example input[type='text']");
    private static final By INPUT_TOGGLE = By.cssSelector("#input-example button");
    private static final By MESSAGE = By.id("message");

    private final BrowserSession session;
    private final UiActions ui;
    private final UiWaits waits;
    private final TheInternetSettings settings;

    public DynamicControlsPage(
            BrowserSession session,
            UiActions ui,
            UiWaits waits,
            TheInternetSettings settings) {
        this.session = Objects.requireNonNull(session, "session must not be null");
        this.ui = Objects.requireNonNull(ui, "ui must not be null");
        this.waits = Objects.requireNonNull(waits, "waits must not be null");
        this.settings = Objects.requireNonNull(settings, "settings must not be null");
    }

    public void open() {
        URI target = settings.baseUrl().resolve("dynamic_controls");
        session.driver().get(target.toString());
        waits.visible(CHECKBOX_TOGGLE);
    }

    public boolean isOpen() {
        return "/dynamic_controls".equals(currentPath()) && ui.count(CHECKBOX_TOGGLE) > 0;
    }

    public boolean isCheckboxPresent() {
        return ui.count(CHECKBOX) > 0;
    }

    public void removeCheckbox() {
        waits.visible(CHECKBOX);
        ui.click(CHECKBOX_TOGGLE);
        waits.absent(CHECKBOX);
        waits.visible(MESSAGE);
    }

    public void restoreCheckbox() {
        ui.click(CHECKBOX_TOGGLE);
        waits.visible(CHECKBOX);
        waits.visible(MESSAGE);
    }

    public void enableInput() {
        ui.click(INPUT_TOGGLE);
        waits.enabled(INPUT);
        waits.visible(MESSAGE);
    }

    public void disableInput() {
        ui.click(INPUT_TOGGLE);
        waits.disabled(INPUT);
        waits.visible(MESSAGE);
    }

    public boolean isInputEnabled() {
        try {
            return session.driver().findElement(INPUT).isEnabled();
        } catch (NoSuchElementException ex) {
            return false;
        }
    }

    public void typeIntoInput(String text) {
        Objects.requireNonNull(text, "text must not be null");
        ui.type(INPUT, text);
    }

    public String inputValue() {
        return ui.value(INPUT);
    }

    public String message() {
        return ui.text(MESSAGE).trim();
    }

    private String currentPath() {
        @Nullable String currentUrl = session.driver().getCurrentUrl();
        @Nullable String path = URI.create(Objects.requireNonNullElse(currentUrl, "")).getPath();
        return Objects.requireNonNullElse(path, "");
    }
}
