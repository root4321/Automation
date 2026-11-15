package org.test;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.test.config.ApiProperties;

@SpringBootApplication
@EnableConfigurationProperties(ApiProperties.class)
public class AutomationApplication {
}
