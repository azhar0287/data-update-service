package com.example.dataupdateservice.insuranceform;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface InsuranceFormRepository extends JpaRepository<InsuranceForm, Long> {

    @Query("Select COUNT(form.createdAt) FROM InsuranceForm form group by form.createdAt")
    int getDailyCount();
}
