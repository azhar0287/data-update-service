package com.example.dataupdateservice.insuranceform;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import javax.persistence.*;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Date;

@Entity
@Getter @Setter @ToString @NoArgsConstructor
public class InsuranceForm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    private String patientId;
    private String orderNumber;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    private byte[] personalImage;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    private byte[] insuranceIdImage;

    @JsonIgnore
    @CreationTimestamp
    private ZonedDateTime createdAt; //our system created datetime i.e. for record

    @JsonIgnore
    @UpdateTimestamp
    private ZonedDateTime updatedAt;
    
    private String collectionDate;
    private String collectionTime;
}
