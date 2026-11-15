package org.test.support;

import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@PropertySource(value = "classpath:LoginData/ShoppingApp.properties", ignoreResourceNotFound = true)
public class ShoppingAppCredentials {
    private final Environment environment;
    private final EnvironmentUrlResolver environments;

    public ShoppingAppCredentials(Environment environment, EnvironmentUrlResolver environments) {
        this.environment = environment;
        this.environments = environments;
    }

    public String email() {
        return requiredCredential("email", "SHOP_EMAIL");
    }

    public String password() {
        return requiredCredential("password", "SHOP_PASSWORD");
    }

    private String requiredCredential(String name, String override) {
        String value = environment.getProperty(override);
        if (StringUtils.hasText(value)) {
            return value;
        }
        String key = name + "_" + environments.selectedEnvironment();
        value = environment.getProperty(key);
        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException("Set " + override + " or " + key
                    + " in LoginData/ShoppingApp.properties");
        }
        return value;
    }
}
