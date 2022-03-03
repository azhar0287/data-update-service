package com.example.dataupdateservice.mappers;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Getter @Setter @NoArgsConstructor
public class PatientDataMapper {
    public String name;
    public int patientNo;
    public String email;
    public String phoneNumber;
    public Date collectionDate;
    public String gender;
    public LocalDate dob;
    public ZonedDateTime cltDate;

    public PatientDataMapper(String firstName, String lastName, String email, String phoneNumber,
                             ZonedDateTime cltDate, String gender, LocalDate dob) throws ParseException {
        this.name = firstName+" "+lastName;
        this.patientNo = patientNo;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.cltDate = cltDate;
        //new SimpleDateFormat("").parse(String.valueOf(cltDate.toInstant()));

        //this.collectionDate = java.util.Date.from(cltDate.toInstant());

        //this.collectionDate = new SimpleDateFormat("dd-M-yyyy hh:mm:ss").parse(String.valueOf(this.collectionDate));
        this.gender = gender;
        this.dob = dob;

    }


}
