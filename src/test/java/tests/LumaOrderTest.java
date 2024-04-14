package tests;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import utils.OrderUtils;

import java.io.File;
import java.util.concurrent.TimeUnit;

import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarEntry;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import net.lightbody.bmp.core.har.HarRequest;
import utils.WebDriverUtils;

import java.util.List;

public class LumaOrderTest {
    private WebDriver driver;

    // Fill order information
    String email = "example@example.com";
    String firstName = "John";
    String lastName = "Doe";
    String streetAddress = "123 Street Name";
    String city = "City";
    String state = "Alabama";
    String zip = "12345";
    String country = "United States";
    String phoneNumber = "1234567890";


    @BeforeClass
    public void setUp() {
        // Remove the "test_data.txt" file if it exists
        removeOrderDataFileIfExists();

        // Setting up WebDriver
        String chromeDriverPath = "lib/chromedriver.exe"; // Relative path to chromedriver executable
        System.setProperty("webdriver.chrome.driver", chromeDriverPath);

        driver = WebDriverUtils.initializeChromeDriverWithRejectedCookies();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
    }

    @Test
    public void basicOrderTest() {
        if (driver == null) {
            setUp(); // Initialize the driver if it's null
        }

        // Navigate to the website and place an order
        driver.get("https://magento.softwaretestingboard.com/");
        navigateToBagsSection();
        addProductToCart();
        proceedToCheckout();
        fillOrderInfo(email, firstName, lastName, streetAddress, city, state, zip, country, phoneNumber);
        placeOrder();
        String orderID = getOrderID();
        Assert.assertNotNull(orderID, "Order ID is null");
        getOrderIDWriteToFile("test_data.txt");
        tearDown();
    }

    @AfterClass
    public void tearDown() {
        // Quit WebDriver after the test
        if (driver != null) {
            driver.quit();
        }
    }
    private void removeOrderDataFileIfExists() {
        File file = new File("test_data.txt");
        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                System.out.println("Previous order data file deleted successfully.");
            } else {
                System.out.println("Failed to delete previous order data file.");
            }
        }
    }

    public void navigateToBagsSection() {
        // Navigate to the Bags section
        WebElement gearMenu = driver.findElement(By.id("ui-id-6"));
        gearMenu.click();

        WebElement bagsSubMenu = driver.findElement(By.xpath("//a[text()='Bags']"));
        bagsSubMenu.click();

        // Verify if we are on the correct page (Bags section)
        Assert.assertTrue(driver.getCurrentUrl().contains("/gear/bags.html"), "Failed to navigate to the Bags section.");
    }

    public void addProductToCart() {
        // Add a product to the cart
        WebElement addToCartButton = driver.findElement(By.xpath("(//button[@title='Add to Cart'])[3]"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", addToCartButton);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", addToCartButton);

        //This is not the best pracite, but tight deadline of the task :)
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        WebElement cartItemCount = driver.findElement(By.cssSelector(".counter-number"));
        int itemCount = Integer.parseInt(cartItemCount.getText());
        Assert.assertTrue(itemCount > 0, "Failed to add a bag to the cart.");
    }

    public void proceedToCheckout() {
            // Start BrowserMob Proxy
            BrowserMobProxy proxy = new BrowserMobProxyServer();
            proxy.start(0);

            // Get the Selenium proxy object
            Proxy seleniumProxy = ClientUtil.createSeleniumProxy(proxy);

            DesiredCapabilities capabilities = new DesiredCapabilities();
            capabilities.setCapability(CapabilityType.PROXY, seleniumProxy);

            // Create a new HAR
            proxy.newHar("checkout");

            // Proceed to the checkout page
            WebElement proceedToCheckoutButton = driver.findElement(By.cssSelector(".action.primary.checkout"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", proceedToCheckoutButton);

            // Wait for the request to be captured
            Har har = proxy.getHar();

            // Get the captured HTTP requests
            List<HarEntry> entries = har.getLog().getEntries();

            // Validate the request data
            boolean requestDataValidated = false;
            for (HarEntry entry : entries) {
                HarRequest request = entry.getRequest();
                if (request != null && request.getUrl().contains("/checkout")) {
                    String postDataText = entry.getRequest().getPostData().getText();
                    System.out.println(postDataText);
                    if (postDataText.contains(email) && postDataText.contains(firstName) && postDataText.contains(lastName) && postDataText.contains(phoneNumber)) {
                        requestDataValidated = true;
                        break;
                    }
                }
            }

            if (requestDataValidated) {
                System.out.println("Request data validated successfully.");
            } else {
                System.out.println("Failed to validate request data.");
            }

            // Stop BrowserMob Proxy
            proxy.stop();
        }

    public void fillOrderInfo(String email, String firstName, String lastName, String streetAddress,
                              String city, String state, String zip, String country, String phoneNumber) {
        // Fill in the required data with provided information
        WebElement emailField = driver.findElement(By.cssSelector("input[data-bind*='textInput: email']"));
        emailField.sendKeys(email);

        WebElement firstNameField = driver.findElement(By.name("firstname"));
        firstNameField.sendKeys(firstName);

        WebElement lastNameField = driver.findElement(By.name("lastname"));
        lastNameField.sendKeys(lastName);

        WebElement streetAddressField = driver.findElement(By.name("street[0]"));
        streetAddressField.sendKeys(streetAddress);

        WebElement cityField = driver.findElement(By.name("city"));
        cityField.sendKeys(city);

        WebElement stateField = driver.findElement(By.name("region_id"));
        Select stateDropdown = new Select(stateField);
        stateDropdown.selectByVisibleText(state);

        WebElement zipField = driver.findElement(By.name("postcode"));
        zipField.sendKeys(zip);

        WebElement countryField = driver.findElement(By.name("country_id"));
        Select countryDropdown = new Select(countryField);
        countryDropdown.selectByVisibleText(country);

        WebElement phoneNumberField = driver.findElement(By.name("telephone"));
        phoneNumberField.sendKeys(phoneNumber);

        // Select a shipping method
        WebElement shippingMethod = driver.findElement(By.name("ko_unique_3"));
        shippingMethod.click();

        // Click on "Next" button
        WebElement nextButton = driver.findElement(By.cssSelector(".button.action.continue.primary"));
        System.out.println("Successfully filled the checkout info");
        nextButton.click();
    }

    public void placeOrder() {
        // Place the order
        WebElement placeOrderButton = driver.findElement(By.cssSelector("button.action.primary.checkout"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", placeOrderButton);

        // Hardcoded sleep for 3 seconds, not the best practice and still the test task and deadline
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Successfully placed an order");
    }

    public String getOrderID() {
        // Get the order ID
        WebElement orderNumberElement = driver.findElement(By.xpath("//div[@class='checkout-success']/p/span"));
        return orderNumberElement.getText();
    }

    public void getOrderIDWriteToFile(String filename) {
        // Write the order ID to a file
        String orderID = getOrderID();
        OrderUtils.writeOrderIDToFile(orderID, filename);
        System.out.println("Successfully wrote the order ID into file");
    }
}
