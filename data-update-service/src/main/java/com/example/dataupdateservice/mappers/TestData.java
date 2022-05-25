package com.example.dataupdateservice.mappers;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter @NoArgsConstructor
public class TestData {

    public List<String> tests = new ArrayList<>();
    public List<String> frequency = new ArrayList<>();
    public List<String> startdt = new ArrayList<>();
    public List<String> stopdt = new ArrayList<>();


}
