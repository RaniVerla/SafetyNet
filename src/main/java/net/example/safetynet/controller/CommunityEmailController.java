package net.example.safetynet.controller;

import lombok.extern.slf4j.Slf4j;
import net.example.safetynet.model.CommunityEmailResponse;
import net.example.safetynet.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
public class CommunityEmailController {

    @Autowired
    private PersonService personService;

    /**
     * Get email addresses of all residents in a specific city
     *
     * @param city the city name
     * @return CommunityEmailResponse containing list of unique email addresses
     */
    @GetMapping("/communityEmail")
    public CommunityEmailResponse getCommunityEmails(@RequestParam String city) {
        log.info("Fetching email addresses for city: {}", city);
        List<String> emails = personService.getEmailsByCity(city);
        return new CommunityEmailResponse(emails);
    }
}

