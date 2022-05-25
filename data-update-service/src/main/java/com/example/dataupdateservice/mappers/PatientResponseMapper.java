package com.example.dataupdateservice.mappers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.SafeHtml;
@JsonIgnoreProperties
@Getter @Setter @NoArgsConstructor
public class PatientResponseMapper {
    private String success;
    private String msg;
    private String patid;
    private String ordnum;
    private String testcode;


}
