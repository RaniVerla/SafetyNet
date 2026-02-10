package net.example.safetynet.controller;


import net.example.safetynet.model.Medicalrecord;
import net.example.safetynet.service.MedicalRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RequestMapping("/v1")
@RestController
public class MedicalRecordController {

    @Autowired
    private MedicalRecordService medicalRecordService;


    @PostMapping("/medicalrecord")
    public ResponseEntity<String> addMedicalRecord(@RequestBody Medicalrecord medicalrecord)
    {
        return medicalRecordService.addMedicalRecord(medicalrecord);

    }


    @GetMapping("/medicalrecord")
    public Set<Medicalrecord> getAllPersons()
    {
        return medicalRecordService.getMedicalRecords();
    }
}
