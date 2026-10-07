package com.example.seleniumtemplate.demo.theinternet.pages;

import com.example.seleniumtemplate.core.browser.BrowserSession;
import com.example.seleniumtemplate.core.ui.UiActions;
import com.example.seleniumtemplate.core.ui.UiWaits;
import com.example.seleniumtemplate.demo.theinternet.TheInternetSettings;
import java.net.URI;
import java.util.Objects;
import org.openqa.selenium.By;
import org.jspecify.annotations.Nullable;

public final class MultipleWindowsPage {

    private static final By OPEN_NEW_WINDOW = By.cssSelector("a[href='/windows/new']");
    private static final By HEADING = By.cssSelector("div.example h3");

    private final BrowserSession session;
    private final UiActions ui;
    private final UiWaits waits;
    private final TheInternetSettings settings;

    public MultipleWindowsPage(
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
        URI target = settings.baseUrl().resolve("windows");
        session.driver().get(target.toString());
        waits.visible(OPEN_NEW_WINDOW);
    }

    public boolean isOpen() {
        return "/windows".equals(currentPath()) && ui.count(OPEN_NEW_WINDOW) > 0;
    }

    public void openNewWindow() {
        ui.click(OPEN_NEW_WINDOW);
    }

    public String heading() {
        return ui.text(HEADING);
    }

    private String currentPath() {
        @Nullable String currentUrl = session.driver().getCurrentUrl();
        @Nullable String path = URI.create(Objects.requireNonNullElse(currentUrl, "")).getPath();
        return Objects.requireNonNullElse(path, "");
    }
}
