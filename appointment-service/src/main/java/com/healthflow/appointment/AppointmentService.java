package com.healthflow.appointment;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final RestClient restClient;
    private final String patientServiceUrl;
    private final String doctorServiceUrl;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            RestClient.Builder restClientBuilder,
            @Value("${services.patient.url:http://localhost:8082}")
            String patientServiceUrl,
            @Value("${services.doctor.url:http://localhost:8083}")
            String doctorServiceUrl) {

        this.appointmentRepository = appointmentRepository;
        this.restClient = restClientBuilder.build();
        this.patientServiceUrl = patientServiceUrl;
        this.doctorServiceUrl = doctorServiceUrl;
    }

    @Transactional
    public Appointment createAppointment(AppointmentRequest request) {
        validateReference(
                patientServiceUrl + "/api/v1/patients/" + request.patientId(),
                "Patient");

        validateReference(
                doctorServiceUrl + "/api/v1/doctors/" + request.doctorId(),
                "Doctor");

        List<Appointment> conflicts =
                appointmentRepository.findConflictingAppointments(
                        request.doctorId(),
                        request.appointmentTime(),
                        AppointmentStatus.SCHEDULED);

        if (!conflicts.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Doctor already has an appointment at this time");
        }

        Appointment appointment = new Appointment(
                request.patientId(),
                request.doctorId(),
                request.appointmentTime(),
                request.reason());

        return appointmentRepository.save(appointment);
    }

    @Transactional(readOnly = true)
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Appointment getAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Appointment not found"));
    }

    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsByPatient(Long patientId) {
        return appointmentRepository
                .findByPatientIdOrderByAppointmentTimeAsc(patientId);
    }

    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsByDoctor(Long doctorId) {
        return appointmentRepository
                .findByDoctorIdOrderByAppointmentTimeAsc(doctorId);
    }

    @Transactional
    public Appointment cancelAppointment(Long id) {
        Appointment appointment = getAppointmentById(id);

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only scheduled appointments can be cancelled");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        return appointmentRepository.save(appointment);
    }

    private void validateReference(String url, String resourceName) {
        try {
            restClient.get()
                    .uri(url)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    resourceName
                            + " does not exist or its service is unavailable");
        }
    }
}