package org.test.support;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.safari.SafariDriver;

import java.util.Locale;

public final class BrowserFactory {
    private BrowserFactory() {
    }

    public static WebDriver create(String browser, boolean headless) {
        switch (browser.trim().toLowerCase(Locale.ROOT)) {
            case "chrome":
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments("--window-size=1440,1000");
                if (headless) {
                    chromeOptions.addArguments("--headless=new");
                }
                return new ChromeDriver(chromeOptions);
            case "edge":
                EdgeOptions edgeOptions = new EdgeOptions();
                edgeOptions.addArguments("--window-size=1440,1000");
                if (headless) {
                    edgeOptions.addArguments("--headless=new");
                }
                return new EdgeDriver(edgeOptions);
            case "safari":
                if (headless) {
                    throw new IllegalArgumentException("Safari does not support headless mode");
                }
                return new SafariDriver();
            default:
                throw new IllegalArgumentException("Unsupported browser '" + browser
                        + "'. Choose chrome, edge, or safari");
        }
    }
}
