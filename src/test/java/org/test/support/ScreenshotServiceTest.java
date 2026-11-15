package org.test.support;

import io.cucumber.java.Scenario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class ScreenshotServiceTest {
    private static WebDriver screenshotDriver(byte[] screenshot) {
        WebDriver driver = mock(WebDriver.class, withSettings().extraInterfaces(TakesScreenshot.class));
        when(((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES)).thenReturn(screenshot);
        return driver;
    }

    private static Scenario scenario(boolean failed) {
        Scenario scenario = mock(Scenario.class);
        when(scenario.getName()).thenReturn("Shop login");
        when(scenario.isFailed()).thenReturn(failed);
        return scenario;
    }

    @Test
    void savesAndAttachesScreenshot(@TempDir Path dir) throws IOException {
        byte[] screenshot = {1, 2, 3};
        Scenario scenario = scenario(true);

        Optional<Path> saved = ScreenshotService.capture(screenshotDriver(screenshot), scenario, dir, "Step failed");

        assertArrayEquals(screenshot, Files.readAllBytes(saved.orElseThrow()));
        verify(scenario).attach(screenshot, "image/png", "Step failed");
    }

    @Test
    void captureProblemsNeverFailTheTest(@TempDir Path dir) {
        WebDriver driver = screenshotDriver(null);
        when(((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES)).thenThrow(new WebDriverException("gone"));
        Scenario scenario = scenario(true);

        assertEquals(Optional.empty(), ScreenshotService.capture(driver, scenario, dir, "Step failed"));
        assertEquals(Optional.empty(), ScreenshotService.capture(mock(WebDriver.class), scenario, dir, "Step failed"));
        verify(scenario, never()).attach(any(byte[].class), anyString(), anyString());
    }

    @Test
    void scenarioWithoutBrowserTakesNoScreenshot() {
        ScenarioContext.start(scenario(true));
        try {
            assertEquals(Optional.empty(), ScreenshotService.capture("End of scenario"));
        } finally {
            ScenarioContext.end();
        }
    }

    @Test
    void screenshotsAreTakenForFailedStepsAssertionsAndScenarioEnd(@TempDir Path dir) throws IOException {
        Scenario scenario = scenario(false);
        WebDriver driver = screenshotDriver(new byte[]{9});
        ScenarioContext context = ScenarioContext.start(scenario);
        context.driver(driver);
        try {
            ScenarioScreenshots.afterStep(context);
            verify(scenario, never()).attach(any(byte[].class), anyString(), anyString());

            // A failed assertion captures once; the failing step must not capture a second time.
            assertThrows(AssertionError.class, () -> UiAssertions.assertTrue("title is shown", false));
            when(scenario.isFailed()).thenReturn(true);
            ScenarioScreenshots.afterStep(context);
            verify(scenario, times(1)).attach(any(byte[].class), eq("image/png"), eq("Assertion failed - title is shown"));
            verify(scenario, never()).attach(any(byte[].class), eq("image/png"), eq("Step failed"));

            // Later skipped steps never add more failure screenshots.
            ScenarioScreenshots.afterStep(context);
            ScenarioScreenshots.afterScenario(context);
            verify(scenario, times(2)).attach(any(byte[].class), eq("image/png"), anyString());
        } finally {
            ScenarioContext.end();
            try (Stream<Path> files = Files.list(Path.of("target", "screenshots"))) {
                files.filter(file -> file.getFileName().toString().startsWith("Shop_login-")).forEach(file -> file.toFile().delete());
            }
        }
    }
}
