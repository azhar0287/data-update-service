package com.example.dataupdateservice.api;

import com.example.dataupdateservice.mappers.InsuranceFormMapper;
import io.github.bonigarcia.wdm.WebDriverManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;


@Service
public class SeleniumService {
    private static final Logger LOGGER = LogManager.getLogger(SeleniumService.class);

    @Value("${login.url}")
    String loginUrl;

    @Value("${firstToxUsername}")
    String firstToxUsername;

    @Value("${firstToxPassword}")
    String firstToxPassword;

    WebDriver driver;

    public WebDriver getDriver() {


       System.setProperty("webdriver.chrome.driver", System.getProperty("user.dir") + "/usr/bin/chromedriver");
        //System.setProperty("webdriver.chrome.driver", System.getProperty("user.dir") + "src/main/resources/chromedriver.exe");
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = getChromeOptions();
        driver = new ChromeDriver(options);
        driver.get(this.loginUrl);
        return driver;
    }

    public ChromeOptions getChromeOptions() {
        DesiredCapabilities capabilities = DesiredCapabilities.chrome();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox"); // Bypass OS security model, MUST BE THE VERY FIRST OPTION
       // options.addArguments("--headless");
        options.setExperimentalOption("useAutomationExtension", false);
        options.addArguments("start-maximized"); // open Browser in maximized mode
        options.addArguments("disable-infobars");
        options.addArguments("--disable-extensions"); // disabling extensions
        options.addArguments("--disable-gpu"); // applicable to windows os only
        options.addArguments("--disable-dev-shm-usage"); // overcome limited resource problems
        options.merge(capabilities);

        return options;
    }

    public String doLogin() {
        driver = this.getDriver();
        try {
            WebElement username = driver.findElement(By.id("cphDefault_txtLoginName"));
            WebElement password = driver.findElement(By.id("cphDefault_txtLoginPassword"));
            WebElement loginButton = driver.findElement(By.id("cphDefault_btnLogin"));
            username.sendKeys(firstToxUsername);
            password.sendKeys(firstToxPassword);
            loginButton.click();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return driver.getTitle();
    }

    public void processForm(InsuranceFormMapper mapper) throws InterruptedException {
        String response  = this.doLogin();
        if(response.equalsIgnoreCase("FirsTox")) {
            LOGGER.info("Logged in Successfully ");

            WebDriverWait wait = new WebDriverWait(driver, 60);// 1 minute
            wait.until(ExpectedConditions.visibilityOfElementLocated(new By.ByLinkText("New Patient")));
            WebElement newPage = driver.findElement(By.linkText("New Patient"));
            newPage.click();

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_ddlOrganization_ddlObj")));
            WebElement organization = driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_ddlOrganization_ddlObj"));

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_txtFirstName")));
            WebElement firstName  = driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_txtFirstName"));
            firstName.sendKeys(mapper.getFirstName());

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_txtLastName")));
            WebElement lastName  = driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_txtLastName"));
            lastName.sendKeys(mapper.getLastName());

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("ctl00$ctl00$ctl00$cphDefault$cphTemplate$cphTemplate$patientDetail$ddlGender$ddlObj")));
            Select dropdown = new Select(driver.findElement(By.name("ctl00$ctl00$ctl00$cphDefault$cphTemplate$cphTemplate$patientDetail$ddlGender$ddlObj")));
            dropdown.selectByIndex(1);

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_txtAddressZip")));
            driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_txtAddressZip")).click();
            driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_txtAddressZip")).sendKeys(mapper.getZipCode());

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_txtAddressStreet")));
            driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_txtAddressStreet")).click();
            driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_txtAddressStreet")).sendKeys(mapper.getStreet());

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_txtHomePhone")));
            driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_txtHomePhone")).click();
            driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_txtHomePhone")).sendKeys(mapper.getMobileNumber());

//            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("ctl00$ctl00$ctl00$cphDefault$cphTemplate$cphTemplate$patientDetail$ddlRace$ddlObj")));
//            Select race = new Select(driver.findElement(By.name("ctl00$ctl00$ctl00$cphDefault$cphTemplate$cphTemplate$patientDetail$ddlRace$ddlObj")));
//            race.selectByIndex(7);
//
//            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("ctl00$ctl00$ctl00$cphDefault$cphTemplate$cphTemplate$patientDetail$ddlEthnicity$ddlObj")));
//            Select ethnicity = new Select(driver.findElement(By.name("ctl00$ctl00$ctl00$cphDefault$cphTemplate$cphTemplate$patientDetail$ddlEthnicity$ddlObj")));
//            ethnicity.selectByIndex(3);

            driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_txtEmailAddress")).click();
            driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_txtEmailAddress")).sendKeys(mapper.getEmail());

            WebElement dob  = driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_txtDOB"));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_txtDOB")));

            WebElement updatePatientButton  = driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_btnSave"));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cphDefault_cphTemplate_cphTemplate_btnSave")));

            organization.sendKeys("Sethi Labs Dallas");
            firstName.sendKeys(mapper.getFirstName());
            lastName.sendKeys(mapper.getLastName());


            Thread.sleep(30000);

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_ddlLocation_ddlObj")));
            driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_ddlLocation_ddlObj"));
            driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_ddlLocation_ddlObj")).sendKeys("Sethi Labs Dallas");


            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_ddlLocation_ddlObj")));
            driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_ddlLocation_ddlObj"));
            driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_ddlLocation_ddlObj")).sendKeys("Sethi Labs Dallas");
/////////////////
            Thread.sleep(30000);


            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_ddlPhysician_ddlObj")));
            driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_ddlPhysician_ddlObj")).click();
            driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_ddlPhysician_ddlObj")).sendKeys("McCoy, APRN Sandra");

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_ddlPhysician_ddlObj")));
            driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_ddlPhysician_ddlObj")).click();
            driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_ddlPhysician_ddlObj")).sendKeys("McCoy, APRN Sandra");


//            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_ddlPhysician_ddlObj")));
//            WebElement provider  = driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_ddlPhysician_ddlObj"));
//


           // updatePatientButton.click();


            LOGGER.info("update patient button clicked");
//            driver.quit();

        }
        System.out.println("Response"+ response);
    }

}
