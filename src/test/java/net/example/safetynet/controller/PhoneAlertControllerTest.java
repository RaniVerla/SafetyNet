package net.example.safetynet.controller;

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

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PhoneAlertControllerTest {

    @Mock
    private FireStationService fireStationService;

    @InjectMocks
    private PhoneAlertController phoneAlertController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(phoneAlertController).build();
    }

    @Test
    @DisplayName("GET /phoneAlert?firestation=1 — returns 200 with list of phone numbers")
    void getPhoneAlert_shouldReturn200WithPhoneNumbers() throws Exception {
        List<String> phones = List.of("841-874-6512", "841-874-8547", "841-874-7462", "841-874-7784");
        when(fireStationService.getPhoneNumbersByStationNumber("1")).thenReturn(phones);

        mockMvc.perform(get("/phoneAlert")
                        .param("firestation", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phones").isArray())
                .andExpect(jsonPath("$.phones.length()").value(4))
                .andExpect(jsonPath("$.phones[0]").value("841-874-6512"))
                .andExpect(jsonPath("$.phones[1]").value("841-874-8547"))
                .andExpect(jsonPath("$.phones[2]").value("841-874-7462"))
                .andExpect(jsonPath("$.phones[3]").value("841-874-7784"));
    }

    @Test
    @DisplayName("GET /phoneAlert?firestation=99 — returns 200 with empty list for unknown station")
    void getPhoneAlert_shouldReturnEmptyListForUnknownStation() throws Exception {
        when(fireStationService.getPhoneNumbersByStationNumber("99"))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/phoneAlert")
                        .param("firestation", "99")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phones").isArray())
                .andExpect(jsonPath("$.phones.length()").value(0));
    }

    @Test
    @DisplayName("GET /phoneAlert?firestation=1 — response contains phones key")
    void getPhoneAlert_shouldContainPhonesKey() throws Exception {
        when(fireStationService.getPhoneNumbersByStationNumber("1"))
                .thenReturn(List.of("841-874-6512"));

        mockMvc.perform(get("/phoneAlert")
                        .param("firestation", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phones").exists());
    }

    @Test
    @DisplayName("GET /phoneAlert?firestation=1 — service is called exactly once with correct station number")
    void getPhoneAlert_shouldCallServiceOnceWithCorrectStation() throws Exception {
        when(fireStationService.getPhoneNumbersByStationNumber("1"))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/phoneAlert").param("firestation", "1"));

        verify(fireStationService, times(1)).getPhoneNumbersByStationNumber("1");
    }

    @Test
    @DisplayName("GET /phoneAlert — missing firestation param returns 400 Bad Request")
    void getPhoneAlert_shouldReturn400WhenParamMissing() throws Exception {
        mockMvc.perform(get("/phoneAlert")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /phoneAlert?firestation=3 — returns single phone number correctly")
    void getPhoneAlert_shouldReturnSinglePhoneNumber() throws Exception {
        when(fireStationService.getPhoneNumbersByStationNumber("3"))
                .thenReturn(List.of("841-874-6512"));

        mockMvc.perform(get("/phoneAlert")
                        .param("firestation", "3")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phones.length()").value(1))
                .andExpect(jsonPath("$.phones[0]").value("841-874-6512"));
    }

    @Test
    @DisplayName("GET /phoneAlert?firestation=1 — returns duplicate phone numbers when persons share a phone")
    void getPhoneAlert_shouldReturnDuplicatePhonesWhenPersonsSharePhone() throws Exception {
        // Three Stelzers all share the same number
        List<String> phones = List.of("841-874-7784", "841-874-7784", "841-874-7784");
        when(fireStationService.getPhoneNumbersByStationNumber("1")).thenReturn(phones);

        mockMvc.perform(get("/phoneAlert")
                        .param("firestation", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phones.length()").value(3));
    }
}
