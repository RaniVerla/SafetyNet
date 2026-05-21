package net.example.safetynet.controller;

import net.example.safetynet.model.PersonInfo;
import net.example.safetynet.model.PersonInfoLastNameResponse;
import net.example.safetynet.service.PersonService;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PersonInfoControllerTest {

    @Mock
    private PersonService personService;

    @InjectMocks
    private PersonInfoController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    // ======================= Direct controller call tests =======================

    @Test
    @DisplayName("getPersonInfoByLastName — delegates to service and returns response")
    void getPersonInfoByLastName_shouldReturnPersonInfoResponse() {
        PersonInfo personInfo = new PersonInfo("John", "Boyd", "1509 Culver St", 42,
                "jaboyd@email.com", List.of("aznol:350mg", "hydrapermazol:100mg"), List.of("nillacilan"));
        PersonInfoLastNameResponse expected = new PersonInfoLastNameResponse(List.of(personInfo));
        when(personService.getPersonInfoByLastName("Boyd")).thenReturn(expected);

        PersonInfoLastNameResponse response = controller.getPersonInfoByLastName("Boyd");

        assertNotNull(response);
        assertEquals(1, response.getPersonInfos().size());
        assertEquals("John",          response.getPersonInfos().get(0).getFirstName());
        assertEquals("Boyd",          response.getPersonInfos().get(0).getLastName());
        assertEquals("1509 Culver St",response.getPersonInfos().get(0).getAddress());
        assertEquals(42,              response.getPersonInfos().get(0).getAge());
        assertEquals("jaboyd@email.com", response.getPersonInfos().get(0).getEmail());
        assertEquals(List.of("aznol:350mg", "hydrapermazol:100mg"), response.getPersonInfos().get(0).getMedications());
        assertEquals(List.of("nillacilan"), response.getPersonInfos().get(0).getAllergies());
        verify(personService, times(1)).getPersonInfoByLastName("Boyd");
    }

    @Test
    @DisplayName("getPersonInfoByLastName — returns all persons with the same last name")
    void getPersonInfoByLastName_shouldHandleMultiplePersons() {
        PersonInfo p1 = new PersonInfo("John",  "Boyd", "1509 Culver St", 42, "jaboyd@email.com",
                List.of("aznol:350mg"), List.of("nillacilan"));
        PersonInfo p2 = new PersonInfo("Jacob", "Boyd", "1509 Culver St", 37, "drk@email.com",
                List.of("pharmacol:5000mg"), List.of());
        when(personService.getPersonInfoByLastName("Boyd"))
                .thenReturn(new PersonInfoLastNameResponse(List.of(p1, p2)));

        PersonInfoLastNameResponse response = controller.getPersonInfoByLastName("Boyd");

        assertNotNull(response);
        assertEquals(2, response.getPersonInfos().size());
        assertEquals("John",  response.getPersonInfos().get(0).getFirstName());
        assertEquals("Jacob", response.getPersonInfos().get(1).getFirstName());
        verify(personService, times(1)).getPersonInfoByLastName("Boyd");
    }

    @Test
    @DisplayName("getPersonInfoByLastName — returns empty list for unknown last name")
    void getPersonInfoByLastName_shouldHandleEmptyResponse() {
        when(personService.getPersonInfoByLastName("Nonexistent"))
                .thenReturn(new PersonInfoLastNameResponse(List.of()));

        PersonInfoLastNameResponse response = controller.getPersonInfoByLastName("Nonexistent");

        assertNotNull(response);
        assertTrue(response.getPersonInfos().isEmpty());
        verify(personService, times(1)).getPersonInfoByLastName("Nonexistent");
    }

    // ======================= MockMvc HTTP-level tests =======================

    @Test
    @DisplayName("GET /personInfolastName?lastName=Boyd — returns 200 with full person info")
    void getPersonInfoByLastName_httpCall_shouldReturn200WithPersonInfo() throws Exception {
        PersonInfo personInfo = new PersonInfo("John", "Boyd", "1509 Culver St", 42,
                "jaboyd@email.com", List.of("aznol:350mg", "hydrapermazol:100mg"), List.of("nillacilan"));
        when(personService.getPersonInfoByLastName("Boyd"))
                .thenReturn(new PersonInfoLastNameResponse(List.of(personInfo)));

        mockMvc.perform(get("/personInfolastName")
                        .param("lastName", "Boyd")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.personInfos").isArray())
                .andExpect(jsonPath("$.personInfos.length()").value(1))
                .andExpect(jsonPath("$.personInfos[0].firstName").value("John"))
                .andExpect(jsonPath("$.personInfos[0].lastName").value("Boyd"))
                .andExpect(jsonPath("$.personInfos[0].address").value("1509 Culver St"))
                .andExpect(jsonPath("$.personInfos[0].age").value(42))
                .andExpect(jsonPath("$.personInfos[0].email").value("jaboyd@email.com"))
                .andExpect(jsonPath("$.personInfos[0].medications.length()").value(2))
                .andExpect(jsonPath("$.personInfos[0].allergies[0]").value("nillacilan"));
    }

    @Test
    @DisplayName("GET /personInfolastName?lastName=Boyd — returns all persons with same last name")
    void getPersonInfoByLastName_httpCall_shouldReturnMultiplePersons() throws Exception {
        List<PersonInfo> persons = List.of(
                new PersonInfo("John",    "Boyd", "1509 Culver St", 42, "jaboyd@email.com", List.of(), List.of()),
                new PersonInfo("Jacob",   "Boyd", "1509 Culver St", 37, "drk@email.com",   List.of(), List.of()),
                new PersonInfo("Tenley",  "Boyd", "1509 Culver St", 14, "tenz@email.com",  List.of(), List.of()),
                new PersonInfo("Allison", "Boyd", "112 Steppes Pl", 61, "aly@imail.com",   List.of(), List.of())
        );
        when(personService.getPersonInfoByLastName("Boyd"))
                .thenReturn(new PersonInfoLastNameResponse(persons));

        mockMvc.perform(get("/personInfolastName")
                        .param("lastName", "Boyd")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.personInfos.length()").value(4))
                .andExpect(jsonPath("$.personInfos[0].firstName").value("John"))
                .andExpect(jsonPath("$.personInfos[1].firstName").value("Jacob"))
                .andExpect(jsonPath("$.personInfos[2].firstName").value("Tenley"))
                .andExpect(jsonPath("$.personInfos[3].firstName").value("Allison"));
    }

    @Test
    @DisplayName("GET /personInfolastName?lastName=Unknown — returns 200 with empty list")
    void getPersonInfoByLastName_httpCall_shouldReturnEmptyForUnknownLastName() throws Exception {
        when(personService.getPersonInfoByLastName("Unknown"))
                .thenReturn(new PersonInfoLastNameResponse(new ArrayList<>()));

        mockMvc.perform(get("/personInfolastName")
                        .param("lastName", "Unknown")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.personInfos").isArray())
                .andExpect(jsonPath("$.personInfos.length()").value(0));
    }

    @Test
    @DisplayName("GET /personInfolastName?lastName=Boyd — response contains personInfos key")
    void getPersonInfoByLastName_httpCall_shouldContainPersonInfosKey() throws Exception {
        when(personService.getPersonInfoByLastName("Boyd"))
                .thenReturn(new PersonInfoLastNameResponse(new ArrayList<>()));

        mockMvc.perform(get("/personInfolastName")
                        .param("lastName", "Boyd")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.personInfos").exists());
    }

    @Test
    @DisplayName("GET /personInfolastName?lastName=Boyd — each entry has all required fields")
    void getPersonInfoByLastName_httpCall_shouldReturnAllRequiredFields() throws Exception {
        PersonInfo personInfo = new PersonInfo("John", "Boyd", "1509 Culver St", 42,
                "jaboyd@email.com", List.of("aznol:350mg"), List.of("nillacilan"));
        when(personService.getPersonInfoByLastName("Boyd"))
                .thenReturn(new PersonInfoLastNameResponse(List.of(personInfo)));

        mockMvc.perform(get("/personInfolastName")
                        .param("lastName", "Boyd")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.personInfos[0].firstName").exists())
                .andExpect(jsonPath("$.personInfos[0].lastName").exists())
                .andExpect(jsonPath("$.personInfos[0].address").exists())
                .andExpect(jsonPath("$.personInfos[0].age").exists())
                .andExpect(jsonPath("$.personInfos[0].email").exists())
                .andExpect(jsonPath("$.personInfos[0].medications").exists())
                .andExpect(jsonPath("$.personInfos[0].allergies").exists());
    }

    @Test
    @DisplayName("GET /personInfolastName?lastName=Boyd — service is called exactly once")
    void getPersonInfoByLastName_httpCall_shouldCallServiceExactlyOnce() throws Exception {
        when(personService.getPersonInfoByLastName("Boyd"))
                .thenReturn(new PersonInfoLastNameResponse(new ArrayList<>()));

        mockMvc.perform(get("/personInfolastName").param("lastName", "Boyd"));

        verify(personService, times(1)).getPersonInfoByLastName("Boyd");
    }

    @Test
    @DisplayName("GET /personInfolastName — missing lastName param returns 400 Bad Request")
    void getPersonInfoByLastName_httpCall_shouldReturn400WhenParamMissing() throws Exception {
        mockMvc.perform(get("/personInfolastName").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /personInfolastName?lastName=Boyd — empty medications and allergies returned correctly")
    void getPersonInfoByLastName_httpCall_shouldReturnEmptyMedicationsAndAllergies() throws Exception {
        PersonInfo personInfo = new PersonInfo("Jamie", "Peters", "908 73rd St", 44,
                "jpeter@email.com", List.of(), List.of());
        when(personService.getPersonInfoByLastName("Peters"))
                .thenReturn(new PersonInfoLastNameResponse(List.of(personInfo)));

        mockMvc.perform(get("/personInfolastName")
                        .param("lastName", "Peters")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.personInfos[0].medications").isArray())
                .andExpect(jsonPath("$.personInfos[0].medications.length()").value(0))
                .andExpect(jsonPath("$.personInfos[0].allergies").isArray())
                .andExpect(jsonPath("$.personInfos[0].allergies.length()").value(0));
    }
}
