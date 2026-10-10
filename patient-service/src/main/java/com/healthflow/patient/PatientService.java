
package com.healthflow.patient;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Transactional
    public Patient createPatient(Patient patient) {
        if (patientRepository.existsByEmail(patient.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A patient with this email already exists"
            );
        }

        return patientRepository.save(patient);
    }

    @Transactional(readOnly = true)
    public Page<Patient> getAllPatients(Pageable pageable) {
        return patientRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Patient getPatientById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Patient not found"
                ));
    }
}
