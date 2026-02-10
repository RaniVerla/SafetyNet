package net.example.safetynet.controller;


import net.example.safetynet.model.Firestation;
import net.example.safetynet.model.Person;
import net.example.safetynet.service.FireStationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RequestMapping("/v1")
@RestController
public class FireStationController {


    @Autowired
    private FireStationService fireStationService;


    @PostMapping("/firestation")
    public ResponseEntity<String> addFireStation(@RequestBody Firestation firestation)
    {
        return fireStationService.addFireStation(firestation);

    }


    @GetMapping("/firestation")
    public Set<Firestation> getFireStations()
    {
        return fireStationService.getAllFireStations();
    }


}


