package org.test.reporting;

/** Number of times failed scenarios are re-executed: default 3, configurable with -Dretry.count, capped at 4. */
public final class RetryPolicy {
    public static final String PROPERTY = "retry.count";
    public static final int DEFAULT_RETRIES = 3;
    public static final int MAX_RETRIES = 4;

    private RetryPolicy() {
    }

    public static int fromSystemProperty() {
        return resolve(System.getProperty(PROPERTY));
    }

    static int resolve(String value) {
        if (value == null || value.isBlank()) {
            return DEFAULT_RETRIES;
        }
        int requested;
        try {
            requested = Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("-D" + PROPERTY + " must be a whole number, but was '" + value + "'", e);
        }
        return Math.max(0, Math.min(requested, MAX_RETRIES));
    }
}
