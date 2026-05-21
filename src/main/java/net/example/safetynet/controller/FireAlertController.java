package net.example.safetynet.controller;

import lombok.extern.slf4j.Slf4j;
import net.example.safetynet.model.FireAlertResponse;
import net.example.safetynet.service.FireStationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class FireAlertController {

    @Autowired
    private FireStationService fireStationService;

    /**
     * Get residents at a given address and the fire station number serving it.
     * Returns name, phone, age, medications and allergies for each resident.
     *
     * @param address the address to query
     * @return FireAlertResponse with station number and resident medical details
     */
    @GetMapping("/fire")
    public FireAlertResponse getResidentsByAddress(@RequestParam String address) {
        log.info("Fetching fire alert info for address: {}", address);
        return fireStationService.getResidentsByAddress(address);
    }
}
