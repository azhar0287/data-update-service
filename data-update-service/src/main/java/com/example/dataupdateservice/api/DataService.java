package com.example.dataupdateservice.api;

import com.example.dataupdateservice.feign.FeignClientService;
import com.example.dataupdateservice.insuranceform.InsuranceForm;

import com.example.dataupdateservice.insuranceform.InsuranceFormRepository;
import com.example.dataupdateservice.mappers.*;
import com.example.dataupdateservice.order.OrderMapper;
import com.example.dataupdateservice.response.DefaultResponse;
import com.example.dataupdateservice.user.User;
import com.example.dataupdateservice.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import net.bytebuddy.TypeCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.awt.print.Book;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;


@Service
public class DataService {

    private static final Logger LOGGER = LogManager.getLogger(DataService.class);

    @Autowired
    UserRepository userRepository;
    @Autowired
    InsuranceFormRepository insuranceFormRepository;
    @Autowired
    FeignClientService feignClientService;
    @Autowired
    SeleniumService seleniumService;
    @Autowired
    OrderCreateService orderCreateService;

    @Value("${userToken}")
    String user;
    String mode = "save";
    String ordnum = "NEW";
    String outputformat = "JSON";
    String housecall = "NO";
    String patid = "NEW";

    public ResponseEntity addFormDataForFirstox(InsuranceFormMapper mapper) {
        PrintDocLink printDocLink = new PrintDocLink();
        try {
            printDocLink =  seleniumService.processForm(mapper);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
       return new ResponseEntity<>(new DefaultResponse("Success", "Form Data has save successfully", printDocLink), HttpStatus.OK);
    }

    public ResponseEntity addFormDataForMarquis(InsuranceFormMapper mapper) {
        OrderResponse orderResponse = null;
        PrintDocLink printDocLink = new PrintDocLink();
        try {
            InsuranceForm insuranceForm = this.mapFormData(mapper);
            orderResponse = this.sendDataToMarques(mapper);
            String patientId = orderResponse.getPatientId();
            if(patientId != null) {
                insuranceForm.setPatientId(patientId);
                insuranceForm.setOrderNumber(orderResponse.getOrderNumber());
                insuranceFormRepository.save(insuranceForm);
                LOGGER.info("Patient Data has saved "+patientId);
            }
            printDocLink.setMarquisPdfLink(orderResponse.getPdfUrl());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new ResponseEntity<>(new DefaultResponse("Success", "Form Data has save successfully", printDocLink), HttpStatus.OK);
    }


    public InsuranceForm mapFormData(InsuranceFormMapper mapper) {
        InsuranceForm insuranceForm = new InsuranceForm();
        try {
            insuranceForm.setFirstName(mapper.getFirstName());
            insuranceForm.setLastName(mapper.getLastName());
            insuranceForm.setDob(mapper.getDob());
            insuranceForm.setGender(mapper.getGender());
            insuranceForm.setPassport(mapper.getPassport());
            insuranceForm.setRace(mapper.getRace());
            insuranceForm.setEthnicity(mapper.getEthnicity());
            insuranceForm.setMobileNumber(mapper.getMobileNumber());
            insuranceForm.setEmail(mapper.getEmail());
            insuranceForm.setState(mapper.getState());
            insuranceForm.setStreet(mapper.getStreet());
            insuranceForm.setCity(mapper.getCity());
            insuranceForm.setZipCode(mapper.getZipCode());
            insuranceForm.setPersonalImage(mapper.getPersonalImage().getBytes(StandardCharsets.UTF_8));
            insuranceForm.setInsuranceIdImage(mapper.getInsuranceIdImage().getBytes(StandardCharsets.UTF_8));

            String middleName, optionalEmail, optionalNumber;
            middleName = mapper.getMiddleName();
            optionalNumber = mapper.getMobileNumber();
            optionalEmail = mapper.getOptionalEmail();
            if(middleName != null) {
                insuranceForm.setMiddleName(middleName);
            }
            if(optionalEmail != null) {
                insuranceForm.setOptionalEmail(optionalEmail);
            }
            if(optionalNumber != null) {
                insuranceForm.setOptionalMobile(optionalNumber);
            }

        } catch (Exception e) {
            LOGGER.error("An error has occurred "+e);
        }
        return insuranceForm;
    }

    public ResponseEntity isAuthenticated(UserDto userDto) {
        User user = userRepository.getUserByCredentials(userDto.getEmail(), userDto.getPassword());
        if(user != null) {
            return new ResponseEntity<>(new DefaultResponse("Success", "User exists", "S02"), HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(new DefaultResponse("Failure", "User does not exists", "F01"), HttpStatus.OK);
        }
    }

    public OrderResponse sendDataToMarques(InsuranceFormMapper insuranceFormMapper) {
        OrderResponse orderResponse = new OrderResponse();
        String newPatientId = null;
        try {
           PatientMapper patientMapper = this.mapPatientObject(insuranceFormMapper);
           ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
           String json = ow.writeValueAsString(patientMapper);
           newPatientId = this.createPatient(json);
           LOGGER.info("New Patient Id is "+newPatientId);
           
          if (orderCreateService.processOrderSpec(newPatientId, insuranceFormMapper)) {
               LOGGER.info("Order spec has created......!");
               String orderNumber = orderCreateService.processOrderTestSrc(newPatientId, insuranceFormMapper);
               LOGGER.info("Order number (Newly created): "+orderNumber);
               boolean result = orderCreateService.processOrderSaveTest(newPatientId, orderNumber, insuranceFormMapper);
               LOGGER.info("Test save result"+ result);
               if(orderCreateService.processOrderDiagnosisCode(newPatientId, orderNumber)) {
                   LOGGER.info("Diagnoses data has also mapped now in order# "+orderNumber);
                    feignClientService.saveSignature("savesignature", orderNumber, "JSON", "1645046790500", this.user, "Abdul");
                    String pdfUrl = "https://marquis.labsvc.net/webreq.cgi?HBHEHHHGHEHMHOBHBGBMAOAOGEHDGIGHGG+"+orderNumber+"+noabn+"+newPatientId;
                    orderResponse.setPdfUrl(pdfUrl);
                    orderResponse.setOrderNumber(orderNumber);
                    orderResponse.setPatientId(newPatientId);
                    LOGGER.info("Pdf url"+pdfUrl);
               }
          }
        }
        catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return orderResponse;
    }

    String createPatient(String json) {
        String newPatientId = null;
        try {
            PatientResponseMapper patientResponseMapper;

            String response = feignClientService.postPatient(this.user, mode, ordnum, housecall, patid, outputformat, json);
            patientResponseMapper = new ObjectMapper().readValue(response, PatientResponseMapper.class);
            LOGGER.info("Response "+response);

            if(patientResponseMapper.getSuccess().equalsIgnoreCase("false") && patientResponseMapper.getMsg().equalsIgnoreCase("Duplicate")) {
                LOGGER.info("Patient Entry is Duplicate");
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
                map.add("mode", mode);
                map.add("patid",patid);
                map.add("user", this.user);
                map.add("json", json);
                map.add("outputformat", outputformat);

                HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<String> finalResponse = restTemplate.postForEntity( "https://marquis.labsvc.net/ordpatins.cgi?FORCE", request , String.class );
                LOGGER.info("Res "+response);
                patientResponseMapper = new ObjectMapper().readValue(finalResponse.getBody(), PatientResponseMapper.class);
                if(patientResponseMapper.getSuccess().equalsIgnoreCase("true") && patientResponseMapper.getMsg().equalsIgnoreCase("OK")) {
                    newPatientId = patientResponseMapper.getPatid();
                }
            } else {

                if(patientResponseMapper.getSuccess().equalsIgnoreCase("true") && patientResponseMapper.getMsg().equalsIgnoreCase("OK")) {
                    newPatientId = patientResponseMapper.getPatid();
                }
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return newPatientId;
    }
    
    public PatientMapper mapPatientObject(InsuranceFormMapper insuranceFormMapper) {
        PatientMapper patientMapper = new PatientMapper();
        try {
            patientMapper.setFirstn(insuranceFormMapper.getFirstName());
            patientMapper.setLastn(insuranceFormMapper.getLastName());
            patientMapper.setCity(insuranceFormMapper.getCity());
            patientMapper.setZip(insuranceFormMapper.getZipCode());
            String date = insuranceFormMapper.getDob().format(DateTimeFormatter.ofPattern("MM/dd/YYYY"));
            LOGGER.info("Date converted "+date);
            patientMapper.setDob(date);
            patientMapper.setSex(insuranceFormMapper.getGender());
            patientMapper.setAddress1(insuranceFormMapper.getStreet());
            patientMapper.setPhone(insuranceFormMapper.getMobileNumber());
        } catch (Exception e) {
          LOGGER.error(e.getMessage(), e);
        }
        return patientMapper;
    }

   void mapOrderObject(InsuranceFormMapper mapper) {
        try {
            OrderMapper order = new OrderMapper();
            order.setUser(this.user);
            order.setNewpat("NO");
            order.setIlname(mapper.getLastName());
            order.setIfname(mapper.getFirstName());
            String gender = mapper.getGender();
            if(gender.equalsIgnoreCase("MALE")) {
                order.setIsex("M");
            }
            if(gender.equalsIgnoreCase("FEMALE")) {
                order.setIsex("F");
            }
        } catch (Exception e) {
            LOGGER.info(e.getMessage(), e);
        }
   }

   public ResponseEntity getDailyOrderStatsForTable() {
       CountDto countDto = new CountDto();
        try {
            LocalDate today = LocalDate.now();
            Date currentDate = java.sql.Date.valueOf(today);
            List<PatientDataMapper> patientData = insuranceFormRepository.getDailyCountData(currentDate);
            for(int i=0; i<patientData.size(); i++) {
                patientData.get(i).setPatientNo(i);
            }

            return new ResponseEntity(patientData, HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
       return new ResponseEntity<>(countDto, HttpStatus.OK);
   }

   public ResponseEntity getDailyOrderStats() {
        CountDto countDto = new CountDto();
        try {
            LocalDate today = LocalDate.now();
            Date currentDate = java.sql.Date.valueOf(today);
            LocalDate endDate = today.minus(1, ChronoUnit.WEEKS);
            Date weekDate = java.sql.Date.valueOf(endDate);

            List<Long> dailyCount = insuranceFormRepository.getDailyCount(currentDate);
            List<Long> weeklyCount = insuranceFormRepository.getWeeklyCount(weekDate, currentDate);
            countDto.setDailyCount(dailyCount.size());
            countDto.setWeeklyCount(weeklyCount.size());

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new ResponseEntity<>(countDto, HttpStatus.OK);
   }
}
