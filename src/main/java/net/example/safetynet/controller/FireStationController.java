package net.example.safetynet.controller;


import net.example.safetynet.model.Firestation;
import net.example.safetynet.model.Person;
import net.example.safetynet.service.FireStationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/v1")
@RestController
public class FireStationController {


    @Autowired
    private FireStationService fireStationService;

    /**
     * Creating POST method for Firestation controller
     *
     */
    @PostMapping("/firestation")
    public ResponseEntity<String> addFireStation(@RequestBody Firestation firestation) {
        return fireStationService.addFireStation(firestation);

    }

    /**
     * Creating put method for Firestation controller
     */
    @PutMapping("/firestation/{station}")
    public ResponseEntity<String> updateFireStation(@RequestBody Firestation firestation, @PathVariable String station) {
        return fireStationService.updateFireStation(firestation, station);
    }

    /**
     * Creating delete method for Firestation controller
     */
    @DeleteMapping("/firestation/{station}")
    public ResponseEntity<String> deleteFireStation(@PathVariable String station) {
        return fireStationService.deleteFireStation(station);
    }


    /**
     * Creating GET method for Firestation controller
     */
    @GetMapping("/firestation")
    public List<Firestation> getFireStations() {
        return fireStationService.getAllFireStations();
    }


}


