package org.test.support;

import io.cucumber.java.Scenario;

/** Decides when screenshots are taken during a scenario; used by the Cucumber hooks. */
public final class ScenarioScreenshots {
    private ScenarioScreenshots() {
    }

    /** After every step: capture once for the step that first fails the scenario, unless an assertion already did. */
    public static void afterStep(ScenarioContext context) {
        Scenario scenario = context.scenario();
        boolean assertionCaptured = context.assertionScreenshotTakenInStep();
        context.assertionScreenshotTakenInStep(false);
        if (scenario.isFailed() && !context.failureReported()) {
            context.failureReported(true);
            if (!assertionCaptured) {
                ScreenshotService.capture("Step failed");
            }
        }
    }

    /** At the end of every scenario, passed or failed. */
    public static void afterScenario(ScenarioContext context) {
        Scenario scenario = context.scenario();
        String title = scenario.isFailed() ? "Scenario failed" : "End of scenario";
        ScreenshotService.capture(title + " (" + scenario.getStatus() + ")");
    }
}
