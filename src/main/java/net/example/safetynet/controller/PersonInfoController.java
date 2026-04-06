package net.example.safetynet.controller;

import lombok.extern.slf4j.Slf4j;
import net.example.safetynet.model.PersonInfoLastNameResponse;
import net.example.safetynet.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class PersonInfoController {

    @Autowired
    private PersonService personService;

    /**
     * Get person information by last name
     *
     * @param lastName the last name
     * @return PersonInfoLastNameResponse containing list of person infos
     */
    @GetMapping("/personInfolastName")
    public PersonInfoLastNameResponse getPersonInfoByLastName(@RequestParam String lastName) {
        log.info("Fetching person info for last name: {}", lastName);
        return personService.getPersonInfoByLastName(lastName);
    }
}
