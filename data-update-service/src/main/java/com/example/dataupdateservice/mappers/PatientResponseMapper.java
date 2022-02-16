package com.example.dataupdateservice.mappers;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.SafeHtml;

@Getter @Setter @NoArgsConstructor
public class PatientResponseMapper {
    private String success;
    private String msg;
    private String patid;
}
