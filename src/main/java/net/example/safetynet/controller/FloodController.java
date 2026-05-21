package net.example.safetynet.controller;

import lombok.extern.slf4j.Slf4j;
import net.example.safetynet.model.FloodResponse;
import net.example.safetynet.service.FireStationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class FloodController {

    @Autowired
    private FireStationService fireStationService;

    /**
     * Get all households served by one or more fire stations, grouped by address.
     * Each resident entry includes name, phone, age, medications and allergies.
     *
     * @param stations comma-separated list of station numbers, e.g. "1,2,3"
     * @return FloodResponse with households grouped by address
     */
    @GetMapping("/flood/stations")
    public FloodResponse getHouseholdsByStations(@RequestParam String stations) {
        log.info("Fetching flood households for stations: {}", stations);
        return fireStationService.getHouseholdsByStations(stations);
    }
}
