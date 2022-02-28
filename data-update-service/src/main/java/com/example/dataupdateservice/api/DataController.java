package com.example.dataupdateservice.api;

import com.example.dataupdateservice.mappers.InsuranceFormMapper;
import com.example.dataupdateservice.mappers.UserDto;
import com.example.dataupdateservice.response.DefaultResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping(value = {"/insuranceData"})
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
        ResponseEntity response = dataService.getOrderStats();
        return response;
    }

    @PostMapping(value = "/user/signIn")
    @ResponseBody
    public ResponseEntity getUserDetails(@RequestBody UserDto userDto) {
        LOGGER.info("Request received "+userDto.getEmail());
        ResponseEntity response = dataService.isAuthenticated(userDto);
        return response;
    }
}
