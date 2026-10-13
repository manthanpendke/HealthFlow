package com.healthflow.appointment;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface AppointmentRepository
        extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientIdOrderByAppointmentTimeAsc(
            Long patientId);

    List<Appointment> findByDoctorIdOrderByAppointmentTimeAsc(
            Long doctorId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT a FROM Appointment a
            WHERE a.doctorId = :doctorId
              AND a.appointmentTime = :appointmentTime
              AND a.status = :status
            """)
    List<Appointment> findConflictingAppointments(
            @Param("doctorId") Long doctorId,
            @Param("appointmentTime") LocalDateTime appointmentTime,
            @Param("status") AppointmentStatus status);
}