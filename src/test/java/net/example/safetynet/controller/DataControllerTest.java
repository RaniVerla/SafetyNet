package net.example.safetynet.controller;

import net.example.safetynet.model.Data;
import net.example.safetynet.model.Firestation;
import net.example.safetynet.model.Medicalrecord;
import net.example.safetynet.model.Person;
import net.example.safetynet.service.DataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DataControllerTest {

    @Mock
    private DataService dataService;

    @InjectMocks
    private DataController dataController;

    private MockMvc mockMvc;
    private Data sampleData;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dataController).build();

        sampleData = new Data(
                List.of(
                        new Person("John",  "Boyd", "1509 Culver St", "Culver", "97451", "841-874-6512", "jaboyd@email.com"),
                        new Person("Jacob", "Boyd", "1509 Culver St", "Culver", "97451", "841-874-6513", "drk@email.com")
                ),
                List.of(
                        new Firestation("1509 Culver St", 3),
                        new Firestation("908 73rd St", 1)
                ),
                List.of(
                        new Medicalrecord("John",  "Boyd", "03/06/1984", new String[]{"aznol:350mg"}, new String[]{"nillacilan"}),
                        new Medicalrecord("Jacob", "Boyd", "03/06/1989", new String[]{"pharmacol:5000mg"}, new String[]{})
                )
        );
    }

    // ======================= Direct controller call tests =======================

    @Test
    @DisplayName("getData — delegates to DataService and returns its response")
    void getData_shouldDelegateToService() {
        doReturn(ResponseEntity.ok(sampleData)).when(dataService).aggregateAndSaveData();

        ResponseEntity<?> response = dataController.getData();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sampleData, response.getBody());
        verify(dataService, times(1)).aggregateAndSaveData();
    }

    @Test
    @DisplayName("getData — returns 500 when service returns internal server error")
    void getData_shouldReturn500WhenServiceFails() {
        doReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error aggregating data"))
                .when(dataService).aggregateAndSaveData();

        ResponseEntity<?> response = dataController.getData();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(dataService, times(1)).aggregateAndSaveData();
    }

    // ======================= MockMvc HTTP-level tests =======================

    @Test
    @DisplayName("GET /v1/data — returns 200 with merged data payload")
    void getData_httpCall_shouldReturn200WithData() throws Exception {
        doReturn(ResponseEntity.ok(sampleData)).when(dataService).aggregateAndSaveData();

        mockMvc.perform(get("/v1/data").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.persons").isArray())
                .andExpect(jsonPath("$.persons.length()").value(2))
                .andExpect(jsonPath("$.persons[0].firstName").value("John"))
                .andExpect(jsonPath("$.persons[0].lastName").value("Boyd"))
                .andExpect(jsonPath("$.persons[1].firstName").value("Jacob"))
                .andExpect(jsonPath("$.firestations").isArray())
                .andExpect(jsonPath("$.firestations.length()").value(2))
                .andExpect(jsonPath("$.firestations[0].address").value("1509 Culver St"))
                .andExpect(jsonPath("$.firestations[0].station").value(3))
                .andExpect(jsonPath("$.medicalrecords").isArray())
                .andExpect(jsonPath("$.medicalrecords.length()").value(2))
                .andExpect(jsonPath("$.medicalrecords[0].firstName").value("John"));
    }

    @Test
    @DisplayName("GET /v1/data — returns 500 when service returns an error")
    void getData_httpCall_shouldReturn500OnError() throws Exception {
        doReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error aggregating data"))
                .when(dataService).aggregateAndSaveData();

        mockMvc.perform(get("/v1/data").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /v1/data — returns empty lists when no data exists")
    void getData_httpCall_shouldReturnEmptyListsWhenNoData() throws Exception {
        Data emptyData = new Data(List.of(), List.of(), List.of());
        doReturn(ResponseEntity.ok(emptyData)).when(dataService).aggregateAndSaveData();

        mockMvc.perform(get("/v1/data").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.persons").isArray())
                .andExpect(jsonPath("$.persons.length()").value(0))
                .andExpect(jsonPath("$.firestations").isArray())
                .andExpect(jsonPath("$.firestations.length()").value(0))
                .andExpect(jsonPath("$.medicalrecords").isArray())
                .andExpect(jsonPath("$.medicalrecords.length()").value(0));
    }

    @Test
    @DisplayName("GET /v1/data — response contains all three top-level keys")
    void getData_httpCall_shouldContainAllThreeKeys() throws Exception {
        doReturn(ResponseEntity.ok(sampleData)).when(dataService).aggregateAndSaveData();

        mockMvc.perform(get("/v1/data").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.persons").exists())
                .andExpect(jsonPath("$.firestations").exists())
                .andExpect(jsonPath("$.medicalrecords").exists());
    }

    @Test
    @DisplayName("GET /v1/data — service is called exactly once per request")
    void getData_httpCall_shouldCallServiceExactlyOnce() throws Exception {
        doReturn(ResponseEntity.ok(sampleData)).when(dataService).aggregateAndSaveData();

        mockMvc.perform(get("/v1/data"));

        verify(dataService, times(1)).aggregateAndSaveData();
    }
}
