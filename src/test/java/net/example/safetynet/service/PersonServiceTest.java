package net.example.safetynet.service;

import net.example.safetynet.model.ChildAlertResponse;
import net.example.safetynet.model.Medicalrecord;
import net.example.safetynet.model.Person;
import net.example.safetynet.utils.ReadFromFileUtil;
import net.example.safetynet.utils.WriteToFileUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tools.jackson.core.type.TypeReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class PersonServiceTest {
    @Mock
    private ReadFromFileUtil readFromFileUtil;

    @Mock
    private WriteToFileUtil writeToFileUtil;

    @InjectMocks
    private PersonService personService;

    @Test
    @DisplayName("Testing the success path")
    void addPerson_shouldAddSuccessfully() throws Exception {
        List<Person> persons = new ArrayList<>();

        Person newPerson = new Person("John", "Boyd",
                "1509 Culver St", "Culver", "97451",
                "841-874-6512", "john@email.com");


        doNothing().when(writeToFileUtil)
                .writeToFile(any(), any());

        ResponseEntity<String> response =
                personService.addPerson(newPerson);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        verify(readFromFileUtil, times(1))
                .readFromFile(any(), any());

        verify(writeToFileUtil, times(1))
                .writeToFile(any(), any());
    }

    @Test
    @DisplayName("Testing the Internal server error")
    void addPerson_5XX_Error() throws Exception {
        List<Person> persons = new ArrayList<>();

        Person newPerson = new Person("John", "Boyd",
                "1509 Culver St", "Culver", "97451",
                "841-874-6512", "john@email.com");

        when(readFromFileUtil.readFromFile(any(), any()))
                .thenReturn(null);

        ResponseEntity<String> response =
                personService.addPerson(newPerson);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());


    }

    @Test
    void deletePerson_shouldRemoveSuccessfully() throws Exception {
        List<Person> persons = new ArrayList<>();
        persons.add(new Person("John", "Boyd", "1509 Culver St", "Culver", "97451", "841-874-6512", "john@email.com"));

        when(readFromFileUtil.<Person>readFromFile(any(), any())).thenReturn(persons);
        doNothing().when(writeToFileUtil).writeToFile(any(), any());

        ResponseEntity<String> response = personService.deletePerson("Boyd", "John");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(writeToFileUtil, times(1)).writeToFile(any(), any());
    }

    @Test
    void getAllPersons_shouldReturnList() throws Exception {

        // Arrange
        List<Person> persons = new ArrayList<>();
        persons.add(new Person("John", "Boyd", "1509 Culver St", "Culver", "97451",
                "841-874-6512", "john@email.com"));

        when(readFromFileUtil.<Person>readFromFile(any(), any())).thenReturn(persons);

        // Act
        List<Person> result = personService.getAllPersons();

        // Assert
        assertEquals(1, result.size());
        assertEquals("John", result.get(0).getFirstName());
        assertEquals("Boyd", result.get(0).getLastName());

        // Optional: verify readFromFile was called
        verify(readFromFileUtil, times(1)).readFromFile(any(), any());
    }


    @Test
    void updatePerson_shouldUpdateSuccessfully() throws Exception {

        // Arrange
        List<Person> persons = new ArrayList<>();
        Person existingPerson = new Person("John", "Boyd",
                "1509 Culver St", "Culver", "97451",
                "841-874-6512", "john@email.com");
        persons.add(existingPerson);

        Person updatedPerson = new Person("John", "Boyd",
                "New Address", "Culver", "97451",
                "841-874-6512", "john@email.com");

        when(readFromFileUtil.<Person>readFromFile(any(), any()))
                .thenReturn(persons);

        doNothing().when(writeToFileUtil).writeToFile(any(), any());

        // Act
        ResponseEntity<String> response =
                personService.updatePerson(updatedPerson, "Boyd", "John");

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Person updated successfully", response.getBody());

        // Verify write called
        verify(writeToFileUtil, times(1)).writeToFile(any(), any());

    }

    @Test
    void deleteFireStation_notFound() {

        when(readFromFileUtil.readFromFile(any(), any()))
                .thenReturn(new ArrayList<>());

        ResponseEntity<String> response =
                personService.deletePerson("lname","fname");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("getChildrenAtAddress should return children at 1509 Culver St")
    void getChildrenAtAddress_shouldReturnChildrenAtValidAddress() throws Exception {
        // Given
        String address = "1509 Culver St";

        // Mock person data
        List<Person> persons = Arrays.asList(
                new Person("John", "Boyd", "1509 Culver St", "Culver", "97451", "841-874-6512", "jaboyd@email.com"),
                new Person("Jacob", "Boyd", "1509 Culver St", "Culver", "97451", "841-874-6513", "drk@email.com"),
                new Person("Tenley", "Boyd", "1509 Culver St", "Culver", "97451", "841-874-6512", "tenz@email.com"),
                new Person("Roger", "Boyd", "1509 Culver St", "Culver", "97451", "841-874-6512", "jaboyd@email.com"),
                new Person("Felicia", "Boyd", "1509 Culver St", "Culver", "97451", "841-874-6544", "jaboyd@email.com")
        );

        // Mock medical record data
        List<Medicalrecord> medicalRecords = Arrays.asList(
                new Medicalrecord("John", "Boyd", "03/06/1984", new String[]{"aznol:350mg", "hydrapermazol:100mg"}, new String[]{"nillacilan"}),
                new Medicalrecord("Jacob", "Boyd", "03/06/1989", new String[]{"pharmacol:5000mg", "terazine:10mg", "noznazol:250mg"}, new String[]{}),
                new Medicalrecord("Tenley", "Boyd", "02/18/2012", new String[]{}, new String[]{"peanut"}), // Age 14
                new Medicalrecord("Roger", "Boyd", "09/06/2017", new String[]{}, new String[]{}), // Age 9
                new Medicalrecord("Felicia", "Boyd", "01/08/1986", new String[]{"tetracyclaz:650mg"}, new String[]{"xilliathal"})
        );

        when(readFromFileUtil.readFromInputStream(any(), any(TypeReference.class)))
                .thenReturn(persons)  // First call for persons
                .thenReturn(medicalRecords); // Second call for medical records

        // When
        ChildAlertResponse response = personService.getChildrenAtAddress(address);

        // Then
        assertNotNull(response);
        assertNotNull(response.getChildren());
        assertEquals(2, response.getChildren().size());

        // Verify first child (Tenley)
        ChildAlertResponse.ChildInfo child1 = response.getChildren().get(0);
        assertEquals("Tenley", child1.getFirstName());
        assertEquals("Boyd", child1.getLastName());
        assertEquals(14, child1.getAge());
        assertEquals(4, child1.getHouseholdMembers().size()); // John, Jacob, Roger, Felicia

        // Verify second child (Roger)
        ChildAlertResponse.ChildInfo child2 = response.getChildren().get(1);
        assertEquals("Roger", child2.getFirstName());
        assertEquals("Boyd", child2.getLastName());
        assertEquals(8, child2.getAge()); // Born 09/06/2017, current date is 03/31/2026
        assertEquals(4, child2.getHouseholdMembers().size()); // John, Jacob, Tenley, Felicia
    }

    @Test
    @DisplayName("getChildrenAtAddress should return empty list when no children at address")
    void getChildrenAtAddress_shouldReturnEmptyWhenNoChildren() throws Exception {
        // Given
        String address = "748 Townings Dr";

        // Mock person data (only adults at this address)
        List<Person> persons = Arrays.asList(
                new Person("Foster", "Shepard", "748 Townings Dr", "Culver", "97451", "841-874-6544", "jaboyd@email.com"),
                new Person("Clive", "Ferguson", "748 Townings Dr", "Culver", "97451", "841-874-6741", "clivfd@ymail.com")
        );

        // Mock medical record data (both are adults)
        List<Medicalrecord> medicalRecords = Arrays.asList(
                new Medicalrecord("Foster", "Shepard", "01/08/1980", new String[]{}, new String[]{}),
                new Medicalrecord("Clive", "Ferguson", "03/06/1994", new String[]{}, new String[]{})
        );

        when(readFromFileUtil.readFromInputStream(any(), any(TypeReference.class)))
                .thenReturn(persons)
                .thenReturn(medicalRecords);

        // When
        ChildAlertResponse response = personService.getChildrenAtAddress(address);

        // Then
        assertNotNull(response);
        assertNotNull(response.getChildren());
        assertTrue(response.getChildren().isEmpty());
    }

    @Test
    @DisplayName("getChildrenAtAddress should return empty list for nonexistent address")
    void getChildrenAtAddress_shouldReturnEmptyForNonexistentAddress() throws Exception {
        // Given
        String address = "Nonexistent Address";

        // Mock empty person data
        List<Person> persons = new ArrayList<>();
        List<Medicalrecord> medicalRecords = new ArrayList<>();

        when(readFromFileUtil.readFromInputStream(any(), any(TypeReference.class)))
                .thenReturn(persons)
                .thenReturn(medicalRecords);

        // When
        ChildAlertResponse response = personService.getChildrenAtAddress(address);

        // Then
        assertNotNull(response);
        assertNotNull(response.getChildren());
        assertTrue(response.getChildren().isEmpty());
    }

    @Test
    @DisplayName("getChildrenAtAddress should handle case insensitive address matching")
    void getChildrenAtAddress_shouldHandleCaseInsensitiveMatching() throws Exception {
        // Given
        String address = "1509 culver st"; // lowercase

        // Mock person data with mixed case
        List<Person> persons = Arrays.asList(
                new Person("Tenley", "Boyd", "1509 Culver St", "Culver", "97451", "841-874-6512", "tenz@email.com")
        );

        List<Medicalrecord> medicalRecords = Arrays.asList(
                new Medicalrecord("Tenley", "Boyd", "02/18/2012", new String[]{}, new String[]{"peanut"})
        );

        when(readFromFileUtil.readFromInputStream(any(), any(TypeReference.class)))
                .thenReturn(persons)
                .thenReturn(medicalRecords);

        // When
        ChildAlertResponse response = personService.getChildrenAtAddress(address);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getChildren().size());
        assertEquals("Tenley", response.getChildren().get(0).getFirstName());
    }

    @Test
    @DisplayName("getChildrenAtAddress should handle exception gracefully")
    void getChildrenAtAddress_shouldHandleExceptionGracefully() throws Exception {
        // Given
        String address = "1509 Culver St";

        // Mock exception during file reading
        when(readFromFileUtil.readFromInputStream(any(), any(TypeReference.class)))
                .thenThrow(new RuntimeException("File read error"));

        // When
        ChildAlertResponse response = personService.getChildrenAtAddress(address);

        // Then
        assertNotNull(response);
        assertNotNull(response.getChildren());
        assertTrue(response.getChildren().isEmpty());
    }
}