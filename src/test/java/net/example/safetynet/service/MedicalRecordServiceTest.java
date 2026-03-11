package net.example.safetynet.service;

import net.example.safetynet.model.Medicalrecord;
import net.example.safetynet.utils.ReadFromFileUtil;
import net.example.safetynet.utils.WriteToFileUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicalRecordServiceTest {

    @Mock
    private ReadFromFileUtil readFromFileUtil;

    @Mock
    private WriteToFileUtil writeToFileUtil;

    @InjectMocks
    private MedicalRecordService medicalRecordService;

    @Test
    void addMedicalRecord_success() {

        List<Medicalrecord> list = new ArrayList<>();

        when(readFromFileUtil.readFromFile(any(), any()))
                .thenReturn((List) list);

        ResponseEntity<String> response =
                medicalRecordService.addMedicalRecord(new Medicalrecord());

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Medical Record added successfully", response.getBody());

        verify(writeToFileUtil, times(1))
                .writeToFile(any(), any());
    }

    @Test
    void getMedicalRecords_success() {

        List<Medicalrecord> list = List.of(new Medicalrecord(), new Medicalrecord());

        when(readFromFileUtil.readFromFile(any(), any()))
                .thenReturn((List) list);

        List<Medicalrecord> result = medicalRecordService.getMedicalRecords();

        assertEquals(2, result.size());

        verify(readFromFileUtil, times(1))
                .readFromFile(any(), any());
    }

    @Test
    void updateMedicalRecord_success() {

        Medicalrecord existing = new Medicalrecord();
        existing.setFirstName("John");
        existing.setLastName("Boyd");

        List<Medicalrecord> list = new ArrayList<>();
        list.add(existing);

        Medicalrecord updated = new Medicalrecord();
        updated.setFirstName("John");
        updated.setLastName("Boyd");

        when(readFromFileUtil.readFromFile(any(), any()))
                .thenReturn((List) list);

        ResponseEntity<String> response =
                medicalRecordService.updateMedicalRecord(updated, "John", "Boyd");

        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        verify(writeToFileUtil, times(1))
                .writeToFile(any(), any());
    }

    @Test
    void updateMedicalRecord_notFound() {

        when(readFromFileUtil.readFromFile(any(), any()))
                .thenReturn(new ArrayList<>());

        Medicalrecord updated = new Medicalrecord();
        updated.setFirstName("John");
        updated.setLastName("Boyd");

        ResponseEntity<String> response =
                medicalRecordService.updateMedicalRecord(updated, "John", "Boyd");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deleteMedicalRecord_success() {

        Medicalrecord existing = new Medicalrecord();
        existing.setFirstName("John");
        existing.setLastName("Boyd");

        List<Medicalrecord> list = new ArrayList<>();
        list.add(existing);

        when(readFromFileUtil.readFromFile(any(), any()))
                .thenReturn((List) list);

        ResponseEntity<String> response =
                medicalRecordService.deleteMedicalRecord("Boyd", "John");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verify(writeToFileUtil, times(1))
                .writeToFile(any(), any());
    }

    @Test
    void deleteMedicalRecord_notFound() {

        when(readFromFileUtil.readFromFile(any(), any()))
                .thenReturn(new ArrayList<>());

        ResponseEntity<String> response =
                medicalRecordService.deleteMedicalRecord("Boyd", "John");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
