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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.*;


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
        options.addArguments("--disable-dev-shm-usage"); // overcome limited resource problems
        options.addArguments("--window-size=1920x1080"); //I added this
        options.addArguments("disable-popup-blocking");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-web-security");
        options.addArguments("--use-fake-ui-for-media-stream");
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
        Long startTime=System.currentTimeMillis();
        String response = this.doLogin();
        PrintDocLink printDocLink = new PrintDocLink();

        if (response.equalsIgnoreCase("FirsTox")) {
            LOGGER.info("Logged in Successfully ");

            WebDriverWait wait = new WebDriverWait(driver, 120);// 1 minute
            wait.until(ExpectedConditions.visibilityOfElementLocated(new By.ByLinkText("New Patient")));
            WebElement newPage = driver.findElement(By.linkText("New Patient"));
            newPage.click();

            Thread.sleep(1000);
            //wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_ddlOrganization_ddlObj")));
            WebElement organization = driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_ddlOrganization_ddlObj"));
            organization.sendKeys("Sethi Labs Dallas");

            Thread.sleep(1500);
            driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_ddlLocation_ddlObj"));
            driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_ddlLocation_ddlObj")).sendKeys("Sethi Labs Dallas");

            Thread.sleep(1500);
            driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_ddlPhysician_ddlObj")).click();
            driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_ddlPhysician_ddlObj")).sendKeys("McCoy, APRN Sandra");

            Thread.sleep(500);
            WebElement firstName = driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_txtFirstName"));
            firstName.sendKeys(mapper.getFirstName());

            Thread.sleep(500);
            WebElement lastName = driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_txtLastName"));
            lastName.sendKeys(mapper.getLastName());

            Thread.sleep(500);
            Select dropdown = new Select(driver.findElement(By.name("ctl00$ctl00$ctl00$cphDefault$cphTemplate$cphTemplate$patientDetail$ddlGender$ddlObj")));
            dropdown.selectByIndex(1);

            Thread.sleep(500);
            driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_txtAddressZip")).click();
            driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_txtAddressZip")).sendKeys(mapper.getZipCode());

            Thread.sleep(1000);
            Select race = new Select(driver.findElement(By.name("ctl00$ctl00$ctl00$cphDefault$cphTemplate$cphTemplate$patientDetail$ddlRace$ddlObj")));
            race.selectByIndex(7);

            Thread.sleep(2000);
            Select ethnicity = new Select(driver.findElement(By.name("ctl00$ctl00$ctl00$cphDefault$cphTemplate$cphTemplate$patientDetail$ddlEthnicity$ddlObj")));
            ethnicity.selectByIndex(3);

            Thread.sleep(2000);
            driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_txtEmailAddress")).click();
            driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_txtEmailAddress")).sendKeys(mapper.getEmail() + "+" + new Random().nextDouble());

            Thread.sleep(500);
            WebElement dob = driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_txtDOB"));
            dob.sendKeys(mapper.getDob().format(DateTimeFormatter.ofPattern("MM/dd/YYYY")));

            Thread.sleep(1000);
            driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_txtAddressStreet")).click();
            driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_txtAddressStreet")).sendKeys(mapper.getStreet());

            Thread.sleep(1000);
            driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_txtHomePhone")).click();
            driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_patientDetail_txtHomePhone")).sendKeys(mapper.getMobileNumber());

            Thread.sleep(500);
            WebElement updatePatientButton = driver.findElement(By.id("cphDefault_cphTemplate_cphTemplate_btnSave"));
            Thread.sleep(500);
            updatePatientButton.click();

            LOGGER.info("Update Patient Clicked ");

            Thread.sleep(7000);
            driver.findElement(new By.ByXPath("//*[@id='divPatientInsurance']/a/div[3]")).click();
            driver.findElement(By.className("addlink")).click();
            LOGGER.info("Add link  ");

            Thread.sleep(3000);
            WebElement iframe1 = driver.findElement(new By.ByXPath("//*[@id='modalIframe']"));
            driver.switchTo().frame(iframe1);
            LOGGER.info("Switch for first frame  ");

            Thread.sleep(2000);
            Select sel = new Select(driver.findElement(By.id("cphTemplate_patientinsuranceDetail_ddlInsurancePriority_ddlObj")));
            sel.selectByVisibleText("Primary");
            Thread.sleep(3000);
            LOGGER.info("Select primary");
            driver.findElement(By.className("searchlink")).click();

            Thread.sleep(1500);
            WebElement iframe2 = driver.findElement(new By.ByXPath("//*[@id='modalIframe']"));
            driver.switchTo().frame(iframe2);

            LOGGER.info("Switch for 2nd Frame  ");
            Thread.sleep(7000);
            //driver.findElement(By.id("cphTemplate_insuranceplanlistView_gvSearch_txtSearch")).sendKeys("hrsa");
            driver.findElement(By.id("cphTemplate_insuranceplanlistView_gvSearch_txtSearch")).sendKeys(mapper.getInsuranceName());

            driver.findElement(By.id("cphTemplate_insuranceplanlistView_gvSearch_txtSearch")).sendKeys(Keys.ENTER);

            LOGGER.info("Hrsa added  ");
            Thread.sleep(3000);
            driver.findElement(new By.ByClassName("selectlink")).click();

            Thread.sleep(3000);
            driver.switchTo().parentFrame();
            LOGGER.info("Save button for insurance ");
            driver.findElement(By.id("cphTemplate_btnSave")).click();

            LOGGER.info("Insurance added ");
            Thread.sleep(3000);
            driver.switchTo().parentFrame();
            driver.switchTo().parentFrame();

            LOGGER.info("Switch for Parent frame  ");
            Thread.sleep(3000);
            driver.findElement(By.className("addorderlink")).click();
            LOGGER.info("Add order Link buttonn clicked  ");
            Thread.sleep(1000);
            sel = new Select(driver.findElement(By.id("cphDefault_cphTemplate_laborderDetail_ddlCustomField1")));
            sel.selectByVisibleText("CPT-Saline");
            Thread.sleep(1000);

            sel = new Select(driver.findElement(By.id("cphDefault_cphTemplate_laborderDetail_ddlCustomField8")));
            sel.selectByVisibleText("Nasopharyngeal (NP)");
            Thread.sleep(3000);

            driver.findElement(By.id("cphDefault_cphTemplate_laborderDetail_lnkAddDiagnosisCode")).click();
            Thread.sleep(2000);
            LOGGER.info("Diagnosis Code selected ");

            WebElement iframe3 = driver.findElement(new By.ByXPath("//*[@id='modalIframe']"));
            driver.switchTo().frame(iframe3);
            Thread.sleep(1000);

            driver.findElement(By.id("cphTemplate_laborderdiagnosiscodeDetail_gvDiagnosisCode_chkDiagnosisCodeMap_1")).click();
            Thread.sleep(3000);

            driver.findElement((By.id("cphTemplate_laborderdiagnosiscodeDetail_gvDiagnosisCode_chkDiagnosisCodeMap_0"))).click();
            Thread.sleep(3000);
            LOGGER.info("Diagnosis added ");

            driver.findElement(By.id("cphTemplate_btnClose")).click();

            Thread.sleep(2000);
            driver.switchTo().parentFrame();
            Thread.sleep(1000);

            ////////////////
            WebElement req = driver.findElement(By.id("cphDefault_cphTemplate_laborderDetail_txtRequisitionDateTime"));
            String time = req.getAttribute("value");

            driver.findElement((By.id("cphDefault_cphTemplate_laborderDetail_txtCollectionDateTime"))).sendKeys(time);

            Thread.sleep(1500);
            driver.findElement(By.id("cphDefault_cphTemplate_laborderDetail_orderTests_gvOrderPanels_chkOrderTest_0")).click();
            Thread.sleep(3000);
            driver.findElement(By.id("cphDefault_cphTemplate_laborderDetail_btnCreateOrder")).click();

            Thread.sleep(3000);
            WebElement iframe4 = driver.findElement(new By.ByXPath("//*[@id='modalIframe']"));
            driver.switchTo().frame(iframe4);

            LOGGER.info("Test added ");

            Thread.sleep(2000);
            //wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Print Requisition")));
            String pdfLink = driver.findElement(By.linkText("Print Requisition")).getAttribute("href");
            LOGGER.info("Pdf Link " + pdfLink);
            driver.findElement(By.id("cphTemplate_lnkPrintRequisition")).click();
            Thread.sleep(1000);

            wait.until(ExpectedConditions.elementToBeClickable(By.id("cphTemplate_lnkPrintLabel")));
            String labelLink = driver.findElement(By.id("cphTemplate_lnkPrintLabel")).getAttribute("href");
            LOGGER.info("Label Link " + labelLink);
            Thread.sleep(1000);
            driver.findElement(By.id("cphTemplate_lnkPrintLabel")).click();
            Thread.sleep(2000);
            driver.findElement(By.id("cphTemplate_lnkPrintLabel")).click();

            LOGGER.info("FirstTox Form has submitted successfully");
            printDocLink.setFirstToxPdfLink(pdfLink);
            printDocLink.setPdf(this.getLablePdf());

            Long endTime=System.currentTimeMillis();
            Long finalTime= (endTime-startTime);
            System.out.println("Time Consumed= "+(finalTime/1000)/60 +"min");

            driver.quit();
        }
        return printDocLink;
    }

    public byte[] getLablePdf() {
        byte[] pdf = new byte[0];
        try {
            Path pdfPath = Paths.get("/data/servers/services/label.pdf");
            //  Path pdfPath = Paths.get("C:/Users/admin/Desktop/data/webreq.pdf");
            pdf = Files.readAllBytes(pdfPath);
            LOGGER.info("pdf content has reached");

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return pdf;
    }

    public ResponseEntity readPdf() {
        byte[] pdf = new byte[0];
        try {
            Path pdfPath = Paths.get("/data/servers/services/label.pdf");
          //  Path pdfPath = Paths.get("C:/Users/admin/Desktop/data/webreq.pdf");
            pdf = Base64.getEncoder().encode(Files.readAllBytes(pdfPath));
            LOGGER.info("pdf content" + pdf);

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new ResponseEntity(pdf, HttpStatus.OK);
    }
}
