package net.example.safetynet.controller;

import net.example.safetynet.model.FloodHousehold;
import net.example.safetynet.model.FloodResident;
import net.example.safetynet.model.FloodResponse;
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
class FloodControllerTest {

    @Mock
    private FireStationService fireStationService;

    @InjectMocks
    private FloodController floodController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(floodController).build();
    }

    private FloodResponse buildSampleResponse() {
        FloodResident john = new FloodResident("John", "Boyd", "841-874-6512", 42,
                List.of("aznol:350mg", "hydrapermazol:100mg"), List.of("nillacilan"));
        FloodResident jacob = new FloodResident("Jacob", "Boyd", "841-874-6513", 37,
                List.of("pharmacol:5000mg"), List.of());

        FloodResident reginold = new FloodResident("Reginold", "Walker", "841-874-8547", 46,
                List.of("thradox:700mg"), List.of("illisoxian"));

        return new FloodResponse(List.of(
                new FloodHousehold("1509 Culver St", List.of(john, jacob)),
                new FloodHousehold("908 73rd St",    List.of(reginold))
        ));
    }

    @Test
    @DisplayName("GET /flood/stations?stations=1,3 — returns 200 with households grouped by address")
    void getHouseholdsByStations_shouldReturn200WithHouseholds() throws Exception {
        when(fireStationService.getHouseholdsByStations("1,3")).thenReturn(buildSampleResponse());

        mockMvc.perform(get("/flood/stations")
                        .param("stations", "1,3")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.households").isArray())
                .andExpect(jsonPath("$.households.length()").value(2))
                .andExpect(jsonPath("$.households[0].address").value("1509 Culver St"))
                .andExpect(jsonPath("$.households[0].residents").isArray())
                .andExpect(jsonPath("$.households[0].residents.length()").value(2))
                .andExpect(jsonPath("$.households[0].residents[0].firstName").value("John"))
                .andExpect(jsonPath("$.households[0].residents[0].lastName").value("Boyd"))
                .andExpect(jsonPath("$.households[0].residents[0].phone").value("841-874-6512"))
                .andExpect(jsonPath("$.households[0].residents[0].age").value(42))
                .andExpect(jsonPath("$.households[0].residents[0].medications.length()").value(2))
                .andExpect(jsonPath("$.households[0].residents[0].allergies[0]").value("nillacilan"))
                .andExpect(jsonPath("$.households[1].address").value("908 73rd St"))
                .andExpect(jsonPath("$.households[1].residents.length()").value(1));
    }

    @Test
    @DisplayName("GET /flood/stations?stations=99 — returns 200 with empty households list")
    void getHouseholdsByStations_shouldReturnEmptyForUnknownStation() throws Exception {
        when(fireStationService.getHouseholdsByStations("99"))
                .thenReturn(new FloodResponse(new ArrayList<>()));

        mockMvc.perform(get("/flood/stations")
                        .param("stations", "99")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.households").isArray())
                .andExpect(jsonPath("$.households.length()").value(0));
    }

    @Test
    @DisplayName("GET /flood/stations?stations=1 — response contains households key")
    void getHouseholdsByStations_shouldContainHouseholdsKey() throws Exception {
        when(fireStationService.getHouseholdsByStations("1"))
                .thenReturn(new FloodResponse(new ArrayList<>()));

        mockMvc.perform(get("/flood/stations")
                        .param("stations", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.households").exists());
    }

    @Test
    @DisplayName("GET /flood/stations?stations=1 — each resident has all required fields")
    void getHouseholdsByStations_shouldReturnAllResidentFields() throws Exception {
        FloodResident resident = new FloodResident("John", "Boyd", "841-874-6512", 42,
                List.of("aznol:350mg"), List.of("nillacilan"));
        when(fireStationService.getHouseholdsByStations("1"))
                .thenReturn(new FloodResponse(List.of(new FloodHousehold("1509 Culver St", List.of(resident)))));

        mockMvc.perform(get("/flood/stations")
                        .param("stations", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.households[0].residents[0].firstName").exists())
                .andExpect(jsonPath("$.households[0].residents[0].lastName").exists())
                .andExpect(jsonPath("$.households[0].residents[0].phone").exists())
                .andExpect(jsonPath("$.households[0].residents[0].age").exists())
                .andExpect(jsonPath("$.households[0].residents[0].medications").exists())
                .andExpect(jsonPath("$.households[0].residents[0].allergies").exists());
    }

    @Test
    @DisplayName("GET /flood/stations?stations=1,3 — service is called exactly once with correct param")
    void getHouseholdsByStations_shouldCallServiceOnceWithCorrectParam() throws Exception {
        when(fireStationService.getHouseholdsByStations("1,3"))
                .thenReturn(new FloodResponse(new ArrayList<>()));

        mockMvc.perform(get("/flood/stations").param("stations", "1,3"));

        verify(fireStationService, times(1)).getHouseholdsByStations("1,3");
    }

    @Test
    @DisplayName("GET /flood/stations — missing stations param returns 400 Bad Request")
    void getHouseholdsByStations_shouldReturn400WhenParamMissing() throws Exception {
        mockMvc.perform(get("/flood/stations").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /flood/stations?stations=1 — empty medications and allergies arrays are returned correctly")
    void getHouseholdsByStations_shouldReturnEmptyMedicationsAndAllergies() throws Exception {
        FloodResident resident = new FloodResident("Jamie", "Peters", "841-874-7462", 44,
                List.of(), List.of());
        when(fireStationService.getHouseholdsByStations("1"))
                .thenReturn(new FloodResponse(List.of(new FloodHousehold("908 73rd St", List.of(resident)))));

        mockMvc.perform(get("/flood/stations")
                        .param("stations", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.households[0].residents[0].medications").isArray())
                .andExpect(jsonPath("$.households[0].residents[0].medications.length()").value(0))
                .andExpect(jsonPath("$.households[0].residents[0].allergies").isArray())
                .andExpect(jsonPath("$.households[0].residents[0].allergies.length()").value(0));
    }
}
