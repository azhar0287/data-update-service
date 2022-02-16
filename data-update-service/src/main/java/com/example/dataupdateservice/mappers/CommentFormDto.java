package com.example.dataupdateservice.mappers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor

public class CommentFormDto {
    String mode;
    String patid;
    String user;
    String json;
    String outputformat;

    public CommentFormDto(String mode, String patid, String user, String json, String outputformat) {
        this.mode = mode;
        this.patid = patid;
        this.user = user;
        this.json = json;
        this.outputformat = outputformat;
    }
}