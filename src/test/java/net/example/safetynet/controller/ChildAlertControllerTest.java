package net.example.safetynet.controller;

import net.example.safetynet.model.ChildAlertResponse;
import net.example.safetynet.service.PersonService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ChildAlertControllerTest {

    @Mock
    private PersonService personService;

    @InjectMocks
    private ChildAlertController childAlertController;

    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /childAlert?address=1509 Culver St - Should return children at address")
    void getChildrenAtAddress_shouldReturnChildren() throws Exception {
        // Given
        String address = "1509 Culver St";

        // Create expected response with children
        ChildAlertResponse.ChildInfo child1 = new ChildAlertResponse.ChildInfo(
            "Tenley", "Boyd", 14,
            Arrays.asList(
                new ChildAlertResponse.HouseholdMember("John", "Boyd"),
                new ChildAlertResponse.HouseholdMember("Jacob", "Boyd"),
                new ChildAlertResponse.HouseholdMember("Felicia", "Boyd")
            )
        );

        ChildAlertResponse.ChildInfo child2 = new ChildAlertResponse.ChildInfo(
            "Roger", "Boyd", 9,
            Arrays.asList(
                new ChildAlertResponse.HouseholdMember("John", "Boyd"),
                new ChildAlertResponse.HouseholdMember("Jacob", "Boyd"),
                new ChildAlertResponse.HouseholdMember("Felicia", "Boyd")
            )
        );

        ChildAlertResponse expectedResponse = new ChildAlertResponse(Arrays.asList(child1, child2));

        when(personService.getChildrenAtAddress("1509 Culver St")).thenReturn(expectedResponse);

        // When & Then
        mockMvc = MockMvcBuilders.standaloneSetup(childAlertController).build();
        mockMvc.perform(get("/childAlert")
                .param("address", address))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.children").isArray())
                .andExpect(jsonPath("$.children.length()").value(2))
                .andExpect(jsonPath("$.children[0].firstName").value("Tenley"))
                .andExpect(jsonPath("$.children[0].lastName").value("Boyd"))
                .andExpect(jsonPath("$.children[0].age").value(14))
                .andExpect(jsonPath("$.children[1].firstName").value("Roger"))
                .andExpect(jsonPath("$.children[1].lastName").value("Boyd"))
                .andExpect(jsonPath("$.children[1].age").value(9));
    }

    @Test
    @DisplayName("GET /childAlert?address=748 Townings Dr - Should return empty list when no children")
    void getChildrenAtAddress_shouldReturnEmptyWhenNoChildren() throws Exception {
        // Given
        String address = "748 Townings Dr";

        // No children at this address (Foster Shepard and Clive Ferguson are adults)
        ChildAlertResponse expectedResponse = new ChildAlertResponse(new ArrayList<>());

        when(personService.getChildrenAtAddress("748 Townings Dr")).thenReturn(expectedResponse);

        // When & Then
        mockMvc = MockMvcBuilders.standaloneSetup(childAlertController).build();
        mockMvc.perform(get("/childAlert")
                .param("address", address))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.children").isArray())
                .andExpect(jsonPath("$.children.length()").value(0));
    }

    @Test
    @DisplayName("GET /childAlert?address=Nonexistent Address - Should return empty list")
    void getChildrenAtAddress_shouldReturnEmptyForNonexistentAddress() throws Exception {
        // Given
        String address = "Nonexistent Address";

        ChildAlertResponse expectedResponse = new ChildAlertResponse(new ArrayList<>());

        when(personService.getChildrenAtAddress("Nonexistent Address")).thenReturn(expectedResponse);

        // When & Then
        mockMvc = MockMvcBuilders.standaloneSetup(childAlertController).build();
        mockMvc.perform(get("/childAlert")
                .param("address", address))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.children").isArray())
                .andExpect(jsonPath("$.children.length()").value(0));
    }

    @Test
    @DisplayName("GET /childAlert?address=\"1509 Culver St\" - Should handle quoted addresses")
    void getChildrenAtAddress_shouldHandleQuotedAddress() throws Exception {
        // Given - controller should strip quotes
        String addressWithQuotes = "\"1509 Culver St\"";

        ChildAlertResponse.ChildInfo child1 = new ChildAlertResponse.ChildInfo(
            "Tenley", "Boyd", 14,
            Arrays.asList(
                new ChildAlertResponse.HouseholdMember("John", "Boyd"),
                new ChildAlertResponse.HouseholdMember("Jacob", "Boyd"),
                new ChildAlertResponse.HouseholdMember("Felicia", "Boyd")
            )
        );

        ChildAlertResponse.ChildInfo child2 = new ChildAlertResponse.ChildInfo(
            "Roger", "Boyd", 9,
            Arrays.asList(
                new ChildAlertResponse.HouseholdMember("John", "Boyd"),
                new ChildAlertResponse.HouseholdMember("Jacob", "Boyd"),
                new ChildAlertResponse.HouseholdMember("Felicia", "Boyd")
            )
        );

        ChildAlertResponse expectedResponse = new ChildAlertResponse(Arrays.asList(child1, child2));

        when(personService.getChildrenAtAddress("1509 Culver St")).thenReturn(expectedResponse);

        // When & Then
        mockMvc = MockMvcBuilders.standaloneSetup(childAlertController).build();
        mockMvc.perform(get("/childAlert")
                .param("address", addressWithQuotes))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.children.length()").value(2));
    }

    @Test
    @DisplayName("GET /childAlert without address parameter - Should return 400 Bad Request")
    void getChildrenAtAddress_shouldReturnBadRequestWithoutAddress() throws Exception {
        // When & Then
        mockMvc = MockMvcBuilders.standaloneSetup(childAlertController).build();
        mockMvc.perform(get("/childAlert"))
                .andExpect(status().isBadRequest());
    }
}
