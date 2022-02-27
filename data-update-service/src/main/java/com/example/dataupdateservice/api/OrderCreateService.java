package com.example.dataupdateservice.api;

import com.example.dataupdateservice.mappers.DiagnoseMapper;
import com.example.dataupdateservice.mappers.InsuranceFormMapper;
import com.example.dataupdateservice.mappers.PatientResponseMapper;
import com.example.dataupdateservice.order.OrderMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Service
public class OrderCreateService {

    private static final Logger LOGGER = LogManager.getLogger(OrderCreateService.class);
    public static String ORDER_SPEC_URL = "https://marquis.labsvc.net/ordspec.cgi";
    public static String ORDER_TEST_SRC = "https://marquis.labsvc.net/ordtestscr.cgi";
    public static String ORDER_SAVE_TEST_URL = "https://marquis.labsvc.net/ordsave.cgi";
    //public static String ORDER_SAVE_TEST_URL = "https://marquis.labsvc.net/ordtestscr.cgi";

    public static String ORDER_SAVE_DIAG_URL = "https://marquis.labsvc.net/ordsave.cgi";
    public static String ORDER_SAVE_SIGNATURE = "https://marquis.labsvc.net/ordsave.cgi";

    @Value("${userToken}")
    String user;

    public String getMappedInsuranceToOrder(InsuranceFormMapper mapper) throws JsonProcessingException {
        OrderMapper order = new OrderMapper();
        try {
            order.setUser(this.user);
            order.setNewpat("NO");
            order.setIlname(mapper.getLastName());
            order.setOifname(mapper.getFirstName());
            order.setIsex(this.getSexType(mapper.getGender()));
            order.setIaddr1(mapper.getStreet());
            order.setIaddr2(mapper.getStreet());
            order.setIdob(mapper.getDob().format(DateTimeFormatter.ofPattern("MM/dd/YYYY")));
            order.setIzip(mapper.getZipCode());
            order.setIphone(mapper.getMobileNumber());
            order.setIstate(mapper.getState());
            order.setCltins("on");
            order.setCltins2("on");
            order.setIns1("4051");
            order.setInsname1("COVID19 HRSA Uninsured Testing");
            order.setInsid1("111111111");
            order.setRelation("SE");
            order.setBillclient("NO");

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        return ow.writeValueAsString(order);
    }

    public String getMappedOrderDiag() throws JsonProcessingException {
        DiagnoseMapper diagnoseMapper = new DiagnoseMapper();
        try {
            diagnoseMapper.setUser(this.user);
            diagnoseMapper.setCltnum("1551");
            diagnoseMapper.setDiag1("Z20.828");
            diagnoseMapper.setDiag2("R05.9");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        return ow.writeValueAsString(diagnoseMapper);
    }

    public String getCurrentDate() {
        String dateString = "";
        try {
            LocalDate date = LocalDate.now(); // Gets the current date
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM-dd-YYYY");
            dateString = date.format(dateFormatter);
            LOGGER.info("Current date for order collection/etc: "+dateString);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return dateString;
    }

    public String getCurrentTime() {
        String timeString = "";
        try {
            LocalTime time = LocalTime.now(); // Gets the current time
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            timeString = time.format(timeFormatter);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return timeString;
    }

    public String sendRequestByRestTemplate(MultiValueMap<String, String> map, String url) {
        String response = "";
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> finalResponse = restTemplate.postForEntity( url, request, String.class );
            LOGGER.info("Res "+finalResponse.getBody());
            response = finalResponse.getBody();

        } catch (Exception e ) {
            LOGGER.error(e.getMessage(), e);
        }
        return response;
    }

    boolean processOrderSaveTest(String patientId, String orderNumber, InsuranceFormMapper mapper) {
        try {
            MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
            map.add("billtype", "4051");
            map.add("ordphys", "1588");
            map.add("ordclt", "1551");
            map.add("orderdate", this.getCurrentDate());
            map.add("user", "HBHEHGHHHFHOHGBHBGBMAOAOGEHDGIGHGG");
            map.add("patid", patientId);
            map.add("collectdt", this.getCurrentDate());
            map.add("fasting", "N");
            map.add("json", "{\"tests\":[\"PCRW\"],\"frequency\":[\"One Time\"],\"startdt\":[\"02/18/2022\"],\"stopdt\":[\"02/18/2022\"]}");
            map.add("mode", "savetest");
            map.add("collecttime", "15:15");
            map.add("ordertime", "15:18");
            map.add("ordnum", orderNumber);
            map.add("ordquest_webid","NOTSET");
            map.add("housecall","NO");
            map.add("outputformat","JSON");
            map.add("testcode","PCRW");
            map.add("call","N");
            map.add("fax","N");
            map.add("source","----");
            map.add("comment",mapper.getEmail());
            String response = sendRequestByRestTemplate(map, ORDER_SAVE_TEST_URL);
            if(response != "") {
                PatientResponseMapper responseMapper = new ObjectMapper().readValue(response, PatientResponseMapper.class);
                if(responseMapper.getSuccess().equalsIgnoreCase("true") && responseMapper.getMsg().equalsIgnoreCase("OK")) {
                    return true;
                }
            }
            else {
                LOGGER.info("Order has not created");
                return false;
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }


    boolean processOrderSpec(String patientId, InsuranceFormMapper mapper) {
        try {
            MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
            map.add("mode", "save");
            map.add("patid", patientId);
            map.add("user", this.user);
            //map.add("json", json);
            map.add("ordernum", "NEW");
            map.add("housecall", "NO");
            map.add("insname1", "COVID19 HRSA Uninsured Testing");
            map.add("ins1", "4051");
            map.add("cltins", "on");
            map.add("ifname", mapper.getFirstName());

            map.add("ilname", mapper.getLastName());
            map.add("idob", mapper.getDob().format(DateTimeFormatter.ofPattern("MM/dd/YYYY")));
            map.add("isex", this.getSexType(mapper.getGender()));
            map.add("outputformat", "JSON");
            map.add("insid1", "11111111"); //Insurance Policy
            map.add("iaddr1", mapper.getStreet());
            map.add("iaddr2", mapper.getCity());
            map.add("istate", mapper.getState());
            map.add("izip", mapper.getZipCode());
            map.add("relation","SE");
            String response = sendRequestByRestTemplate(map, ORDER_SPEC_URL);
            if(response != "") {
                PatientResponseMapper responseMapper = new ObjectMapper().readValue(response, PatientResponseMapper.class);
                if(responseMapper.getSuccess().equalsIgnoreCase("true") && responseMapper.getMsg().equalsIgnoreCase("OK")) {
                    return true;
                }
            }
            else {
                LOGGER.info("Order has not created");
                return false;
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    String processOrderTestSrc(String patientId, InsuranceFormMapper mapper) {
        String ordNum = "";
        try {

            MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
            map.add("mode", "save");
            map.add("patid", patientId);
            map.add("json", this.getMappedInsuranceToOrder(mapper));
            map.add("ordernum", "NEW");
            map.add("user", this.user);
            map.add("collectdt", this.getCurrentDate());
            map.add("collecttime", this.getCurrentTime());
            map.add("orderdate", this.getCurrentDate());
            map.add("ordertime", this.getCurrentTime());
            map.add("ordphys", "1588");
            map.add("ordclt", "1551");
            map.add("source", "Blood");

            String response = sendRequestByRestTemplate(map, ORDER_TEST_SRC);
            if(response != "") {
                String[] parts = response.split("'");
                if(parts[1].equalsIgnoreCase("OK")) {
                    ordNum = parts[3];
                }
            }
            else {
                LOGGER.info("Order has not created");
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ordNum;
    }

    boolean processOrderDiagnosisCode(String patientId, String orderNumber) {
        try {
            MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
            map.add("mode", "savediag");
            map.add("patid", patientId);
            map.add("json", this.getMappedOrderDiag());
            map.add("outputformat", "JSON");
            map.add("housecall", "NO");
            map.add("ordnum", orderNumber);

            String response = sendRequestByRestTemplate(map, ORDER_SAVE_DIAG_URL);
            if(response != "") {
                PatientResponseMapper responseMapper = new ObjectMapper().readValue(response, PatientResponseMapper.class);
                if(responseMapper.getSuccess().equalsIgnoreCase("true") && responseMapper.getMsg().equalsIgnoreCase("OK")) {
                    return true;
                }
            }
            else {
                LOGGER.info("Order has not created");
                return false;
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