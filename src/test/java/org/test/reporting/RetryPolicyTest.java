package org.test.reporting;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RetryPolicyTest {
    @Test
    void defaultsToThreeRetries() {
        assertEquals(3, RetryPolicy.resolve(null));
        assertEquals(3, RetryPolicy.resolve(" "));
    }

    @Test
    void honoursRequestedCountUpToTheMaximumOfFour() {
        assertEquals(1, RetryPolicy.resolve("1"));
        assertEquals(4, RetryPolicy.resolve("4"));
        assertEquals(4, RetryPolicy.resolve("10"));
    }

    @Test
    void zeroOrNegativeDisablesRetries() {
        assertEquals(0, RetryPolicy.resolve("0"));
        assertEquals(0, RetryPolicy.resolve("-2"));
    }

    @Test
    void rejectsNonNumericValues() {
        assertThrows(IllegalArgumentException.class, () -> RetryPolicy.resolve("many"));
    }
}
