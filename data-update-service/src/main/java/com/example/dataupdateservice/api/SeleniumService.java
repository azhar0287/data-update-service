package com.example.dataupdateservice.api;

import com.example.dataupdateservice.mappers.InsuranceFormMapper;
import com.example.dataupdateservice.mappers.PrintDocLink;
import io.github.bonigarcia.wdm.WebDriverManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Random;


@Service
public class SeleniumService {
    private static final Logger LOGGER = LogManager.getLogger(SeleniumService.class);

    @Autowired
    DataService dataService;

    @Autowired
    OrderCreateService orderCreateService;

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
        options.addArguments("--headless");
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

    public PrintDocLink processForm(InsuranceFormMapper mapper) throws InterruptedException {
        String response  = this.doLogin();
        PrintDocLink printDocLink = new PrintDocLink();
        if(response.equalsIgnoreCase("FirsTox")) {
            LOGGER.info("Logged in Successfully ");

            WebDriverWait wait = new WebDriverWait(driver, 120);// 1 minute
            wait.until(ExpectedConditions.visibilityOfElementLocated(new By.ByLinkText("New Patient")));
            WebElement newPage = driver.findElement(By.linkText("New Patient"));
            newPage.click();

            Thread.sleep(2000);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_ddlOrganization_ddlObj")));
            WebElement organization = driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_ddlOrganization_ddlObj"));
            organization.sendKeys("Sethi Labs Dallas");

            Thread.sleep(3000);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_ddlLocation_ddlObj")));
            driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_ddlLocation_ddlObj"));
            driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_ddlLocation_ddlObj")).sendKeys("Sethi Labs Dallas");

            Thread.sleep(3000);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_ddlPhysician_ddlObj")));
            driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_ddlPhysician_ddlObj")).click();
            driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_ddlPhysician_ddlObj")).sendKeys("McCoy, APRN Sandra");

            Thread.sleep(1000);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_txtFirstName")));
            WebElement firstName  = driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_txtFirstName"));
            firstName.sendKeys(mapper.getFirstName());

            Thread.sleep(1000);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_txtLastName")));
            WebElement lastName  = driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_txtLastName"));
            lastName.sendKeys(mapper.getLastName());

            Thread.sleep(1000);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("ctl00$ctl00$ctl00$cphDefault$cphTemplate$cphTemplate$patientDetail$ddlGender$ddlObj")));
            Select dropdown = new Select(driver.findElement(By.name("ctl00$ctl00$ctl00$cphDefault$cphTemplate$cphTemplate$patientDetail$ddlGender$ddlObj")));
            dropdown.selectByIndex(1);

            Thread.sleep(1000);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_txtAddressZip")));
            driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_txtAddressZip")).click();
            driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_txtAddressZip")).sendKeys(mapper.getZipCode());

            Thread.sleep(2000);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("ctl00$ctl00$ctl00$cphDefault$cphTemplate$cphTemplate$patientDetail$ddlRace$ddlObj")));
            Select race = new Select(driver.findElement(By.name("ctl00$ctl00$ctl00$cphDefault$cphTemplate$cphTemplate$patientDetail$ddlRace$ddlObj")));
            race.selectByIndex(7);

            Thread.sleep(4000);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("ctl00$ctl00$ctl00$cphDefault$cphTemplate$cphTemplate$patientDetail$ddlEthnicity$ddlObj")));
            Select ethnicity = new Select(driver.findElement(By.name("ctl00$ctl00$ctl00$cphDefault$cphTemplate$cphTemplate$patientDetail$ddlEthnicity$ddlObj")));
            ethnicity.selectByIndex(3);

            Thread.sleep(2000);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_txtEmailAddress")));
            driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_txtEmailAddress")).click();
            driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_txtEmailAddress")).sendKeys(mapper.getEmail()+"+"+new Random().nextDouble());

            Thread.sleep(1000);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_txtDOB")));
            WebElement dob  = driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_txtDOB"));
            dob.sendKeys(mapper.getDob().format(DateTimeFormatter.ofPattern("MM/dd/YYYY")));

            Thread.sleep(2000);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_txtAddressStreet")));
            driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_txtAddressStreet")).click();
            driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_txtAddressStreet")).sendKeys(mapper.getStreet());

            Thread.sleep(2000);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_txtHomePhone")));
            driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_txtHomePhone")).click();
            driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_txtHomePhone")).sendKeys(mapper.getMobileNumber());

            Thread.sleep(1000);
            WebElement updatePatientButton  = driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_btnSave"));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cphDefault_cphTemplate_cphTemplate_btnSave")));

            updatePatientButton.click();

            LOGGER.info("Update Patient Clicked ");

            Thread.sleep(5000);
            wait.until(ExpectedConditions.visibilityOfElementLocated(new By.ByXPath("//*[@id='divPatientInsurance']/a/div[3]")));
            driver.findElement(new By.ByXPath("//*[@id='divPatientInsurance']/a/div[3]")).click();

            driver.findElement(By.className("addlink")).click();

            Thread.sleep(5000);
            WebElement iframe1 = driver.findElement(new By.ByXPath("//*[@id='modalIframe']"));
            driver.switchTo().frame(iframe1);

            Thread.sleep(3000);
            Select sel = new Select(driver.findElement(By.id("cphTemplate_patientinsuranceDetail_ddlInsurancePriority_ddlObj")));
            sel.selectByVisibleText("Primary");
            Thread.sleep(5000);

            driver.findElement(By.className("searchlink")).click();

            Thread.sleep(3000);
            WebElement iframe2 = driver.findElement(new By.ByXPath("//*[@id='modalIframe']"));
            driver.switchTo().frame(iframe2);

            Thread.sleep(10000);
            driver.findElement(By.id("cphTemplate_insuranceplanlistView_gvSearch_txtSearch")).sendKeys("hrsa");
            driver.findElement(By.id("cphTemplate_insuranceplanlistView_gvSearch_txtSearch")).sendKeys(Keys.ENTER);

            Thread.sleep(5000);
            driver.findElement(new By.ByClassName("selectlink")).click();
            Thread.sleep(5000);
            driver.switchTo().parentFrame();

            driver.findElement(By.id("cphTemplate_btnSave")).click();

            LOGGER.info("Insurance added ");
            Thread.sleep(5000);
            driver.switchTo().parentFrame();

            Thread.sleep(10000);
            driver.findElement(By.className("addorderlink")).click();

            Thread.sleep(2000);
            sel = new Select(driver.findElement(By.id("cphDefault_cphTemplate_laborderDetail_ddlCustomField1")));
            sel.selectByVisibleText("CPT-Saline");
            Thread.sleep(2000);

            sel = new Select(driver.findElement(By.id("cphDefault_cphTemplate_laborderDetail_ddlCustomField8")));
            sel.selectByVisibleText("Nasopharyngeal (NP)");
            Thread.sleep(5000);

            driver.findElement(By.id("cphDefault_cphTemplate_laborderDetail_lnkAddDiagnosisCode")).click();
            Thread.sleep(3000);

            WebElement iframe3 = driver.findElement (new By.ByXPath("//*[@id='modalIframe']"));
            driver.switchTo().frame(iframe3);
            Thread.sleep(1000);

            driver.findElement(By.id("cphTemplate_laborderdiagnosiscodeDetail_gvDiagnosisCode_chkDiagnosisCodeMap_1")).click();
            Thread.sleep(5000);

            driver.findElement((By.id("cphTemplate_laborderdiagnosiscodeDetail_gvDiagnosisCode_chkDiagnosisCodeMap_0"))).click();
            Thread.sleep(5000);
            LOGGER.info("Diagnosis added ");

            driver.findElement(By.id("cphTemplate_btnClose")).click();

            Thread.sleep(3000);
            driver.switchTo().parentFrame();
            Thread.sleep(2000);
            driver.findElement((By.id("cphDefault_cphTemplate_laborderDetail_txtCollectionDateTime"))).sendKeys(orderCreateService.getCurrentDate());

            Thread.sleep(3000);
            driver.findElement(By.id("cphDefault_cphTemplate_laborderDetail_orderTests_gvOrderPanels_chkOrderTest_0")).click();
            driver.findElement(By.id("cphDefault_cphTemplate_laborderDetail_btnCreateOrder")).click();

            Thread.sleep(5000);
            WebElement iframe4 = driver.findElement(new By.ByXPath("//*[@id='modalIframe']"));
            driver.switchTo().frame(iframe4);

            LOGGER.info("Test added ");

            Thread.sleep(4000);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Print Requisition")));
            String pdfLink = driver.findElement(By.linkText("Print Requisition")).getAttribute("href");
            LOGGER.info("Pdf Link "+pdfLink);
            driver.findElement(By.id("cphTemplate_lnkPrintRequisition")).click();
            Thread.sleep(2000);

            String labelLink = driver.findElement (By.linkText("Print Label")).getAttribute("href");
            driver.findElement (By.linkText("Print Label")).click();

            printDocLink.setFirstToxPdfLink(pdfLink);
            printDocLink.setFirstToxLabelLink(labelLink);

            LOGGER.info("FirstTox Form has submitted successfully");
            driver.quit();
        }
        return printDocLink;
    }
}
