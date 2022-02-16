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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;


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

    @Value("${userToken}")
    String user;


    String mode = "save";
    String ordnum = "NEW";
    String outputformat = "JSON";
    String housecall = "NO";
    String patid = "NEW";

    public ResponseEntity addFormData(InsuranceFormMapper mapper) {
        try {
            InsuranceForm insuranceForm = this.mapFormData(mapper);
//            seleniumService.processForm(mapper);

            String patId = this.sendDataToMarques(mapper);
            if(patId != null) {
                insuranceForm.setPatientId(patId);
                insuranceFormRepository.save(insuranceForm);
                LOGGER.info("Patient Data has saved "+patId);
            }
            LOGGER.info("Data does not save");

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new ResponseEntity<>(new DefaultResponse("Success", "Form Data has save successfully", "S01"), HttpStatus.OK);
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

    public String sendDataToMarques(InsuranceFormMapper insuranceFormMapper) {
        PatientResponseMapper patientResponseMapper = null;
        String newPatientId = null;
        try {
           PatientMapper patientMapper = this.mapPatientObject(insuranceFormMapper);
           ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
           String json = ow.writeValueAsString(patientMapper);
           newPatientId = this.createPatient(json);
           LOGGER.info("New Patient Id is "+newPatientId);


        }
        catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return newPatientId;
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
            }
            else {
                if(patientResponseMapper.getSuccess().equalsIgnoreCase("true") && patientResponseMapper.getMsg().equalsIgnoreCase("OK")) {
                    newPatientId = patientResponseMapper.getPatid();
                }
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return newPatientId;
    }

    public void sendOrderWithPatient(String json, String patid) {
        try{
            MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
            map.add("mode", mode);
            map.add("patid",patid);
            map.add("user", this.user);
            map.add("json", json);
            map.add("outputformat", outputformat);
            map.add("patid", patid);
            map.add("housecall", "NO");

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

    }

    public PatientMapper mapPatientObject(InsuranceFormMapper insuranceFormMapper) {
        PatientMapper patientMapper = new PatientMapper();
        try {
            patientMapper.setFirstn(insuranceFormMapper.getFirstName());
            patientMapper.setLastn(insuranceFormMapper.getLastName());
            patientMapper.setCity(insuranceFormMapper.getCity());
            patientMapper.setZip(insuranceFormMapper.getZipCode());
            String date = insuranceFormMapper.getDob().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            LOGGER.info("Date converted "+date);
            patientMapper.setDob(date);
            patientMapper.setSex(insuranceFormMapper.getGender());

        } catch (Exception e) {
          LOGGER.error(e.getMessage(), e);
        }
        return patientMapper;
    }

   void mapOrderObject(InsuranceFormMapper mapper){
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

}
