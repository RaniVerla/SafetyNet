package net.example.safetynet.controller;


import net.example.safetynet.model.Person;
import net.example.safetynet.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PersonController {

    @Autowired
    PersonService personService;

    @PostMapping("/person")
    public ResponseEntity<String> addPerson(@RequestBody Person person)
    {
       return personService.addPerson(person);

    }

}
