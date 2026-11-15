package org.test.support;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.safari.SafariDriver;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockConstruction;

class BrowserFactoryTest {
    @Test
    void startsChromeWithHeadlessOptions() {
        try (MockedConstruction<ChromeDriver> drivers = mockConstruction(ChromeDriver.class, (driver, context) -> {
            ChromeOptions options = (ChromeOptions) context.arguments().get(0);
            Map<?, ?> chromeOptions = (Map<?, ?>) options.getCapability("goog:chromeOptions");
            assertTrue(((List<?>) chromeOptions.get("args")).contains("--headless=new"));
        })) {
            ChromeDriver driver = (ChromeDriver) BrowserFactory.create(" CHROME ", true);
            assertSame(drivers.constructed().get(0), driver);
        }
    }

    @Test
    void startsEdgeWithHeadlessOptions() {
        try (MockedConstruction<EdgeDriver> drivers = mockConstruction(EdgeDriver.class, (driver, context) -> {
            EdgeOptions options = (EdgeOptions) context.arguments().get(0);
            Map<?, ?> edgeOptions = (Map<?, ?>) options.getCapability("ms:edgeOptions");
            assertTrue(((List<?>) edgeOptions.get("args")).contains("--headless=new"));
        })) {
            EdgeDriver driver = (EdgeDriver) BrowserFactory.create("edge", true);
            assertSame(drivers.constructed().get(0), driver);
        }
    }

    @Test
    void startsSafari() {
        try (MockedConstruction<SafariDriver> drivers = mockConstruction(SafariDriver.class)) {
            SafariDriver driver = (SafariDriver) BrowserFactory.create("safari", false);
            assertSame(drivers.constructed().get(0), driver);
        }
    }

    @Test
    void rejectsUnknownBrowsers() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> BrowserFactory.create("firefox", false));
        assertTrue(error.getMessage().contains("chrome, edge, or safari"));
    }

    @Test
    void rejectsHeadlessSafariBeforeStartingADriver() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> BrowserFactory.create("Safari", true));
        assertTrue(error.getMessage().contains("does not support headless"));
    }
}
