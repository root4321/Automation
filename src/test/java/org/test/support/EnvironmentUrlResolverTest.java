package org.test.support;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnvironmentUrlResolverTest {
    @Test
    void resolvesUrlForTheActiveProfile() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("dev.ui.url", "https://dev.example.test")
                .withProperty("pre.ui.url", "https://pre.example.test");
        environment.setDefaultProfiles("dev");
        environment.setActiveProfiles("pre");

        assertEquals("https://pre.example.test", new EnvironmentUrlResolver(environment).urlFor("ui"));
    }

    @Test
    void prefersBaseUrlOverrideToProfileUrl() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("pre.ui.url", "https://pre.example.test")
                .withProperty("UI_BASE_URL", "https://override.example.test");
        environment.setActiveProfiles("pre");

        assertEquals("https://override.example.test", new EnvironmentUrlResolver(environment).urlFor("ui"));
    }

    @Test
    void reportsMissingUrlForTheSelectedProfile() {
        MockEnvironment environment = new MockEnvironment();
        environment.setDefaultProfiles("prod");

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> new EnvironmentUrlResolver(environment).urlFor("ui"));
        assertTrue(error.getMessage().contains("prod.ui.url"));
    }

    @Test
    void rejectsAmbiguousProfiles() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("dev", "pre");

        assertThrows(IllegalStateException.class, () -> new EnvironmentUrlResolver(environment).urlFor("ui"));
    }
}
