package net.example.safetynet.controller;

import net.example.safetynet.model.PersonInfo;
import net.example.safetynet.model.PersonInfoLastNameResponse;
import net.example.safetynet.service.PersonService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonInfoControllerTest {

    @Mock
    private PersonService personService;

    @InjectMocks
    private PersonInfoController controller;

    @Test
    void getPersonInfoByLastName_shouldReturnPersonInfoResponse() {
        // Given
        String lastName = "Boyd";
        PersonInfo personInfo = new PersonInfo("John", "Boyd", "1509 Culver St", 36, "jaboyd@email.com",
                List.of("aznol:350mg", "hydrapermazol:100mg"), List.of("nillacilan"));
        PersonInfoLastNameResponse expectedResponse = new PersonInfoLastNameResponse(List.of(personInfo));

        when(personService.getPersonInfoByLastName(lastName)).thenReturn(expectedResponse);

        // When
        PersonInfoLastNameResponse response = controller.getPersonInfoByLastName(lastName);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getPersonInfos().size());
        assertEquals("John", response.getPersonInfos().get(0).getFirstName());
        assertEquals("Boyd", response.getPersonInfos().get(0).getLastName());
        assertEquals("1509 Culver St", response.getPersonInfos().get(0).getAddress());
        assertEquals(36, response.getPersonInfos().get(0).getAge());
        assertEquals("jaboyd@email.com", response.getPersonInfos().get(0).getEmail());
        assertEquals(List.of("aznol:350mg", "hydrapermazol:100mg"), response.getPersonInfos().get(0).getMedications());
        assertEquals(List.of("nillacilan"), response.getPersonInfos().get(0).getAllergies());

        verify(personService, times(1)).getPersonInfoByLastName(lastName);
    }

    @Test
    void getPersonInfoByLastName_shouldHandleMultiplePersons() {
        // Given
        String lastName = "Boyd";
        PersonInfo personInfo1 = new PersonInfo("John", "Boyd", "1509 Culver St", 36, "jaboyd@email.com",
                List.of("aznol:350mg"), List.of("nillacilan"));
        PersonInfo personInfo2 = new PersonInfo("Jacob", "Boyd", "1509 Culver St", 31, "drk@email.com",
                List.of("pharmacol:5000mg"), List.of());
        PersonInfoLastNameResponse expectedResponse = new PersonInfoLastNameResponse(List.of(personInfo1, personInfo2));

        when(personService.getPersonInfoByLastName(lastName)).thenReturn(expectedResponse);

        // When
        PersonInfoLastNameResponse response = controller.getPersonInfoByLastName(lastName);

        // Then
        assertNotNull(response);
        assertEquals(2, response.getPersonInfos().size());
        assertEquals("John", response.getPersonInfos().get(0).getFirstName());
        assertEquals("Jacob", response.getPersonInfos().get(1).getFirstName());

        verify(personService, times(1)).getPersonInfoByLastName(lastName);
    }

    @Test
    void getPersonInfoByLastName_shouldHandleEmptyResponse() {
        // Given
        String lastName = "Nonexistent";
        PersonInfoLastNameResponse expectedResponse = new PersonInfoLastNameResponse(List.of());

        when(personService.getPersonInfoByLastName(lastName)).thenReturn(expectedResponse);

        // When
        PersonInfoLastNameResponse response = controller.getPersonInfoByLastName(lastName);

        // Then
        assertNotNull(response);
        assertTrue(response.getPersonInfos().isEmpty());

        verify(personService, times(1)).getPersonInfoByLastName(lastName);
    }
}
