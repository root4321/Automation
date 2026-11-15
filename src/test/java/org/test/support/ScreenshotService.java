package org.test.support;

import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

/** Captures browser screenshots, saves them under {@code target/screenshots} and attaches them to the scenario. */
public final class ScreenshotService {
    static final Path OUTPUT_DIRECTORY = Path.of("target", "screenshots");
    private static final Logger LOG = LoggerFactory.getLogger(ScreenshotService.class);

    private ScreenshotService() {
    }

    /** Captures the current scenario's browser, if it has one. A capture problem never fails the test. */
    public static Optional<Path> capture(String title) {
        return ScenarioContext.current()
                .flatMap(context -> context.driver()
                        .flatMap(driver -> capture(driver, context.scenario(), OUTPUT_DIRECTORY, title)));
    }

    static Optional<Path> capture(WebDriver driver, Scenario scenario, Path outputDirectory, String title) {
        if (!(driver instanceof TakesScreenshot screenshotDriver)) {
            LOG.warn("Screenshot '{}' skipped: WebDriver does not support screenshots", title);
            return Optional.empty();
        }
        try {
            byte[] screenshot = screenshotDriver.getScreenshotAs(OutputType.BYTES);
            String filename = sanitize(scenario.getName()) + "-" + sanitize(title) + "-" + UUID.randomUUID() + ".png";
            Files.createDirectories(outputDirectory);
            Path file = Files.write(outputDirectory.resolve(filename), screenshot);
            scenario.attach(screenshot, "image/png", title);
            LOG.info("Screenshot '{}' saved to {}", title, file);
            return Optional.of(file);
        } catch (RuntimeException | IOException e) {
            LOG.warn("Screenshot '{}' could not be captured: {}", title, e.toString());
            return Optional.empty();
        }
    }

    private static String sanitize(String value) {
        String clean = value.replaceAll("[^A-Za-z0-9._-]+", "_");
        return clean.length() > 80 ? clean.substring(0, 80) : clean;
    }
}
