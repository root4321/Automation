package org.test.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class ApiPropertiesTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(PropertiesConfiguration.class)
            .withInitializer(context -> {
                context.getEnvironment().getPropertySources().remove("systemProperties");
                context.getEnvironment().getPropertySources().remove("systemEnvironment");
            })
            .withPropertyValues(
                    "api.key=test-key",
                    "api.default-update-address=test address",
                    "api.endpoints.add=/add",
                    "api.endpoints.get=/get",
                    "api.endpoints.update=/update",
                    "api.endpoints.delete=/delete");

    @Test
    void bindsEnvironmentSettingsAndEndpoints() {
        contextRunner.withPropertyValues("api.base-url=http://localhost:8080")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    ApiProperties properties = context.getBean(ApiProperties.class);
                    assertThat(properties.getBaseUrl()).isEqualTo("http://localhost:8080");
                    assertThat(properties.getKey()).isEqualTo("test-key");
                    assertThat(properties.getEndpoints().getAdd()).isEqualTo("/add");
                });
    }

    @Test
    void rejectsMissingBaseUrl() {
        contextRunner.run(context -> assertThat(context).hasFailed()
                .getFailure().hasRootCauseMessage("api.base-url must be configured for the selected environment"));
    }

    @Test
    void rejectsMissingApiKey() {
        contextRunner.withPropertyValues("api.base-url=http://localhost:8080", "api.key=")
                .run(context -> assertThat(context).hasFailed()
                        .getFailure().hasRootCauseMessage("api.key must be configured for the selected environment"));
    }

    @TestConfiguration(proxyBeanMethods = false)
    @EnableConfigurationProperties(ApiProperties.class)
    static class PropertiesConfiguration {
    }
}
