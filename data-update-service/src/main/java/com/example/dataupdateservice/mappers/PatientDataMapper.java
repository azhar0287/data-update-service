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
    public String collectionDate;
    public String collectionTime;
    public String gender;
    public String dob;
    public ZonedDateTime cltDate;

    public PatientDataMapper(String firstName, String lastName, String email, String phoneNumber,
                             String collectionDate, String collectionTime, String gender, LocalDate dob) {
        this.name = firstName+" "+lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.collectionDate = collectionDate;
        this.collectionTime = collectionTime;
        this.gender = gender;
        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/YYYY");
        this.dob = dob.format(DateTimeFormatter.ofPattern("MM/dd/YYYY"));
    }

}
