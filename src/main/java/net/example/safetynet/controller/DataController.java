package net.example.safetynet.controller;

import net.example.safetynet.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class DataController {

    @Autowired
    private DataService dataService;

    /**
     * Aggregates all data from person, firestation and medicalrecord JSON files,
     * saves the result to data.json, and returns the merged payload.
     *
     * @return merged Data object containing persons, firestations and medicalrecords
     */
    @GetMapping("/data")
    public ResponseEntity<?> getData() {
        return dataService.aggregateAndSaveData();
    }
}
