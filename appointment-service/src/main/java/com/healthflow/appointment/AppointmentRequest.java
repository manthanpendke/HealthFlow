package com.healthflow.appointment;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AppointmentRequest(

        @NotNull
        @Positive
        Long patientId,

        @NotNull
        @Positive
        Long doctorId,

        @NotNull
        @Future
        LocalDateTime appointmentTime,

        @Size(max = 500)
        String reason
) {
}