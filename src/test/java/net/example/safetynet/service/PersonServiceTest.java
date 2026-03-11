package net.example.safetynet.service;

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

import java.util.ArrayList;
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
}