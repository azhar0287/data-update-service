package com.example.dataupdateservice.api;

import com.example.dataupdateservice.mappers.InsuranceDataMapper;
import com.example.dataupdateservice.mappers.InsuranceFormMapper;
import com.example.dataupdateservice.mappers.UserDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping(value = {"/PatientOrder"})
@CrossOrigin("*")
public class DataController {

    private static final Logger LOGGER = LogManager.getLogger(DataController.class);

    @Autowired
    DataService dataService;

    @Autowired
    SeleniumService seleniumService;

    @RequestMapping(value = "/marquis", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity addFormDataMarquis(@RequestBody InsuranceFormMapper insuranceForm) {
        LOGGER.info("Request received for form data marquis");
        ResponseEntity response = dataService.addFormDataForMarquis(insuranceForm);
        return response;
    }

    @RequestMapping(value = "/firstox", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity addFormDataFirstox(@RequestBody InsuranceFormMapper insuranceForm) {
        LOGGER.info("Request received for form data firstox");
        ResponseEntity response = dataService.addFormDataForFirstox(insuranceForm);
        return response;
    }

    @GetMapping(value = "/orders/count")
    public ResponseEntity getOrderStats() {
        LOGGER.info("Request received for form data");
        ResponseEntity response = dataService.getDailyOrderStats();
        return response;
    }

    @GetMapping(value = "/orders/table")
    public ResponseEntity getOrderStatsForTable() {
        LOGGER.info("Request received for form data");
        ResponseEntity response = dataService.getDailyOrderStatsForTable();
        return response;
    }

    @PostMapping(value = "/user/signIn")
    @ResponseBody
    public ResponseEntity getUserDetails(@RequestBody UserDto userDto) {
        LOGGER.info("Request received "+userDto.getEmail());
        ResponseEntity response = dataService.isAuthenticated(userDto);
        return response;
    }

    @GetMapping(value = "/pdf")
    @ResponseBody
    public ResponseEntity readPdf() {
        ResponseEntity response = seleniumService.readPdf();
        return response;
    }

    /*
     * Other APIs
     * */

    @PostMapping(value = "/insuranceData/fill")
    @ResponseBody
    public ResponseEntity postInsuranceData(@RequestBody InsuranceDataMapper insuranceData) {
        LOGGER.info("Request received for fill insurance data");
        return dataService.fillInsuranceData(insuranceData);
    }

    @GetMapping(value = "/insurance/list/firtox")
    @ResponseBody
    public ResponseEntity getInsuranceList() {
        LOGGER.info("Request received for insurance name list");
        return dataService.getInsuranceList();
    }

    @GetMapping(value = "/insurance/list/marquis")
    @ResponseBody
    public ResponseEntity getInsuranceListForMarquis() {
        LOGGER.info("Request received for insurance name list");
        return dataService.getInsuranceListMarquis();
    }

    @GetMapping(value = "/form/qrcode")
    @ResponseBody
    public ResponseEntity getQrCodeForSubmission() {
        LOGGER.info("Request received for insurance name list");
        return dataService.getQRCode();
    }

}
