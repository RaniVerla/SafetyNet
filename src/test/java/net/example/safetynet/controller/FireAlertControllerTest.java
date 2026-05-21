package net.example.safetynet.controller;

import net.example.safetynet.model.FireAlertResponse;
import net.example.safetynet.service.FireStationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class FireAlertControllerTest {

    @Mock
    private FireStationService fireStationService;

    @InjectMocks
    private FireAlertController fireAlertController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(fireAlertController).build();
    }

    @Test
    @DisplayName("GET /fire?address=1509 Culver St — returns 200 with station number and residents")
    void getResidentsByAddress_shouldReturn200WithFullResponse() throws Exception {
        FireAlertResponse response = new FireAlertResponse(3, List.of(
                new FireAlertResponse.FireAlertResident("John", "Boyd", "841-874-6512", 42,
                        List.of("aznol:350mg", "hydrapermazol:100mg"), List.of("nillacilan")),
                new FireAlertResponse.FireAlertResident("Jacob", "Boyd", "841-874-6513", 37,
                        List.of("pharmacol:5000mg"), List.of()),
                new FireAlertResponse.FireAlertResident("Tenley", "Boyd", "841-874-6512", 14,
                        List.of(), List.of("peanut"))
        ));
        when(fireStationService.getResidentsByAddress("1509 Culver St")).thenReturn(response);

        mockMvc.perform(get("/fire")
                        .param("address", "1509 Culver St")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stationNumber").value(3))
                .andExpect(jsonPath("$.residents").isArray())
                .andExpect(jsonPath("$.residents.length()").value(3))
                .andExpect(jsonPath("$.residents[0].firstName").value("John"))
                .andExpect(jsonPath("$.residents[0].lastName").value("Boyd"))
                .andExpect(jsonPath("$.residents[0].phone").value("841-874-6512"))
                .andExpect(jsonPath("$.residents[0].age").value(42))
                .andExpect(jsonPath("$.residents[0].medications.length()").value(2))
                .andExpect(jsonPath("$.residents[0].allergies[0]").value("nillacilan"));
    }

    @Test
    @DisplayName("GET /fire?address=Unknown — returns 200 with stationNumber=0 and empty residents")
    void getResidentsByAddress_shouldReturnEmptyForUnknownAddress() throws Exception {
        when(fireStationService.getResidentsByAddress("Unknown"))
                .thenReturn(new FireAlertResponse(0, new ArrayList<>()));

        mockMvc.perform(get("/fire")
                        .param("address", "Unknown")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stationNumber").value(0))
                .andExpect(jsonPath("$.residents").isArray())
                .andExpect(jsonPath("$.residents.length()").value(0));
    }

    @Test
    @DisplayName("GET /fire?address=1509 Culver St — response contains stationNumber and residents keys")
    void getResidentsByAddress_shouldContainRequiredKeys() throws Exception {
        when(fireStationService.getResidentsByAddress("1509 Culver St"))
                .thenReturn(new FireAlertResponse(3, new ArrayList<>()));

        mockMvc.perform(get("/fire")
                        .param("address", "1509 Culver St")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stationNumber").exists())
                .andExpect(jsonPath("$.residents").exists());
    }

    @Test
    @DisplayName("GET /fire?address=1509 Culver St — each resident has all required fields")
    void getResidentsByAddress_shouldReturnAllResidentFields() throws Exception {
        FireAlertResponse response = new FireAlertResponse(3, List.of(
                new FireAlertResponse.FireAlertResident("John", "Boyd", "841-874-6512", 42,
                        List.of("aznol:350mg"), List.of("nillacilan"))
        ));
        when(fireStationService.getResidentsByAddress("1509 Culver St")).thenReturn(response);

        mockMvc.perform(get("/fire")
                        .param("address", "1509 Culver St")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.residents[0].firstName").exists())
                .andExpect(jsonPath("$.residents[0].lastName").exists())
                .andExpect(jsonPath("$.residents[0].phone").exists())
                .andExpect(jsonPath("$.residents[0].age").exists())
                .andExpect(jsonPath("$.residents[0].medications").exists())
                .andExpect(jsonPath("$.residents[0].allergies").exists());
    }

    @Test
    @DisplayName("GET /fire?address=1509 Culver St — service is called exactly once with correct address")
    void getResidentsByAddress_shouldCallServiceOnceWithCorrectAddress() throws Exception {
        when(fireStationService.getResidentsByAddress("1509 Culver St"))
                .thenReturn(new FireAlertResponse(3, new ArrayList<>()));

        mockMvc.perform(get("/fire").param("address", "1509 Culver St"));

        verify(fireStationService, times(1)).getResidentsByAddress("1509 Culver St");
    }

    @Test
    @DisplayName("GET /fire — missing address param returns 400 Bad Request")
    void getResidentsByAddress_shouldReturn400WhenParamMissing() throws Exception {
        mockMvc.perform(get("/fire").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /fire?address=908 73rd St — medications and allergies arrays are correct")
    void getResidentsByAddress_shouldReturnCorrectMedicationsAndAllergies() throws Exception {
        FireAlertResponse response = new FireAlertResponse(1, List.of(
                new FireAlertResponse.FireAlertResident("Jamie", "Peters", "841-874-7462", 44,
                        List.of(), List.of())
        ));
        when(fireStationService.getResidentsByAddress("908 73rd St")).thenReturn(response);

        mockMvc.perform(get("/fire")
                        .param("address", "908 73rd St")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stationNumber").value(1))
                .andExpect(jsonPath("$.residents[0].firstName").value("Jamie"))
                .andExpect(jsonPath("$.residents[0].medications").isArray())
                .andExpect(jsonPath("$.residents[0].medications.length()").value(0))
                .andExpect(jsonPath("$.residents[0].allergies").isArray())
                .andExpect(jsonPath("$.residents[0].allergies.length()").value(0));
    }
}
