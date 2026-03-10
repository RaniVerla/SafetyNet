package net.example.safetynet.controller;

import net.example.safetynet.model.Medicalrecord;
import net.example.safetynet.service.MedicalRecordService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicalRecordControllerTest {

    @Mock
    private MedicalRecordService  medicalRecordService;

    @InjectMocks
    private MedicalRecordController medicalRecordController;

    @Test
    void addMedicalRecord_directCall(){

        Medicalrecord medicalrecord=new Medicalrecord();

        when(medicalRecordService.addMedicalRecord(any()))
                .thenReturn(ResponseEntity.status(HttpStatus.ACCEPTED)
                        .body("Medical Record added successfully"));

        ResponseEntity<String> response = medicalRecordController.addMedicalRecord(medicalrecord);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertEquals("Medical Record added successfully", response.getBody());

    }

    @Test
    void updateMedicalRecord_directCall(){

        Medicalrecord medicalrecord=new Medicalrecord();

        when(medicalRecordService.updateMedicalRecord(any(),any(), any()))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED)
                        .body("Medical Record updated successfully"));

        ResponseEntity<String> response = medicalRecordController
                .updateMedicalRecord(medicalrecord, "Boyd", "John");

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Medical Record updated successfully", response.getBody());

    }

    @Test
    void deleteMedicalRecord_directCall(){


        when(medicalRecordService.deleteMedicalRecord(any(), any()))
                .thenReturn(ResponseEntity.status(HttpStatus.NO_CONTENT)
                        .body("Medical Record deleted successfully"));

        ResponseEntity<String> response = medicalRecordController
                .deleteMedicalRecord("Boyd", "John");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertEquals("Medical Record deleted successfully", response.getBody());

        verify(medicalRecordService, times(1))
                .deleteMedicalRecord("Boyd","John");

    }

    @Test
    void getAllMedicalRecords_directCall(){

       List<Medicalrecord> medicalrecords=List.of(new Medicalrecord());

        when(medicalRecordService.getMedicalRecords())
                .thenReturn(medicalrecords);

        List<Medicalrecord> response = medicalRecordController.getAllPersons();

        assertEquals(1, response.size());
        verify(medicalRecordService, times(1)).getMedicalRecords();
    }


}