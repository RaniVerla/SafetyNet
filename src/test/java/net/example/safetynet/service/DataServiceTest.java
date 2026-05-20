package net.example.safetynet.service;

import net.example.safetynet.model.Data;
import net.example.safetynet.model.Firestation;
import net.example.safetynet.model.Medicalrecord;
import net.example.safetynet.model.Person;
import net.example.safetynet.utils.ReadFromFileUtil;
import net.example.safetynet.utils.WriteToFileUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tools.jackson.core.type.TypeReference;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataServiceTest {

    @Mock
    private ReadFromFileUtil readFromFileUtil;

    @Mock
    private WriteToFileUtil writeToFileUtil;

    @InjectMocks
    private DataService dataService;

    private List<Person> mockPersons;
    private List<Firestation> mockFirestations;
    private List<Medicalrecord> mockMedicalRecords;

    @BeforeEach
    void setUp() {
        mockPersons = List.of(
                new Person("John",  "Boyd", "1509 Culver St", "Culver", "97451", "841-874-6512", "jaboyd@email.com"),
                new Person("Jacob", "Boyd", "1509 Culver St", "Culver", "97451", "841-874-6513", "drk@email.com")
        );
        mockFirestations = List.of(
                new Firestation("1509 Culver St", 3),
                new Firestation("908 73rd St", 1)
        );
        mockMedicalRecords = List.of(
                new Medicalrecord("John",  "Boyd", "03/06/1984", new String[]{"aznol:350mg"}, new String[]{"nillacilan"}),
                new Medicalrecord("Jacob", "Boyd", "03/06/1989", new String[]{"pharmacol:5000mg"}, new String[]{})
        );
    }

    @Test
    @DisplayName("aggregateAndSaveData — returns 200 with merged Data on success")
    void aggregateAndSaveData_shouldReturn200WithMergedData() throws Exception {
        when(readFromFileUtil.readFromInputStream(any(), any(TypeReference.class)))
                .thenReturn(mockPersons)
                .thenReturn(mockFirestations)
                .thenReturn(mockMedicalRecords);
        doNothing().when(writeToFileUtil).writeObjectToFile(any(), any());

        ResponseEntity<?> response = dataService.aggregateAndSaveData();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        Data data = (Data) response.getBody();
        assertEquals(2, data.getPersons().size());
        assertEquals(2, data.getFirestations().size());
        assertEquals(2, data.getMedicalrecords().size());
    }

    @Test
    @DisplayName("aggregateAndSaveData — persons list is correctly populated")
    void aggregateAndSaveData_shouldPopulatePersonsCorrectly() throws Exception {
        when(readFromFileUtil.readFromInputStream(any(), any(TypeReference.class)))
                .thenReturn(mockPersons)
                .thenReturn(mockFirestations)
                .thenReturn(mockMedicalRecords);
        doNothing().when(writeToFileUtil).writeObjectToFile(any(), any());

        ResponseEntity<?> response = dataService.aggregateAndSaveData();
        Data data = (Data) response.getBody();

        assertNotNull(data);
        assertEquals("John",  data.getPersons().get(0).getFirstName());
        assertEquals("Boyd",  data.getPersons().get(0).getLastName());
        assertEquals("Jacob", data.getPersons().get(1).getFirstName());
    }

    @Test
    @DisplayName("aggregateAndSaveData — firestations list is correctly populated")
    void aggregateAndSaveData_shouldPopulateFirestationsCorrectly() throws Exception {
        when(readFromFileUtil.readFromInputStream(any(), any(TypeReference.class)))
                .thenReturn(mockPersons)
                .thenReturn(mockFirestations)
                .thenReturn(mockMedicalRecords);
        doNothing().when(writeToFileUtil).writeObjectToFile(any(), any());

        ResponseEntity<?> response = dataService.aggregateAndSaveData();
        Data data = (Data) response.getBody();

        assertNotNull(data);
        assertEquals("1509 Culver St", data.getFirestations().get(0).getAddress());
        assertEquals(3, data.getFirestations().get(0).getStation());
        assertEquals("908 73rd St", data.getFirestations().get(1).getAddress());
        assertEquals(1, data.getFirestations().get(1).getStation());
    }

    @Test
    @DisplayName("aggregateAndSaveData — medicalrecords list is correctly populated")
    void aggregateAndSaveData_shouldPopulateMedicalRecordsCorrectly() throws Exception {
        when(readFromFileUtil.readFromInputStream(any(), any(TypeReference.class)))
                .thenReturn(mockPersons)
                .thenReturn(mockFirestations)
                .thenReturn(mockMedicalRecords);
        doNothing().when(writeToFileUtil).writeObjectToFile(any(), any());

        ResponseEntity<?> response = dataService.aggregateAndSaveData();
        Data data = (Data) response.getBody();

        assertNotNull(data);
        assertEquals("John", data.getMedicalrecords().get(0).getFirstName());
        assertEquals("03/06/1984", data.getMedicalrecords().get(0).getBirthdate());
        assertArrayEquals(new String[]{"aznol:350mg"}, data.getMedicalrecords().get(0).getMedications());
        assertArrayEquals(new String[]{"nillacilan"}, data.getMedicalrecords().get(0).getAllergies());
    }

    @Test
    @DisplayName("aggregateAndSaveData — writeObjectToFile is called exactly once")
    void aggregateAndSaveData_shouldWriteToFileExactlyOnce() throws Exception {
        when(readFromFileUtil.readFromInputStream(any(), any(TypeReference.class)))
                .thenReturn(mockPersons)
                .thenReturn(mockFirestations)
                .thenReturn(mockMedicalRecords);
        doNothing().when(writeToFileUtil).writeObjectToFile(any(), any());

        dataService.aggregateAndSaveData();

        verify(writeToFileUtil, times(1)).writeObjectToFile(any(), any());
    }

    @Test
    @DisplayName("aggregateAndSaveData — readFromInputStream is called three times (persons, firestations, medicalrecords)")
    void aggregateAndSaveData_shouldReadFromInputStreamThreeTimes() throws Exception {
        when(readFromFileUtil.readFromInputStream(any(), any(TypeReference.class)))
                .thenReturn(mockPersons)
                .thenReturn(mockFirestations)
                .thenReturn(mockMedicalRecords);
        doNothing().when(writeToFileUtil).writeObjectToFile(any(), any());

        dataService.aggregateAndSaveData();

        verify(readFromFileUtil, times(3)).readFromInputStream(any(), any());
    }

    @Test
    @DisplayName("aggregateAndSaveData — returns 500 when readFromInputStream throws an exception")
    void aggregateAndSaveData_shouldReturn500OnReadException() throws Exception {
        when(readFromFileUtil.readFromInputStream(any(), any(TypeReference.class)))
                .thenThrow(new RuntimeException("File read error"));

        ResponseEntity<?> response = dataService.aggregateAndSaveData();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().toString().contains("Error aggregating data"));
    }

    @Test
    @DisplayName("aggregateAndSaveData — returns 500 when writeObjectToFile throws an exception")
    void aggregateAndSaveData_shouldReturn500OnWriteException() throws Exception {
        when(readFromFileUtil.readFromInputStream(any(), any(TypeReference.class)))
                .thenReturn(mockPersons)
                .thenReturn(mockFirestations)
                .thenReturn(mockMedicalRecords);
        doThrow(new RuntimeException("Write error")).when(writeToFileUtil).writeObjectToFile(any(), any());

        ResponseEntity<?> response = dataService.aggregateAndSaveData();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Error aggregating data"));
    }

    @Test
    @DisplayName("aggregateAndSaveData — handles empty source files gracefully")
    void aggregateAndSaveData_shouldHandleEmptySourceFiles() throws Exception {
        when(readFromFileUtil.readFromInputStream(any(), any(TypeReference.class)))
                .thenReturn(List.of())
                .thenReturn(List.of())
                .thenReturn(List.of());
        doNothing().when(writeToFileUtil).writeObjectToFile(any(), any());

        ResponseEntity<?> response = dataService.aggregateAndSaveData();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Data data = (Data) response.getBody();
        assertNotNull(data);
        assertTrue(data.getPersons().isEmpty());
        assertTrue(data.getFirestations().isEmpty());
        assertTrue(data.getMedicalrecords().isEmpty());
    }
}
