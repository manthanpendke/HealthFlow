package com.healthflow.doctor;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:doctortest;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
class DoctorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createAndRetrieveDoctor() throws Exception {
        String request = """
                {
                    "fullName": "Dr. Priya Sharma",
                    "email": "priya.sharma@example.com",
                    "specialty": "Cardiology",
                    "consultationFee": 750.00
                }
                """;

        mockMvc.perform(post("/api/v1/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fullName").value("Dr. Priya Sharma"))
                .andExpect(jsonPath("$.specialty").value("Cardiology"));
    }

    @Test
    void rejectInvalidDoctorRequest() throws Exception {
        String request = """
                {
                    "fullName": "",
                    "email": "not-an-email",
                    "specialty": "",
                    "consultationFee": -10
                }
                """;

        mockMvc.perform(post("/api/v1/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnNotFoundForMissingDoctor() throws Exception {
        mockMvc.perform(get("/api/v1/doctors/9999"))
                .andExpect(status().isNotFound());
    }
}