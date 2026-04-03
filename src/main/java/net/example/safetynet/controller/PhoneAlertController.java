package net.example.safetynet.controller;

import lombok.extern.slf4j.Slf4j;
import net.example.safetynet.model.PhoneAlertResponse;
import net.example.safetynet.service.FireStationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
public class PhoneAlertController {

    @Autowired
    private FireStationService fireStationService;

    /**
     * Get phone numbers of residents served by a fire station
     * Used to send emergency text messages to specific households
     *
     * @param firestation the fire station number
     * @return PhoneAlertResponse containing list of phone numbers
     */
    @GetMapping("/phoneAlert")
    public PhoneAlertResponse getPhoneAlert(@RequestParam String firestation) {
        log.info("Fetching phone numbers for fire station: {}", firestation);
        List<String> phoneNumbers = fireStationService.getPhoneNumbersByStationNumber(firestation);
        return new PhoneAlertResponse(phoneNumbers);
    }
}

