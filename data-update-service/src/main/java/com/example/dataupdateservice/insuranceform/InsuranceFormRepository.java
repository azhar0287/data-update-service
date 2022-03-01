package com.example.dataupdateservice.insuranceform;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Repository
public interface InsuranceFormRepository extends JpaRepository<InsuranceForm, Long> {

    @Query("Select form.id FROM InsuranceForm form where DATE(form.createdAt) =:inputDate")
    List<Long> getDailyCount(@Param("inputDate") Date inputDate);

    @Query("Select form.id FROM InsuranceForm form where DATE(form.createdAt) Between :startDate and :endDate")
    List<Long> getWeeklyCount(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
}
