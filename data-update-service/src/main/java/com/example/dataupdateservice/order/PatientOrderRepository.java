package com.example.dataupdateservice.order;

import com.example.dataupdateservice.mappers.PatientDataMapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface PatientOrderRepository extends JpaRepository<PatientOrder, Long> {

    @Query("Select form.id FROM PatientOrder form where DATE(form.createdAt) =:inputDate")
    List<Long> getDailyCount(@Param("inputDate") Date inputDate);

    @Query("Select form.id FROM PatientOrder form where DATE(form.createdAt) Between :startDate and :endDate")
    List<Long> getWeeklyCount(@Param("startDate") Date startDate, @Param("endDate") Date endDate);


    @Query("Select new com.example.dataupdateservice.mappers.PatientDataMapper(form.firstName, form.lastName, form.email, form.mobileNumber, form.collectionDate, form.collectionTime, form.gender, form.dob) FROM PatientOrder form where DATE(form.createdAt)  =:startDate")
    List<PatientDataMapper> getDailyCountData(@Param("startDate") Date startDate);

    @Query("SELECT order FROM PatientOrder order where order.uuid =:uuid")
    PatientOrder getOrderByUuid(@Param("uuid") String uuid);
}
