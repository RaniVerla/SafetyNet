package net.example.safetynet.service;

import net.example.safetynet.model.Data;
import net.example.safetynet.model.FireAlertResponse;
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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FireAlertServiceTest {

    @Mock
    private ReadFromFileUtil readFromFileUtil;

    @Mock
    private WriteToFileUtil writeToFileUtil;

    @InjectMocks
    private FireStationService fireStationService;

    private List<Firestation> mockFirestations;
    private List<Person> mockPersons;
    private List<Medicalrecord> mockMedicalRecords;

    @BeforeEach
    void setUp() {
        mockFirestations = List.of(
                new Firestation("1509 Culver St", 3),
                new Firestation("908 73rd St",    1)
        );

        mockPersons = List.of(
                new Person("John",   "Boyd", "1509 Culver St", "Culver", "97451", "841-874-6512", "jaboyd@email.com"),
                new Person("Jacob",  "Boyd", "1509 Culver St", "Culver", "97451", "841-874-6513", "drk@email.com"),
                new Person("Tenley", "Boyd", "1509 Culver St", "Culver", "97451", "841-874-6512", "tenz@email.com"),
                new Person("Jamie",  "Peters", "908 73rd St",  "Culver", "97451", "841-874-7462", "jpeter@email.com")
        );

        mockMedicalRecords = List.of(
                new Medicalrecord("John",   "Boyd", "03/06/1984", new String[]{"aznol:350mg", "hydrapermazol:100mg"}, new String[]{"nillacilan"}),
                new Medicalrecord("Jacob",  "Boyd", "03/06/1989", new String[]{"pharmacol:5000mg"}, new String[]{}),
                new Medicalrecord("Tenley", "Boyd", "02/18/2012", new String[]{}, new String[]{"peanut"}),
                new Medicalrecord("Jamie",  "Peters", "03/06/1982", new String[]{}, new String[]{})
        );
    }

    private Data buildData() {
        return new Data(mockPersons, mockFirestations, mockMedicalRecords);
    }

    // ======================= Tests for getResidentsByAddress =======================

    @Test
    @DisplayName("Returns residents and correct station number for a known address")
    void getResidentsByAddress_shouldReturnResidentsAndStationNumber() throws Exception {
        when(readFromFileUtil.readObjectFromInputStream(any(), any())).thenReturn(buildData());

        FireAlertResponse response = fireStationService.getResidentsByAddress("1509 Culver St");

        assertNotNull(response);
        assertEquals(3, response.getStationNumber());
        assertEquals(3, response.getResidents().size());
    }

    @Test
    @DisplayName("Each resident contains first name, last name, phone, age, medications and allergies")
    void getResidentsByAddress_shouldReturnCompleteResidentInfo() throws Exception {
        when(readFromFileUtil.readObjectFromInputStream(any(), any())).thenReturn(buildData());

        FireAlertResponse response = fireStationService.getResidentsByAddress("1509 Culver St");

        FireAlertResponse.FireAlertResident john = response.getResidents().stream()
                .filter(r -> r.getFirstName().equals("John"))
                .findFirst().orElse(null);

        assertNotNull(john);
        assertEquals("John",          john.getFirstName());
        assertEquals("Boyd",          john.getLastName());
        assertEquals("841-874-6512",  john.getPhone());
        assertEquals(42,              john.getAge());   // born 03/06/1984, today 2026
        assertEquals(2,               john.getMedications().size());
        assertTrue(john.getMedications().contains("aznol:350mg"));
        assertTrue(john.getMedications().contains("hydrapermazol:100mg"));
        assertEquals(1,               john.getAllergies().size());
        assertTrue(john.getAllergies().contains("nillacilan"));
    }

    @Test
    @DisplayName("Resident with no medications returns empty medications list")
    void getResidentsByAddress_shouldReturnEmptyMedicationsWhenNone() throws Exception {
        when(readFromFileUtil.readObjectFromInputStream(any(), any())).thenReturn(buildData());

        FireAlertResponse response = fireStationService.getResidentsByAddress("1509 Culver St");

        FireAlertResponse.FireAlertResident tenley = response.getResidents().stream()
                .filter(r -> r.getFirstName().equals("Tenley"))
                .findFirst().orElse(null);

        assertNotNull(tenley);
        assertTrue(tenley.getMedications().isEmpty());
        assertEquals(1, tenley.getAllergies().size());
        assertTrue(tenley.getAllergies().contains("peanut"));
    }

    @Test
    @DisplayName("Returns stationNumber=0 and empty residents for an unknown address")
    void getResidentsByAddress_shouldReturnEmptyForUnknownAddress() throws Exception {
        when(readFromFileUtil.readObjectFromInputStream(any(), any())).thenReturn(buildData());

        FireAlertResponse response = fireStationService.getResidentsByAddress("Unknown Address");

        assertNotNull(response);
        assertEquals(0, response.getStationNumber());
        assertTrue(response.getResidents().isEmpty());
    }

    @Test
    @DisplayName("Address matching is case-insensitive")
    void getResidentsByAddress_shouldBeCaseInsensitive() throws Exception {
        when(readFromFileUtil.readObjectFromInputStream(any(), any())).thenReturn(buildData());

        FireAlertResponse response = fireStationService.getResidentsByAddress("1509 culver st");

        assertNotNull(response);
        assertEquals(3, response.getStationNumber());
        assertEquals(3, response.getResidents().size());
    }

    @Test
    @DisplayName("Resident with no medical record gets age=-1 and empty lists")
    void getResidentsByAddress_shouldHandleMissingMedicalRecord() throws Exception {
        List<Person> persons = List.of(
                new Person("Unknown", "Person", "123 Test St", "Culver", "97451", "000-000-0000", "u@email.com")
        );
        Data data = new Data(persons, List.of(new Firestation("123 Test St", 2)), new ArrayList<>());
        when(readFromFileUtil.readObjectFromInputStream(any(), any())).thenReturn(data);

        FireAlertResponse response = fireStationService.getResidentsByAddress("123 Test St");

        assertNotNull(response);
        assertEquals(1, response.getResidents().size());
        FireAlertResponse.FireAlertResident resident = response.getResidents().get(0);
        assertEquals(-1, resident.getAge());
        assertTrue(resident.getMedications().isEmpty());
        assertTrue(resident.getAllergies().isEmpty());
    }

    @Test
    @DisplayName("Returns stationNumber=0 when no firestation covers the address")
    void getResidentsByAddress_shouldReturnStationZeroWhenNoFirestationCoversAddress() throws Exception {
        List<Person> persons = List.of(
                new Person("John", "Boyd", "1509 Culver St", "Culver", "97451", "841-874-6512", "jaboyd@email.com")
        );
        // No firestation entry for this address
        Data data = new Data(persons, new ArrayList<>(), new ArrayList<>());
        when(readFromFileUtil.readObjectFromInputStream(any(), any())).thenReturn(data);

        FireAlertResponse response = fireStationService.getResidentsByAddress("1509 Culver St");

        assertNotNull(response);
        assertEquals(0, response.getStationNumber());
        assertEquals(1, response.getResidents().size()); // person still returned
    }

    @Test
    @DisplayName("Returns empty response gracefully on exception")
    void getResidentsByAddress_shouldReturnEmptyOnException() throws Exception {
        when(readFromFileUtil.readObjectFromInputStream(any(), any()))
                .thenThrow(new RuntimeException("File read error"));

        FireAlertResponse response = fireStationService.getResidentsByAddress("1509 Culver St");

        assertNotNull(response);
        assertEquals(0, response.getStationNumber());
        assertTrue(response.getResidents().isEmpty());
    }

    @Test
    @DisplayName("Only returns residents at the exact queried address, not other addresses")
    void getResidentsByAddress_shouldOnlyReturnResidentsAtQueriedAddress() throws Exception {
        when(readFromFileUtil.readObjectFromInputStream(any(), any())).thenReturn(buildData());

        FireAlertResponse response = fireStationService.getResidentsByAddress("908 73rd St");

        assertNotNull(response);
        assertEquals(1, response.getStationNumber());
        assertEquals(1, response.getResidents().size());
        assertEquals("Jamie", response.getResidents().get(0).getFirstName());
    }

    @Test
    @DisplayName("Age is calculated correctly from birthdate")
    void getResidentsByAddress_shouldCalculateAgeCorrectly() throws Exception {
        when(readFromFileUtil.readObjectFromInputStream(any(), any())).thenReturn(buildData());

        FireAlertResponse response = fireStationService.getResidentsByAddress("1509 Culver St");

        FireAlertResponse.FireAlertResident jacob = response.getResidents().stream()
                .filter(r -> r.getFirstName().equals("Jacob"))
                .findFirst().orElse(null);

        assertNotNull(jacob);
        assertEquals(37, jacob.getAge()); // born 03/06/1989, today 2026
    }
}
