package com.example.dataupdateservice.feign;


import com.example.dataupdateservice.mappers.CommentFormDto;
import com.example.dataupdateservice.mappers.PatientResponseMapper;
import feign.Headers;

import feign.Param;
import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
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

    @PostMapping(value="/ordpatins.cgi", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    String saveSignature(@RequestParam("mode") String mode,
                         @RequestParam("ordnum") String ordnum,
                         @RequestParam("outputformat") String outputformat,
                         @RequestParam("_dc") String _dc,
                         @RequestParam("sessionkey") String sessionkey,
                         @RequestParam("signature") String signature);





//    @RequestMapping(method = RequestMethod.POST, value="/services/rest/record/v1/invoice")
//    @Headers({"Content-Type: application/json"})
//    void sendInvoiceToNetSuite(@RequestHeader("Authorization") String token, @RequestBody NetsuiteInvoice netsuiteInvoice);
//
//    @RequestMapping(method = RequestMethod.GET, value="/services/rest/record/v1/vendor")
//    VendorMapper getVendorsLinksMapper(@RequestHeader("Authorization") String token);
//
//    @RequestMapping(method = RequestMethod.GET, value="/services/rest/record/v1/customer")
//    CustomerMapper getCustomersLinksMapper(@RequestHeader("Authorization") String token);
//
//    @RequestMapping(method = RequestMethod.GET, value="/services/rest/record/v1/subsidiary")
//    NetsuiteLinksMapper getSubsidiaryLinksMapper(@RequestHeader("Authorization") String token);
//
//    @RequestMapping(method = RequestMethod.GET, value="/services/rest/record/v1/location")
//    NetsuiteLinksMapper getLocationLinksMapper(@RequestHeader("Authorization") String token);
//
//    @PostMapping(value="/services/rest/record/v1/customer", produces = {"application/json; charset=UTF-8"})
//    @Headers({"Content-Type: application/json"})
//    void createCustomerInNetsuite(@RequestHeader("Authorization") String token, @RequestBody LegalEntityObjectMapper legalEntityObjectMapper) ;
//
//    @RequestMapping(method = RequestMethod.GET, value="/services/rest/record/v1/currency")
//    NetsuiteLinksMapper getCurrencyLinksMapper(@RequestHeader("Authorization") String token);
//
//    @RequestMapping(method = RequestMethod.GET, value="/services/rest/record/v1/customlist_creditstate")
//    NetsuiteLinksMapper getCustomCreditStateLinksMapper(@RequestHeader("Authorization") String token);
//
//    @RequestMapping(method = RequestMethod.GET, value="/services/rest/record/v1/vendorSubsidiaryRelationship")
//    NetsuiteLinksMapper getVendorSubsidiaryRelationLinks(@RequestHeader("Authorization") String token);
//
//    @RequestMapping(method = RequestMethod.GET, value= "{testurl}")
//    NetsuiteLinksMapper getCustomerSubsidiaryRelationLinks(@RequestHeader("Authorization") String token, @PathVariable("testurl") String testUrl);
//
//    @RequestMapping(method = RequestMethod.GET, value="/services/rest/record/v1/invoice?q=trandate%20ON_OR_AFTER%201-JAN-2021")
//    NetsuiteLinksMapper getInvoicesByDate(@RequestHeader("Authorization") String token);
//
//    @RequestMapping(method = RequestMethod.GET, value="/services/rest/record/v1/invoice/{invoiceId}/item")
//    NetsuiteLinksMapper getItemByInvoice(@RequestHeader("Authorization") String token, @PathVariable("invoiceId") Long invoiceId);
//
//
//    /*Custom Record Shipment APIS Implementation*/
//
//    @GetMapping(value="/services/rest/record/v1/customrecord_shipment/eid:{externalId}")
//    @Headers({"Content-Type: application/json"})
//    CustomRecordShipmentMapper getCustomRecordShipment(@RequestHeader("Authorization") String token, @PathVariable("externalId") String externalId);
//
//    @PatchMapping( value="/services/rest/record/v1/customrecord_shipment/eid:{externalId}")
//    Response updateCustomRecordShipment(@RequestHeader("Authorization") String token,
//                                    @PathVariable("externalId") String externalId,
//                                    @RequestBody CustomRecordShipmentMapper shipmentMapper);
//
//    @PostMapping(value="services/rest/record/v1/customrecord_shipment/", produces = {"application/json; charset=UTF-8"})
//    @Headers({"Content-Type: application/json"})
//    void createCustomRecordShipment(@RequestHeader("Authorization") String token, @RequestBody CustomRecordShipmentMapper customRecordShipmentMapper) ;


}