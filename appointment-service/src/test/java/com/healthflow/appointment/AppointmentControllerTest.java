
package com.healthflow.appointment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(AppointmentController.class)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AppointmentService appointmentService;

    @Test
    void healthEndpointReturnsUp() throws Exception {
        mockMvc.perform(get("/api/v1/appointments/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").value("appointment-service"))
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void getAllAppointmentsReturnsOk() throws Exception {
        when(appointmentService.getAllAppointments()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/appointments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getAppointmentByIdReturnsAppointment() throws Exception {
        Appointment appointment = new Appointment(
                1L, 1L, LocalDateTime.now().plusDays(1), "Routine consultation");

        when(appointmentService.getAppointmentById(1L)).thenReturn(appointment);

        mockMvc.perform(get("/api/v1/appointments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(1))
                .andExpect(jsonPath("$.doctorId").value(1))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    void createAppointmentReturnsCreated() throws Exception {
        LocalDateTime appointmentTime = LocalDateTime.now()
                .plusDays(2).withNano(0);

        AppointmentRequest request = new AppointmentRequest(
                1L, 1L, appointmentTime, "Routine consultation");

        Appointment appointment = new Appointment(
                1L, 1L, appointmentTime, "Routine consultation");

        when(appointmentService.createAppointment(any(AppointmentRequest.class)))
                .thenReturn(appointment);

        mockMvc.perform(post("/api/v1/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.patientId").value(1))
                .andExpect(jsonPath("$.doctorId").value(1))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    void cancelAppointmentReturnsUpdatedAppointment() throws Exception {
        Appointment appointment = new Appointment(
                1L, 1L, LocalDateTime.now().plusDays(1), "Routine consultation");
        appointment.setStatus(AppointmentStatus.CANCELLED);

        when(appointmentService.cancelAppointment(1L)).thenReturn(appointment);

        mockMvc.perform(patch("/api/v1/appointments/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}
