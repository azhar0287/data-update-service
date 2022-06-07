package com.example.dataupdateservice.insurancedata;

import com.example.dataupdateservice.mappers.InsuranceListMapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InsuranceNameListRepository extends JpaRepository<InsuranceNameList, Long> {

    @Query("SELECT distinct list.name FROM InsuranceNameList list where list.type is null or list.type like '' ")
    List<String> getInsuranceList();

    @Query("SELECT distinct new com.example.dataupdateservice.mappers.InsuranceListMapper(list.name, list.code) FROM InsuranceNameList list where list.type =:type  ")
    List<InsuranceListMapper> getInsuranceListMarquis(@Param("type") String type);

}
