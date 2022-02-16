package com.example.dataupdateservice.api;

import com.example.dataupdateservice.feign.FeignClientService;
import com.example.dataupdateservice.insuranceform.InsuranceForm;

import com.example.dataupdateservice.insuranceform.InsuranceFormRepository;
import com.example.dataupdateservice.mappers.*;
import com.example.dataupdateservice.response.DefaultResponse;
import com.example.dataupdateservice.user.User;
import com.example.dataupdateservice.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import feign.Response;
import io.cucumber.java.an.E;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.http.Header;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.*;

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


    public ResponseEntity addFormData(InsuranceFormMapper mapper) {
        try {
            InsuranceForm insuranceForm = this.mapFormData(mapper);
            seleniumService.processForm(mapper);

//            String patId = this.sendDataToMarques(mapper);
//            if(patId != null) {
//                insuranceForm.setPatientId(patId);
//                insuranceFormRepository.save(insuranceForm);
//                LOGGER.info("Patient Data has saved "+patId);
//            }
//            LOGGER.info("Data does not save");

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
        try {
           PatientMapper patientMapper = this.mapPatientObject(insuranceFormMapper);
           ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
           String json = ow.writeValueAsString(patientMapper);

           String mode = "save";
           String ordnum = "NEW";
           String outputformat = "JSON";
           String housecall = "NO";
           String patid = "NEW";

//           String response = feignClientService.postPatient(this.user, mode, ordnum, housecall, patid, outputformat, json);
//           patientResponseMapper = new ObjectMapper().readValue(response, PatientResponseMapper.class);
//           LOGGER.info("Response "+response);
//           if(patientResponseMapper.getSuccess().equalsIgnoreCase("false") && patientResponseMapper.getMsg().equalsIgnoreCase("Duplicate")){
            CommentFormDto commentFormDto = new CommentFormDto(mode, patid, this.user, json, outputformat);
            Map<String, String> form = new HashMap<>();

            form.put("mode", mode);
            form.put("patid",patid);
            form.put("user",this.user);
            form.put("json", json);
            form.put("outputformat",outputformat);

          String response =  feignClientService.postDuplicatePatient(form);
                LOGGER.info("Response ");

           // patientResponseMapper = new ObjectMapper().readValue(finalResponse, PatientResponseMapper.class);
     //       }
//            else {
//                patientResponseMapper.getPatid();
//            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return patientResponseMapper.getPatid();
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


}
