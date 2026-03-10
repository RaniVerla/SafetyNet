package net.example.safetynet.controller;


import net.example.safetynet.model.Person;
import net.example.safetynet.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/v1")
@RestController
public class PersonController {

    @Autowired
    PersonService personService;

    /**
     * Creating Post method for person controller
     *
     * @param person
     * @return
     */
    @PostMapping("/person")
    public ResponseEntity<String> addPerson(@RequestBody Person person) {
        return personService.addPerson(person);

    }

    /**
     * Creating put method for person controller
     *
     * @param person
     * @param lastName
     * @return
     */
    @PutMapping("/person/{lastName}/{firstName}")
    public ResponseEntity<String> updatePerson(@RequestBody Person person, @PathVariable String lastName,
                                               @PathVariable String firstName) {
        return personService.updatePerson(person, lastName, firstName);

    }

    /**
     * Creating Delete method for person controller
     *
     * @param firstName
     * @param lastName
     * @return
     */
    @DeleteMapping("/person/{lastName}/{firstName}")
    public ResponseEntity<String> deletePerson(@PathVariable String lastName, @PathVariable String firstName) {
        return personService.deletePerson(lastName, firstName);

    }

    /**
     * Creating Get method for person controller
     *
     * @return
     */
    @GetMapping("/person")
    public List<Person> getAllPersons() {
        return personService.getAllPersons();
    }

}
