
package com.healthflow.patient;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PatientController.class)
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PatientService patientService;

    @Test
    void healthEndpointShouldReturnUp() throws Exception {
        mockMvc.perform(get("/api/v1/patients/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").value("patient-service"))
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void invalidPatientInputShouldReturnBadRequest() throws Exception {
        String requestBody = """
                {
                    "firstName": "",
                    "lastName": "Demo",
                    "email": "invalid-email",
                    "dateOfBirth": "1995-06-15"
                }
                """;

        mockMvc.perform(post("/api/v1/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/api/v1/patients"));
    }

    @Test
    void missingPatientShouldReturnNotFound() throws Exception {
        given(patientService.getPatientById(99999L))
                .willThrow(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Patient not found"));

        mockMvc.perform(get("/api/v1/patients/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Patient not found"))
                .andExpect(jsonPath("$.path").value("/api/v1/patients/99999"));
    }

    @Test
    void duplicateEmailShouldReturnConflict() throws Exception {
        String requestBody = """
                {
                    "firstName": "Asha",
                    "lastName": "Demo",
                    "email": "asha.demo@example.com",
                    "dateOfBirth": "1995-06-15"
                }
                """;

        given(patientService.createPatient(any(Patient.class)))
                .willThrow(new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "A patient with this email already exists"));

        mockMvc.perform(post("/api/v1/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("A patient with this email already exists"))
                .andExpect(jsonPath("$.path").value("/api/v1/patients"));
    }
}
