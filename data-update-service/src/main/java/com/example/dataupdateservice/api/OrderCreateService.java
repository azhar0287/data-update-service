package com.example.dataupdateservice.api;

import com.example.dataupdateservice.mappers.InsuranceFormMapper;
import com.example.dataupdateservice.mappers.PatientResponseMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.an.E;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;

@Service
public class OrderCreateService {

    @Value("${userToken}")
    String user;

    private static final Logger LOGGER = LogManager.getLogger(OrderCreateService.class);



    public static String ORDER_SPEC_URL = "https://marquis.labsvc.net/ordspec.cgi";
    public static String ORDER_TEST_SRC = "https://marquis.labsvc.net/ordtestscr.cgi";
    public static String ORDER_SAVE_TEST_URL = "https://marquis.labsvc.net/ordsave.cgi";
    public static String ORDER_SAVE_DIAG_URL = "https://marquis.labsvc.net/ordspec.cgi";
    public static String ORDER_SAVE_SIGNATURE = "https://marquis.labsvc.net/ordsave.cgi";


    public String sendRequestByRestTemplate(MultiValueMap<String, String> map, String url) {
        String response = "";
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> finalResponse = restTemplate.postForEntity( url, request, String.class );
            LOGGER.info("Res "+finalResponse);
            response = finalResponse.getBody();

        } catch (Exception e ) {
            LOGGER.error(e.getMessage(), e);
        }
        return response;
    }

    boolean processOrderSpec(String patientId, InsuranceFormMapper mapper) {
        try {
            MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
            map.add("mode", "save");
            map.add("patid", patientId);
            map.add("user", this.user);
            //map.add("json", json);
            map.add("outputformat", "JSON");

            map.add("ordernum", "NEW");
            map.add("housecall", "NO");
            map.add("insname1", "COVID19 HRSA Uninsured Testing");
            map.add("ins1", "4051");
            map.add("cltins", "on");
            map.add("ifname", mapper.getFirstName());

            map.add("ilname", mapper.getLastName());
            map.add("idob", mapper.getDob().format(DateTimeFormatter.ofPattern("MM/DD/YYYY")));
            map.add("isex", this.getSexType(mapper.getGender()));
            map.add("insid1", "JSON");
            map.add("outputformat", "11111111"); //Insurance Policy
            map.add("iaddr1", mapper.getStreet());
            map.add("iaddr2", mapper.getStreet());
            map.add("istate", mapper.getState());
            map.add("izip", mapper.getZipCode());

            String response = sendRequestByRestTemplate(map, ORDER_SPEC_URL);
            PatientResponseMapper responseMapper = new ObjectMapper().readValue(response, PatientResponseMapper.class);
            if(responseMapper.getSuccess().equalsIgnoreCase("Success") && responseMapper.getMsg().equalsIgnoreCase("OK")) {
                return true;
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    String getSexType(String sexType) {
        if(sexType.equalsIgnoreCase("MALE")) {
            return "M";
        }
        if(sexType.equalsIgnoreCase("FEMALE")) {
            return "F";
        }
        return "M";
    }



}
