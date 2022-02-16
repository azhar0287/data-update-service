package com.example.dataupdateservice.insuranceform;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InsuranceFormRepository extends JpaRepository<InsuranceForm, Long> {

}
