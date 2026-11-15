package org.test.support;

import io.cucumber.java.Scenario;
import org.openqa.selenium.WebDriver;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Per-scenario state bound to the thread executing the scenario, so helpers outside step classes
 * (API logging, assertions) can reach the running {@link Scenario} and browser during parallel runs.
 */
public final class ScenarioContext {
    private static final ThreadLocal<ScenarioContext> CURRENT = new ThreadLocal<>();

    private final Scenario scenario;
    private WebDriver driver;
    private boolean assertionScreenshotTakenInStep;
    private boolean failureReported;

    private ScenarioContext(Scenario scenario) {
        this.scenario = scenario;
    }

    public static ScenarioContext start(Scenario scenario) {
        ScenarioContext context = new ScenarioContext(scenario);
        CURRENT.set(context);
        return context;
    }

    public static Optional<ScenarioContext> current() {
        return Optional.ofNullable(CURRENT.get());
    }

    public static void end() {
        CURRENT.remove();
    }

    public Scenario scenario() {
        return scenario;
    }

    public Optional<WebDriver> driver() {
        return Optional.ofNullable(driver);
    }

    public void driver(WebDriver driver) {
        this.driver = driver;
    }

    public void attachText(String name, String text) {
        scenario.attach(text.getBytes(StandardCharsets.UTF_8), "text/plain", name);
    }

    boolean assertionScreenshotTakenInStep() {
        return assertionScreenshotTakenInStep;
    }

    void assertionScreenshotTakenInStep(boolean value) {
        this.assertionScreenshotTakenInStep = value;
    }

    boolean failureReported() {
        return failureReported;
    }

    void failureReported(boolean value) {
        this.failureReported = value;
    }
}
