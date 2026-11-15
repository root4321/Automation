package org.test.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class ShopDashboardPage {
    private final WebDriverWait wait;

    @FindBy(tagName = "nav")
    private WebElement navigation;

    public ShopDashboardPage(WebDriver driver) {
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        PageFactory.initElements(driver, this);
    }

    public void waitUntilDisplayed() {
        wait.until(ExpectedConditions.urlContains("/dashboard/"));
        wait.until(ExpectedConditions.visibilityOf(navigation));
    }
}
