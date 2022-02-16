package com.example.dataupdateservice.mappers;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor
public class InsuranceFormMapper {

    private Long id;
    private String firstName;
    private String lastName;
    private String middleName;
    private String email;
    private String optionalEmail;
    private String mobileNumber;
    private String passport;
    private String state;
    private String city;
    private String zipCode;
    private String race;
    private String ethnicity;
    private String gender;
    private LocalDate dob;
    private String language;
    private String country;
    private String password;
    private String street;
    private String optionalMobile;
    private String personalImage;
    private String insuranceIdImage;

}
