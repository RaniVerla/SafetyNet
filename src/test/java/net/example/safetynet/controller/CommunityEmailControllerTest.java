package net.example.safetynet.controller;

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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CommunityEmailControllerTest {

    @Mock
    private PersonService personService;

    @InjectMocks
    private CommunityEmailController communityEmailController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(communityEmailController).build();
    }

    @Test
    @DisplayName("GET /communityEmail?city=Culver — returns 200 with list of emails")
    void getCommunityEmails_shouldReturn200WithEmails() throws Exception {
        List<String> emails = Arrays.asList(
                "jaboyd@email.com", "drk@email.com", "tenz@email.com"
        );
        when(personService.getEmailsByCity("Culver")).thenReturn(emails);

        mockMvc.perform(get("/communityEmail")
                        .param("city", "Culver")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emails").isArray())
                .andExpect(jsonPath("$.emails.length()").value(3))
                .andExpect(jsonPath("$.emails[0]").value("jaboyd@email.com"))
                .andExpect(jsonPath("$.emails[1]").value("drk@email.com"))
                .andExpect(jsonPath("$.emails[2]").value("tenz@email.com"));
    }

    @Test
    @DisplayName("GET /communityEmail?city=UnknownCity — returns 200 with empty list")
    void getCommunityEmails_shouldReturnEmptyListForUnknownCity() throws Exception {
        when(personService.getEmailsByCity("UnknownCity")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/communityEmail")
                        .param("city", "UnknownCity")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emails").isArray())
                .andExpect(jsonPath("$.emails.length()").value(0));
    }

    @Test
    @DisplayName("GET /communityEmail?city=Culver — response contains emails key")
    void getCommunityEmails_shouldContainEmailsKey() throws Exception {
        when(personService.getEmailsByCity("Culver"))
                .thenReturn(List.of("jaboyd@email.com"));

        mockMvc.perform(get("/communityEmail")
                        .param("city", "Culver")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emails").exists());
    }

    @Test
    @DisplayName("GET /communityEmail?city=Culver — service is called exactly once with correct city")
    void getCommunityEmails_shouldCallServiceOnceWithCorrectCity() throws Exception {
        when(personService.getEmailsByCity("Culver")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/communityEmail").param("city", "Culver"));

        verify(personService, times(1)).getEmailsByCity("Culver");
    }

    @Test
    @DisplayName("GET /communityEmail — missing city param returns 400 Bad Request")
    void getCommunityEmails_shouldReturn400WhenCityParamMissing() throws Exception {
        mockMvc.perform(get("/communityEmail")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /communityEmail?city=Culver — returns single email correctly")
    void getCommunityEmails_shouldReturnSingleEmail() throws Exception {
        when(personService.getEmailsByCity("Culver"))
                .thenReturn(List.of("jaboyd@email.com"));

        mockMvc.perform(get("/communityEmail")
                        .param("city", "Culver")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emails.length()").value(1))
                .andExpect(jsonPath("$.emails[0]").value("jaboyd@email.com"));
    }
}
