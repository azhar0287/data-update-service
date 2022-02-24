package com.example.dataupdateservice.response;

import com.example.dataupdateservice.mappers.PrintDocLink;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class DefaultResponse {
    String responseIdentifier;
    String description;
    String responseCode;
    PrintDocLink printDocLink;


    /*
    * Constructors
     */
    public DefaultResponse(String responseIdentifier, String description, String responseCode) {
        this.responseIdentifier = responseIdentifier;
        this.description = description;
        this.responseCode = responseCode;
    }

    public DefaultResponse(String responseIdentifier, String description, PrintDocLink printDocLink) {
        this.responseIdentifier = responseIdentifier;
        this.description = description;
        this.printDocLink = printDocLink;
    }
}
