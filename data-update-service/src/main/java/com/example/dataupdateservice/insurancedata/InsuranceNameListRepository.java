package com.example.dataupdateservice.insurancedata;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InsuranceNameListRepository extends JpaRepository<InsuranceNameList, Long> {

    @Query("SELECT list.name FROM InsuranceNameList list")
    List<String> getInsuranceList();
}
