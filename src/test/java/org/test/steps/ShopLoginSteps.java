package org.test.steps;

import io.cucumber.java.After;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.springframework.core.env.Environment;
import org.test.pages.ShopDashboardPage;
import org.test.pages.ShopLoginPage;
import org.test.support.BrowserFactory;
import org.test.support.EnvironmentUrlResolver;
import org.test.support.ShoppingAppCredentials;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class ShopLoginSteps {
    private WebDriver driver;
    private ShopLoginPage loginPage;
    private final Environment environment;
    private final EnvironmentUrlResolver urls;
    private final ShoppingAppCredentials credentials;
    private String email;
    private String password;

    public ShopLoginSteps(Environment environment, EnvironmentUrlResolver urls, ShoppingAppCredentials credentials) {
        this.environment = environment;
        this.urls = urls;
        this.credentials = credentials;
    }

    @Given("the shop login page is open")
    public void theShopLoginPageIsOpen() {
        email = credentials.email();
        password = credentials.password();

        String url = urls.urlFor("ui");
        driver = BrowserFactory.create(environment.getRequiredProperty("browser"),
                Boolean.getBoolean("browser.headless") || Boolean.getBoolean("shop.headless"));
        driver.manage().window().setSize(new Dimension(1440, 1000));
        loginPage = new ShopLoginPage(driver);
        loginPage.open(url);
    }

    @When("I sign in with the configured shop credentials")
    public void iSignInWithTheConfiguredShopCredentials() {
        loginPage.signIn(email, password);
    }

    @Then("the shop dashboard is displayed")
    public void theShopDashboardIsDisplayed() {
        new ShopDashboardPage(driver).waitUntilDisplayed();
    }

    @After("@ui")
    public void closeBrowser(Scenario scenario) throws IOException {
        if (driver != null) {
            try {
                captureScreenshotIfFailed(driver, scenario, Path.of("target", "screenshots"));
            } finally {
                driver.quit();
                driver = null;
            }
        }
    }

    static void captureScreenshotIfFailed(WebDriver driver, Scenario scenario, Path outputDirectory)
            throws IOException {
        if (!scenario.isFailed()) {
            return;
        }
        if (!(driver instanceof TakesScreenshot screenshotDriver)) {
            throw new IllegalStateException("WebDriver does not support screenshots");
        }

        byte[] screenshot = screenshotDriver.getScreenshotAs(OutputType.BYTES);
        String scenarioName = scenario.getName().replaceAll("[^A-Za-z0-9._-]", "_");
        String filename = scenarioName + "-" + UUID.randomUUID() + ".png";
        Files.createDirectories(outputDirectory);
        Files.write(outputDirectory.resolve(filename), screenshot);
        scenario.attach(screenshot, "image/png", filename);
    }
}
