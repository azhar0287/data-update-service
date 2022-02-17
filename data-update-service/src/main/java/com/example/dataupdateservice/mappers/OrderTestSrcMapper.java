package com.example.dataupdateservice.mappers;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class OrderTestSrcMapper {

    @JsonProperty("success")
    private Boolean success;
    private String msg;
    private String ordnum;
}
