package org.test.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class ShopLoginPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    @FindBy(id = "userEmail")
    private WebElement email;

    @FindBy(id = "userPassword")
    private WebElement password;

    @FindBy(id = "login")
    private WebElement login;

    public ShopLoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        PageFactory.initElements(driver, this);
    }

    public void open(String url) {
        driver.get(url);
        wait.until(ExpectedConditions.visibilityOf(email));
    }

    public void signIn(String emailAddress, String passwordValue) {
        email.sendKeys(emailAddress);
        password.sendKeys(passwordValue);
        wait.until(ExpectedConditions.elementToBeClickable(login)).click();
    }
}
