package com.example.seleniumtemplate.demo.theinternet;

import java.net.URI;
import java.util.Objects;

public record TheInternetSettings(URI baseUrl) {

    public TheInternetSettings {
        Objects.requireNonNull(baseUrl, "baseUrl must not be null");
        if (!baseUrl.isAbsolute()) {
            throw new IllegalArgumentException(
                    "baseUrl must be an absolute URI, but was: " + baseUrl);
        }
    }
}
