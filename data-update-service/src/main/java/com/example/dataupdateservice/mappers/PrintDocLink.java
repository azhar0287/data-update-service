package com.example.dataupdateservice.mappers;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class PrintDocLink {
    String marquisPdfLink;
    String FirstToxPdfLink;
    String FirstToxLabelLink;
    String uuid;
    byte[] pdf;
}
