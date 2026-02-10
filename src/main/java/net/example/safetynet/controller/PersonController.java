package net.example.safetynet.controller;


import net.example.safetynet.model.Person;
import net.example.safetynet.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RequestMapping("/v1")
@RestController
public class PersonController {

    @Autowired
    PersonService personService;

    //Done
    @PostMapping("/person")
    public ResponseEntity<String> addPerson(@RequestBody Person person)
    {
       return personService.addPerson(person);

    }


    @GetMapping("/person")
    public Set<Person> getAllPersons()
    {
        return personService.getAllPersons();
    }

}
