package utils;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

public class WebDriverUtils {

    // Utility method to initialize Firefox driver with specified user agent and rejected third-party cookies
    public static WebDriver initializeFirefoxDriverWithUserAgent() {
        String geckoDriverPath = "lib/geckodriver.exe"; // Relative path to geckodriver executable
        System.setProperty("webdriver.gecko.driver", geckoDriverPath);

        FirefoxOptions options = new FirefoxOptions();
        options.addPreference("network.cookie.cookieBehavior", 2); // Reject third-party cookies
        options.addPreference("general.useragent.override", "Mozilla/5.0 (iPhone; CPU iPhone OS 16_3_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.3 Mobile/15E148 Safari/604.1");

        return new FirefoxDriver(options);
    }

    // Utility method to initialize Chrome driver with rejected third-party cookies
    public static WebDriver initializeChromeDriverWithRejectedCookies() {
        String chromeDriverPath = "lib/chromedriver.exe"; // Relative path to chromedriver executable
        System.setProperty("webdriver.chrome.driver", chromeDriverPath);

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-third-party-cookies"); // Reject third-party cookies

        return new ChromeDriver(options);
    }
}
