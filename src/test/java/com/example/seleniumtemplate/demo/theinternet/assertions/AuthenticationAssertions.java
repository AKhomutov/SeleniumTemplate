package com.example.seleniumtemplate.demo.theinternet.assertions;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.seleniumtemplate.demo.theinternet.AuthenticationHelper;
import java.util.Objects;

public final class AuthenticationAssertions {

    private final AuthenticationHelper auth;

    public AuthenticationAssertions(AuthenticationHelper auth) {
        this.auth = Objects.requireNonNull(auth, "auth must not be null");
    }

    public void userIsLoggedIn() {
        boolean loggedIn = auth.isLoggedIn();
        assertThat(loggedIn)
                .withFailMessage("user should be logged in")
                .isTrue();
    }

    public void userIsLoggedOut() {
        boolean loggedIn = auth.isLoggedIn();
        assertThat(loggedIn)
                .withFailMessage("user should be logged out")
                .isFalse();
    }

    public void flashContains(String expected) {
        Objects.requireNonNull(expected, "expected must not be null");
        String flashMessage = auth.flashMessage();
        assertThat(flashMessage)
                .withFailMessage("authentication flash message should contain <%s> but was <%s>",
                        expected, flashMessage)
                .contains(expected);
    }
}
