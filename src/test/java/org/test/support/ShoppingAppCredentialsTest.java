package org.test.support;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShoppingAppCredentialsTest {
    @Test
    void selectsCredentialsForTheActiveProfile() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("email_dev", "dev-example")
                .withProperty("password_dev", "dev-example")
                .withProperty("email_pre", "pre-example")
                .withProperty("password_pre", "pre-example");
        environment.setActiveProfiles("pre");
        ShoppingAppCredentials credentials = new ShoppingAppCredentials(
                environment, new EnvironmentUrlResolver(environment));

        assertEquals("pre-example", credentials.email());
        assertEquals("pre-example", credentials.password());
    }

    @Test
    void allowsEnvironmentVariablesToOverrideTheFile() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("email_dev", "file-example")
                .withProperty("SHOP_EMAIL", "override-example");
        environment.setDefaultProfiles("dev");
        ShoppingAppCredentials credentials = new ShoppingAppCredentials(
                environment, new EnvironmentUrlResolver(environment));

        assertEquals("override-example", credentials.email());
    }

    @Test
    void reportsMissingCredentialsForTheSelectedEnvironment() {
        MockEnvironment environment = new MockEnvironment();
        environment.setDefaultProfiles("pre");
        ShoppingAppCredentials credentials = new ShoppingAppCredentials(
                environment, new EnvironmentUrlResolver(environment));

        IllegalStateException error = assertThrows(IllegalStateException.class, credentials::password);
        assertTrue(error.getMessage().contains("password_pre"));
    }
}
