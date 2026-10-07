package com.example.seleniumtemplate.core.browser;

public record WindowSize(int width, int height) {

    public WindowSize {
        if (width <= 0) {
            throw new IllegalArgumentException("Window width must be positive, but was: " + width);
        }
        if (height <= 0) {
            throw new IllegalArgumentException("Window height must be positive, but was: " + height);
        }
    }
}
