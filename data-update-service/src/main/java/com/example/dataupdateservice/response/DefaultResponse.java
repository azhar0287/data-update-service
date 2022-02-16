package com.example.dataupdateservice.response;

public class DefaultResponse {
    String responseIdentifier;
    String description;
    String responseCode;


    /*
    * Constructors
     */
    public DefaultResponse(String responseIdentifier, String description, String responseCode) {
        this.responseIdentifier = responseIdentifier;
        this.description = description;
        this.responseCode = responseCode;
    }

    public DefaultResponse(){}

    public DefaultResponse(String responseIdentifier) {
        this.responseIdentifier = responseIdentifier;
    }

    public String getResponseIdentifier() {
        return responseIdentifier;
    }

    public void setResponseIdentifier(String responseIdentifier) {
        this.responseIdentifier = responseIdentifier;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }
}
