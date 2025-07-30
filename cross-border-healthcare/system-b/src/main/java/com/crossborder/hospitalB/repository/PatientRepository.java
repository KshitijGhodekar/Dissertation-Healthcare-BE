package com.crossborder.hospitalB.repository;

import com.crossborder.hospitalB.model.PatientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;
@Repository
@Transactional
public interface PatientRepository extends JpaRepository<PatientEntity, Long> {
    boolean existsByRequestId(String requestId);
    Optional<PatientEntity> findByPatientId(String patientId);
}
