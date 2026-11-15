package org.test.support;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Locale;

@Component
public class EnvironmentUrlResolver {
    private final Environment environment;

    public EnvironmentUrlResolver(Environment environment) {
        this.environment = environment;
    }

    public String urlFor(String service) {
        String key = selectedEnvironment() + "." + service + ".url";
        String override = service.toUpperCase(Locale.ROOT) + "_BASE_URL";
        String url = environment.getProperty(override);
        if (!StringUtils.hasText(url)) {
            url = environment.getProperty(key);
        }
        if (!StringUtils.hasText(url)) {
            throw new IllegalStateException("Configure " + override + " or " + key
                    + " in environment.properties");
        }
        return url;
    }

    public String selectedEnvironment() {
        String[] profiles = environment.getActiveProfiles();
        if (profiles.length == 0) {
            profiles = environment.getDefaultProfiles();
        }
        if (profiles.length != 1) {
            throw new IllegalStateException("Select exactly one environment profile to resolve URLs");
        }
        return profiles[0];
    }
}
