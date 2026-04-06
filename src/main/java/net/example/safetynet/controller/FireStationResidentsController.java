package net.example.safetynet.controller;

import lombok.extern.slf4j.Slf4j;
import net.example.safetynet.model.FireStationResidents;
import net.example.safetynet.service.FireStationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/firestation")
public class FireStationResidentsController {

    @Autowired
    private FireStationService fireStationService;

    /**
     * Get residents covered by a fire station
     *
     * @param stationNumber the station number to query
     * @return FireStationResidents containing residents list with first name, last name, address, phone,
     *         and counts of adults and children
     */
    @GetMapping
    public FireStationResidents getResidentsByStation(@RequestParam String stationNumber) {
        log.info("Fetching residents for station: {}", stationNumber);
        return fireStationService.getResidentsByStationNumber(stationNumber);
    }

}
