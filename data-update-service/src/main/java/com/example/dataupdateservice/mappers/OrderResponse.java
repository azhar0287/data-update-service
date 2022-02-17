package com.example.dataupdateservice.mappers;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class OrderResponse {
    String patientId;
    String pdfUrl;
    String orderNumber;
}
