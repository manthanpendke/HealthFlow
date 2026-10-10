
package com.healthflow.patient;

import org.springframework.web.server.ResponseStatusException;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/v1/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
                "service", "patient-service",
                "status", "UP"
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Patient createPatient(@Valid @RequestBody PatientRequest request) {
        Patient patient = new Patient(
                request.firstName(),
                request.lastName(),
                request.email(),
                request.dateOfBirth()
        );

        return patientService.createPatient(patient);
    }

    @GetMapping
public Page<Patient> getAllPatients(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "asc") String direction) {

    if (page < 0 || size < 1 || size > 100) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Page must be non-negative and size must be between 1 and 100"
        );
    }

    if (!sortBy.equals("id")
            && !sortBy.equals("firstName")
            && !sortBy.equals("lastName")
            && !sortBy.equals("email")) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Unsupported sort field"
        );
    }

    Sort sort;
    if (direction.equalsIgnoreCase("asc")) {
        sort = Sort.by(sortBy).ascending();
    } else if (direction.equalsIgnoreCase("desc")) {
        sort = Sort.by(sortBy).descending();
    } else {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Direction must be asc or desc"
        );
    }

    Pageable pageable = PageRequest.of(page, size, sort);
    return patientService.getAllPatients(pageable);
}

    @GetMapping("/{id}")
    public Patient getPatientById(@PathVariable Long id) {
        return patientService.getPatientById(id);
    }
}
