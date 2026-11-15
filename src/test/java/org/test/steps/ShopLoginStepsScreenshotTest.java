package org.test.steps;

import io.cucumber.java.Scenario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.endsWith;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class ShopLoginStepsScreenshotTest {
    @Test
    void savesAndAttachesScreenshotForFailedScenario(@TempDir Path outputDirectory) throws IOException {
        byte[] screenshot = {1, 2, 3};
        Scenario scenario = mock(Scenario.class);
        when(scenario.isFailed()).thenReturn(true);
        when(scenario.getName()).thenReturn("Shop login");

        WebDriver driver = mock(WebDriver.class, withSettings().extraInterfaces(TakesScreenshot.class));
        TakesScreenshot screenshotDriver = (TakesScreenshot) driver;
        when(screenshotDriver.getScreenshotAs(OutputType.BYTES)).thenReturn(screenshot);

        ShopLoginSteps.captureScreenshotIfFailed(driver, scenario, outputDirectory);

        List<Path> savedScreenshots;
        try (Stream<Path> files = Files.list(outputDirectory)) {
            savedScreenshots = files.toList();
        }
        assertEquals(1, savedScreenshots.size());
        assertArrayEquals(screenshot, Files.readAllBytes(savedScreenshots.get(0)));
        verify(scenario).attach(eq(screenshot), eq("image/png"), endsWith(".png"));
    }

    @Test
    void doesNotCaptureScreenshotWhenScenarioPasses(@TempDir Path outputDirectory) throws IOException {
        Scenario scenario = mock(Scenario.class);
        WebDriver driver = mock(WebDriver.class, withSettings().extraInterfaces(TakesScreenshot.class));
        TakesScreenshot screenshotDriver = (TakesScreenshot) driver;
        when(scenario.isFailed()).thenReturn(false);

        ShopLoginSteps.captureScreenshotIfFailed(driver, scenario, outputDirectory);

        try (Stream<Path> files = Files.list(outputDirectory)) {
            assertEquals(0, files.count());
        }
        verify(screenshotDriver, never()).getScreenshotAs(any());
        verify(scenario, never()).attach(any(byte[].class), anyString(), anyString());
    }
}
