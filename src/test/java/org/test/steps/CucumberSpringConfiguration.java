package org.test.steps;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.test.AutomationApplication;

@CucumberContextConfiguration
@SpringBootTest(classes = AutomationApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class CucumberSpringConfiguration {
}
