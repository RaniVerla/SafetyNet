package net.example.safetynet.controller;


import net.example.safetynet.model.Medicalrecord;
import net.example.safetynet.model.Person;
import net.example.safetynet.service.MedicalRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/v1")
@RestController
public class MedicalRecordController {

    @Autowired
    private MedicalRecordService medicalRecordService;

    /**
     * Creating get method for Medicalrecord controller
     *
     * @param medicalrecord
     * @return
     */
    @PostMapping("/medicalrecord")
    public ResponseEntity<String> addMedicalRecord(@RequestBody Medicalrecord medicalrecord) {
        return medicalRecordService.addMedicalRecord(medicalrecord);

    }


    /**
     * Creating put method for Medicalrecord controller
     *
     * @param medicalrecord
     * @param lastName
     * @return
     */
    @PutMapping("/medicalrecord/{lastName}/{firstName}")
    public ResponseEntity<String> updateMedicalRecord(@RequestBody Medicalrecord medicalrecord, @PathVariable String lastName,
                                                      @PathVariable String firstName) {
        return medicalRecordService.updateMedicalRecord(medicalrecord, firstName, lastName);

    }


    /**
     * Creating Delete method for Medicalrecord controller
     *
     * @param lastName
     * @return
     */
    @DeleteMapping("/medicalrecord/{lastName}/{firstName}")
    public ResponseEntity<String> deleteMedicalRecord(@PathVariable String lastName,
                                                      @PathVariable String firstName) {
        return medicalRecordService.deleteMedicalRecord(lastName,firstName);

    }

    /**
     * Creating get method for Medicalrecord controller
     *
     * @return
     */
    @GetMapping("/medicalrecord")
    public List<Medicalrecord> getAllPersons() {
        return medicalRecordService.getMedicalRecords();
    }
}
