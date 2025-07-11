package com.crossborder.hospitalA.repository;

import com.crossborder.hospitalA.model.PatientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRepository extends JpaRepository<PatientEntity, Long> {

    PatientEntity findByPatientId(String patientId);
}
