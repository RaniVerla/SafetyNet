package net.example.safetynet.controller;

import net.example.safetynet.model.Person;
import net.example.safetynet.service.PersonService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class PersonControllerTest {

    @Mock
    private PersonService personService ;

    @InjectMocks
    private PersonController controller ;

    @Test
    void addPerson_directCall() {

        Person person = new Person();

        when(personService.addPerson(any()))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED)
                        .body("Person added successfully"));

        ResponseEntity<String> response = controller.addPerson(person);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Person added successfully", response.getBody());
    }

    @Test
    void updatePerson_directCall() {

        Person person = new Person();

        when(personService.updatePerson(any(), any(), any()))
                .thenReturn(ResponseEntity.ok("Person updated successfully"));

        ResponseEntity<String> response =
                controller.updatePerson(person, "Boyd", "John");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Person updated successfully", response.getBody());

        verify(personService, times(1))
                .updatePerson(person, "Boyd", "John");
    }

    @Test
    void deletePerson_directCall() {

        when(personService.deletePerson(any(), any()))
                .thenReturn(ResponseEntity.status(HttpStatus.NO_CONTENT)
                        .body("Person deleted successfully"));

        ResponseEntity<String> response =
                controller.deletePerson("Boyd", "John");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertEquals("Person deleted successfully", response.getBody());

        verify(personService, times(1))
                .deletePerson("Boyd", "John");
    }

    @Test
    void getAllPersons_directCall() {

        List<Person> persons = List.of(new Person());

        when(personService.getAllPersons()).thenReturn(persons);

        List<Person> response = controller.getAllPersons();

        assertEquals(1, response.size());
        verify(personService, times(1)).getAllPersons();
    }
}