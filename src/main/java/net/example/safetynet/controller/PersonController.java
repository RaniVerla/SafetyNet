package net.example.safetynet.controller;


import net.example.safetynet.model.Person;
import net.example.safetynet.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PersonController {

    @Autowired
    PersonService personService;

    @PostMapping("/person")
    public ResponseEntity<String> addPerson(@RequestBody List<Person> persons)
    {
       return personService.addPerson(persons);

    }


    @GetMapping("/person")
    public List<Person> getAllPersons()
    {
        return personService.getAllPersons();
    }

}
