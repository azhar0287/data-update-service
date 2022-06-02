package com.example.dataupdateservice.mappers;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class InsuranceListMapper {
    /*For query object*/
    private String name;
    private String code;

    public InsuranceListMapper(String name, String code) {
        this.name = name;
        this.code = code;
    }
}
