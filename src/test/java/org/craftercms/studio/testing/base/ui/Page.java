package org.craftercms.studio.testing.base.ui;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.Select;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public abstract class Page extends PageComponent {

    public Page(BaseSeleniumTest test) {
        super(test);
    }

    public abstract String getPath();

    public void open() {
        getCurrentSeleniumTest().open(getPath());
    }

    public WebDriver getDriver() {
        return getCurrentSeleniumTest().getDriver();
    }

    public String getBaseUrl() {
        return getCurrentSeleniumTest().getBaseUrl();
    }
    public int getTimeOut() {
        return getCurrentSeleniumTest().getTimeOut();
    }

    public void implicitWait() {
        getDriver().manage().timeouts().implicitlyWait(4, TimeUnit.SECONDS);
    }

    public void waitPageTitleToBe(String title) {
        waitForPageToLoad(getDriver());
        WebDriverWait wait = new WebDriverWait(getDriver(), getTimeOut());
        wait.until(ExpectedConditions.titleIs(title));
    }

    public void waitElementVisible(String locator) {
        waitForPageToLoad(getDriver());
        WebDriverWait wait = new WebDriverWait(getDriver(), getTimeOut());
        wait.until(ExpectedConditions.visibilityOfElementLocated(getElementBy(locator)));
    }



    public void waitElementInvisible(String locator) {
        waitForPageToLoad(getDriver());
        WebDriverWait wait = new WebDriverWait(getDriver(), getTimeOut());
        wait.until(ExpectedConditions.invisibilityOfElementLocated(getElementBy(locator)));
    }

    public void waitElementPresent(String locator) {
        waitForPageToLoad(getDriver());
        WebDriverWait wait = new WebDriverWait(getDriver(), getTimeOut());
        wait.until(ExpectedConditions.presenceOfElementLocated(getElementBy(locator)));
    }




    public void waitElementClickable(String locator) {
        waitForPageToLoad(getDriver());
        WebDriverWait wait = new WebDriverWait(getDriver(), getTimeOut());
        wait.until(ExpectedConditions.elementToBeClickable(getElementBy(locator)));}


    public void type(String locator, String value){
        waitElementVisible(locator);
        getDriver().findElement(getElementBy(locator)).sendKeys(value);
    }

    public void click(String locator){
        waitElementClickable(locator);
        getDriver().findElement(getElementBy(locator)).click();
        waitForPageToLoad(getDriver());
        implicitWait();
    }



    public void clickByHref(String locator){
        waitForPageToLoad(getDriver());
        WebDriverWait wait = new WebDriverWait(getDriver(), getTimeOut());
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*=" + "'" + locator + "'" + "]")));
        getDriver().findElement(By.cssSelector("a[href*=" + "'" + locator + "'" + "]")).click();
        waitForPageToLoad(getDriver());
        implicitWait();
    }

    public void clickAndSelect(String locator, String option){
        waitForPageToLoad(getDriver());
        Select dropdown = new Select(getDriver().findElement(getElementBy(locator)));
        dropdown.selectByValue(option);
        waitForPageToLoad(getDriver());
        implicitWait();
    }

    public void selectItem(String locator, String option){
        waitElementClickable(locator);
        Select dropdown = new Select(getDriver().findElement(getElementBy(locator)));
        dropdown.selectByValue(option);
        waitForPageToLoad(getDriver());
        implicitWait();
    }

    public void getDropdownValues(String locator, String[] regions){
        //waitForPageToLoad(getDriver());
        waitElementClickable(locator);
        Select dropdown = new Select(getDriver().findElement(getElementBy(locator)));
        List list = Arrays.asList(regions);
        List list1 = new ArrayList();
        for(WebElement element:dropdown.getOptions())
        {
            list1.add(element.getAttribute("value"));
        }
        assertEquals(list.toArray(), list1.toArray());
    }

    public void selectItemByText(String locator, String text){
        waitElementClickable(locator);
        Select dropdown = new Select(getDriver().findElement(getElementBy(locator)));
        dropdown.selectByVisibleText(text);
    }

    public void clear(String locator){
        waitElementVisible(locator);
        getDriver().findElement(getElementBy(locator)).clear();
    }

    public void goForward(){
        getDriver().navigate().forward();
    }

    public void goBack(){
        getDriver().navigate().back();
    }

    public void switchUrl(String url){
        getDriver().get(url);
    }

    public void clickEmail(String locator) {
        WebDriverWait wait = new WebDriverWait(getDriver(), getTimeOut());
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(locator)));
                getDriver().findElement(By.xpath(locator)).click();
    }

    public void mouseHover(String locator){
        waitForPageToLoad(getDriver());
        Actions hover = new Actions(getDriver());
        hover.moveToElement(getDriver().findElement(getElementBy(locator))).build().perform();
    }

    public void switchParentWindow(){
        getDriver().switchTo().window((String) getDriver().getWindowHandles().toArray()[0]);
    }

    public void switchNewWindow(){
        for(String winHandle : getDriver().getWindowHandles()){
            getDriver().switchTo().window(winHandle);
            waitForPageToLoad(getDriver());
        }
    }

    public void goToBackWindow(){
        getDriver().close();
        switchParentWindow();
    }

    public boolean isElementPresent(String locator) {
        try {
            implicitWait();
            waitForPageToLoad(getDriver());
            getDriver().findElement(getElementBy(locator));
            return true;
        }catch (Exception e) {
            return false;
        }
    }

    public boolean isElementDisplayed(String locator) {
        try {
            implicitWait();
            waitForPageToLoad(getDriver());
            getDriver().findElement(getElementBy(locator)).isDisplayed();
            return true;
        }catch (Exception e) {
            return false;
        }
    }
    public boolean isElementDisplayedClass(String locator) {
        try {
            implicitWait();
            waitForPageToLoad(getDriver());
            getDriver().findElement(By.className(locator)).isDisplayed();
            return true;
        }catch (Exception e) {
            return false;
        }
    }

    public boolean isElementDisplayedHref(String locator) {
        try {
            implicitWait();
            waitForPageToLoad(getDriver());
            getDriver().findElement(By.cssSelector("a[href*=" + "'" + locator + "'" + "]")).isDisplayed();
            return true;
        }catch (Exception e) {
            return false;
        }
    }



    public boolean isTextContained(String text) {
        try {
            assertTrue(getDriver().findElement(By.cssSelector("BODY")).getText().matches("^[\\s\\S]*" + text +
                "[\\s\\S]*$"));
            return true;
        }catch (Exception e) {
            return false;
        }
    }

    public boolean isElementSelected(String locator) {
        try {
            waitElementVisible(locator);
            getDriver().findElement(getElementBy(locator)).isSelected();
            return true;
        }catch (Exception e) {
            return false;
        }
    }

    public boolean isElementEnabled(String locator) {
        try {
            waitElementVisible(locator);
            getDriver().findElement(getElementBy(locator)).isEnabled();
            return true;
        }catch (Exception e) {
            return false;
        }
    }

    public String getElementText(String locator) {
        waitElementVisible(locator);
        String message = getDriver().findElement(getElementBy(locator)).getText();
        return message;
    }

    public String getElementTextCss(String locator) {
        waitElementVisible(locator);
        String message = getDriver().findElement(By.cssSelector(locator)).getText();
        return message;
    }


    public String getPageTitle() {
        waitForPageToLoad(getDriver());
        String title = getDriver().getTitle();
        return title;
    }

    public String getPageUrl(){
        waitForPageToLoad(getDriver());
        String url =getDriver().getCurrentUrl();
        return url;
    }

    public String getBaseUrlUpdated(String addedUrl){
        String newUrl = getBaseUrl()+addedUrl;
        return newUrl;
    }

    public String getReadOnlyAttribute(String locator) {
        waitElementPresent(locator);
        String attribute = getDriver().findElement(getElementBy(locator)).getAttribute("readonly");
        return attribute;
    }

    public String getReadOnlyAttributeByName(String locator) {
        String attribute = getDriver().findElement(By.name(locator)).getAttribute("readonly");
        return attribute;
    }

    public int getQuantityOfElementsPresent(String locator){
        waitForPageToLoad(getDriver());
        int count = getDriver().findElements(By.className(locator)).size();
        return count;
    }

    public void waitForPageToLoad(WebDriver driver) {
        ExpectedCondition<Boolean> pageLoadCondition = new
                ExpectedCondition<Boolean>() {
                    public Boolean apply(WebDriver driver) {
                        return ((JavascriptExecutor)driver).executeScript("return document.readyState").equals("complete");
                    }
                };
        WebDriverWait wait = new WebDriverWait(driver, getTimeOut()*2);
        wait.until(pageLoadCondition);
    }

    protected By getElementBy(String locator){
        if(locator.startsWith("id:")){
            return By.id(locator.replace("id:", ""));
        }else if(locator.startsWith("css:")){
            return By.cssSelector(locator.replace("css:", ""));
        }else if(locator.startsWith("xpath:")) {
            return By.xpath(locator.replace("xpath:", ""));
        }else if(locator.startsWith("name:")){
                return By.xpath(locator.replace("name:",""));
        }else {
            return By.id(locator);
        }
    }
}