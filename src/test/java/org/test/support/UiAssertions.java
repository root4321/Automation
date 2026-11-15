package org.test.support;

import org.junit.Assert;

import java.util.Objects;

/**
 * Assertions for UI steps that take a screenshot at the moment an assertion fails,
 * before the error propagates and the page can change.
 */
public final class UiAssertions {
    private UiAssertions() {
    }

    public static void assertTrue(String message, boolean condition) {
        verify(message, () -> Assert.assertTrue(message, condition));
    }

    public static void assertEquals(String message, Object expected, Object actual) {
        verify(message, () -> Assert.assertEquals(message, expected, actual));
    }

    public static void verify(String description, Runnable assertion) {
        Objects.requireNonNull(assertion);
        try {
            assertion.run();
        } catch (AssertionError e) {
            ScreenshotService.capture("Assertion failed - " + description);
            ScenarioContext.current().ifPresent(context -> context.assertionScreenshotTakenInStep(true));
            throw e;
        }
    }
}
