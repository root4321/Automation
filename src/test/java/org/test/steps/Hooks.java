package org.test.steps;

import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.test.support.ScenarioContext;
import org.test.support.ScenarioScreenshots;

public class Hooks {
    private static final Logger LOG = LoggerFactory.getLogger(Hooks.class);

    @Before(order = 0)
    public void startScenario(Scenario scenario) {
        ScenarioContext.start(scenario);
        LOG.info("START  scenario: {} ({})", scenario.getName(), scenario.getUri());
    }

    @AfterStep
    public void afterStep() {
        ScenarioContext.current().ifPresent(ScenarioScreenshots::afterStep);
    }

    @After(order = 0)
    public void endScenario(Scenario scenario) {
        try {
            ScenarioContext.current().ifPresent(context -> {
                try {
                    ScenarioScreenshots.afterScenario(context);
                } finally {
                    context.driver().ifPresent(driver -> {
                        try {
                            driver.quit();
                        } finally {
                            context.driver(null);
                        }
                    });
                }
            });
        } finally {
            ScenarioContext.end();
            LOG.info("FINISH scenario: {} -> {}", scenario.getName(), scenario.getStatus());
        }
    }
}
