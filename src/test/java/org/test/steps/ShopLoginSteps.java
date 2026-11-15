package org.test.steps;

import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.springframework.core.env.Environment;
import org.test.pages.ShopDashboardPage;
import org.test.pages.ShopLoginPage;
import org.test.support.BrowserFactory;
import org.test.support.EnvironmentUrlResolver;
import org.test.support.ShoppingAppCredentials;

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
    public void closeBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }

}
