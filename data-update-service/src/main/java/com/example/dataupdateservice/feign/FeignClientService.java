package com.example.dataupdateservice.feign;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.Map;



@FeignClient(name = "Marquis",url ="https://marquis.labsvc.net", configuration = CustomConfig.class)
@Service
public interface FeignClientService {
    @PostMapping(value="/ordpatins.cgi", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    String postPatient(@RequestParam("user") String user, @RequestParam("mode") String mode, @RequestParam("ordnum") String ordnum,
                       @RequestParam("housecall") String housecall, @RequestParam("patid") String patid,
                       @RequestParam("outputformat") String outputformat, @RequestParam("json") String json);


    @PostMapping(value="/ordpatins.cgi?FORCE", produces = MediaType.TEXT_PLAIN_VALUE, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    String postDuplicatePatient(@RequestBody Map<String, String> body);

    @PostMapping(value="/ordsave.cgi", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    String saveSignature(@RequestParam("mode") String mode,
                         @RequestParam("ordnum") String ordnum,
                         @RequestParam("outputformat") String outputformat,
                         @RequestParam("_dc") String _dc,
                         @RequestParam("sessionkey") String sessionkey,
                         @RequestParam("signature") String signature);


    @PostMapping(value="/inslist.cgi", produces = MediaType.TEXT_PLAIN_VALUE, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    String getInsuranceDetails(@RequestBody MultiValueMap<String, String> body);

}