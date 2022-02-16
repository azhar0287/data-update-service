package com.example.dataupdateservice.mappers;

import io.cucumber.java.mk_latn.No;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor

public class PatientMapper {
    private String docchart;
    private String firstn;
    private String lastn;
    private String mi;
    private String address1;
    private String city;
    private String state;
    private String zip;
    private String phone;
    private String dob;
    private String sex;
    private String room;
}
