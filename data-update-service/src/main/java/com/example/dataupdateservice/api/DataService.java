package com.example.dataupdateservice.api;

import com.example.dataupdateservice.feign.FeignClientService;
import com.example.dataupdateservice.insurancedata.InsuranceNameList;
import com.example.dataupdateservice.insurancedata.InsuranceNameListRepository;
import com.example.dataupdateservice.order.PatientOrder;

import com.example.dataupdateservice.order.PatientOrderRepository;
import com.example.dataupdateservice.mappers.*;
import com.example.dataupdateservice.order.OrderMapper;
import com.example.dataupdateservice.response.DefaultResponse;
import com.example.dataupdateservice.user.User;
import com.example.dataupdateservice.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;


@Service
public class DataService {

    private static final Logger LOGGER = LogManager.getLogger(DataService.class);

    @Autowired
    UserRepository userRepository;
    @Autowired
    PatientOrderRepository patientOrderRepository;
    @Autowired
    FeignClientService feignClientService;
    @Autowired
    SeleniumService seleniumService;
    @Autowired
    OrderCreateService orderCreateService;
    @Autowired
    InsuranceNameListRepository insuranceDataRepository;
    @Autowired
    QRCodeGeneratorService qrCodeGeneratorService;


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
       return new ResponseEntity<>(new DefaultResponse("Success", "Firstox Form Data has saved successfully", printDocLink), HttpStatus.OK);
    }

    public ResponseEntity addFormDataForMarquis(InsuranceFormMapper mapper) {
        OrderResponse orderResponse = null;
        PrintDocLink printDocLink = new PrintDocLink();
        try {
            PatientOrder patientOrder = this.mapFormData(mapper);
            orderResponse = this.sendDataToMarques(mapper);
            String patientId = orderResponse.getPatientId();
            if(patientId != null) {
                patientOrder.setPatientId(patientId);
                patientOrder.setOrderNumber(orderResponse.getOrderNumber());
                patientOrderRepository.save(patientOrder);
                LOGGER.info("Patient Data has saved "+patientId);
            }
           printDocLink.setMarquisPdfLink(orderResponse.getPdfUrl());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new ResponseEntity<>(new DefaultResponse("Success", "Form data for Marquis has saved successfully", printDocLink), HttpStatus.OK);
    }


    public PatientOrder mapFormData(InsuranceFormMapper mapper) {
        PatientOrder patientOrder = new PatientOrder();
        try {
            patientOrder.setFirstName(mapper.getFirstName());
            patientOrder.setLastName(mapper.getLastName());
            patientOrder.setDob(mapper.getDob());
            patientOrder.setGender(mapper.getGender());
            patientOrder.setPassport(mapper.getPassport());
            patientOrder.setMobileNumber(mapper.getMobileNumber());
            patientOrder.setEmail(mapper.getEmail());
            patientOrder.setState(mapper.getState());
            patientOrder.setStreet(mapper.getStreet());
            patientOrder.setCity(mapper.getCity());
            patientOrder.setZipCode(mapper.getZipCode());
            patientOrder.setInsuranceName(mapper.getInsuranceName());
            patientOrder.setInsuranceNumber(mapper.getInsuranceNumber());
            patientOrder.setRace("Other");
            patientOrder.setEthnicity("Other");
            String uuid = UUID.randomUUID().toString();
            patientOrder.setUuid(uuid); //Unique submission Id
            //patientOrder.setSubmissionQRC(qrCodeGeneratorService.getQRCodeImage(uuid,250,250));

            patientOrder.setCollectionTime(orderCreateService.getCurrentTimeForSpecificTz());
            patientOrder.setCollectionDate(orderCreateService.getCurrentDateForSpecificTz());

            String middleName, optionalEmail, optionalNumber;
            middleName = mapper.getMiddleName();
            optionalNumber = mapper.getMobileNumber();
            optionalEmail = mapper.getOptionalEmail();

            //Checking Optional fields
            if(mapper.getInsuranceIdImage() != null) {
                patientOrder.setInsuranceIdImage(mapper.getInsuranceIdImage().getBytes(StandardCharsets.UTF_8));
            }
            if(mapper.getPersonalImage() != null) {
                patientOrder.setPersonalImage(mapper.getPersonalImage().getBytes(StandardCharsets.UTF_8));
            }

            if(middleName != null) {
                patientOrder.setMiddleName(middleName);
            }
            if(optionalEmail != null) {
                patientOrder.setOptionalEmail(optionalEmail);
            }
            if(optionalNumber != null) {
                patientOrder.setOptionalMobile(optionalNumber);
            }

        } catch (Exception e) {
            LOGGER.error("An error has occurred ", e);
        }
        return patientOrder;
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
            List<PatientDataMapper> patientData = patientOrderRepository.getDailyCountData(currentDate);
            if(patientData.size() > 0)
            for(int i=0; i<patientData.size(); i++) {
                patientData.get(i).setPatientNo(i+1);
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

            List<Long> dailyCount = patientOrderRepository.getDailyCount(currentDate);
            List<Long> weeklyCount = patientOrderRepository.getWeeklyCount(weekDate, currentDate);
            countDto.setDailyCount(dailyCount.size());
            countDto.setWeeklyCount(weeklyCount.size());

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new ResponseEntity<>(countDto, HttpStatus.OK);
   }

    ResponseEntity fillInsuranceData(InsuranceDataMapper insuranceData) {
        int size = insuranceData.getNames().size();
        try {
            InsuranceNameList insurance;
            Set<InsuranceNameList> entityList = new HashSet<>();
            for (String name:insuranceData.getNames()) {
                insurance = new InsuranceNameList();
                insurance.setName(name.trim());
                insurance.setUuid(UUID.randomUUID().toString());
                entityList.add(insurance);
            }
            insuranceDataRepository.saveAll(entityList);
            LOGGER.info("Insurance data has saved, size: "+size);

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new ResponseEntity(new DefaultResponse("Success","Insurance list has saved size: "+size,"S003"),HttpStatus.OK);
    }

    ResponseEntity getInsuranceList() {
        List<String> insuranceList = new LinkedList<>();
        try {
            insuranceList = insuranceDataRepository.getInsuranceList();
            LOGGER.info("Insurance data has saved, size: "+insuranceList.size());

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new ResponseEntity(insuranceList, HttpStatus.OK);
    }

    ResponseEntity getQRCode() {
        String text="2626262";
        byte[] image;
        String QR_CODE_IMAGE_PATH = "./src/main/resources/QRCode.png";

        try {
            image = qrCodeGeneratorService.getQRCodeImage(text,250,250);

            // Generate and Save Qr Code Image in static/image folder
            qrCodeGeneratorService.generateQRCodeImage(text,250,250, QR_CODE_IMAGE_PATH);
            String qrcode = Base64.getEncoder().encodeToString(image);

            String s = new String(image, StandardCharsets.UTF_8);
            LOGGER.info("Text   "+text);

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
       return null;
    }
}
