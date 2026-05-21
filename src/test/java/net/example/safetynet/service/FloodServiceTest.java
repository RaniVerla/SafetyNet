package net.example.safetynet.service;

import net.example.safetynet.model.Data;
import net.example.safetynet.model.Firestation;
import net.example.safetynet.model.FloodHousehold;
import net.example.safetynet.model.FloodResident;
import net.example.safetynet.model.FloodResponse;
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
class FloodServiceTest {

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
                new Firestation("834 Binoc Ave",  3),
                new Firestation("908 73rd St",    1),
                new Firestation("892 Downing Ct", 2)
        );

        mockPersons = List.of(
                new Person("John",    "Boyd",    "1509 Culver St", "Culver", "97451", "841-874-6512", "jaboyd@email.com"),
                new Person("Jacob",   "Boyd",    "1509 Culver St", "Culver", "97451", "841-874-6513", "drk@email.com"),
                new Person("Tenley",  "Boyd",    "1509 Culver St", "Culver", "97451", "841-874-6512", "tenz@email.com"),
                new Person("Tessa",   "Carman",  "834 Binoc Ave",  "Culver", "97451", "841-874-6512", "tenz@email.com"),
                new Person("Reginold","Walker",  "908 73rd St",    "Culver", "97451", "841-874-8547", "reg@email.com"),
                new Person("Jamie",   "Peters",  "908 73rd St",    "Culver", "97451", "841-874-7462", "jpeter@email.com"),
                new Person("Sophia",  "Zemicks", "892 Downing Ct", "Culver", "97451", "841-874-7878", "soph@email.com")
        );

        mockMedicalRecords = List.of(
                new Medicalrecord("John",    "Boyd",    "03/06/1984", new String[]{"aznol:350mg", "hydrapermazol:100mg"}, new String[]{"nillacilan"}),
                new Medicalrecord("Jacob",   "Boyd",    "03/06/1989", new String[]{"pharmacol:5000mg"}, new String[]{}),
                new Medicalrecord("Tenley",  "Boyd",    "02/18/2012", new String[]{}, new String[]{"peanut"}),
                new Medicalrecord("Tessa",   "Carman",  "02/18/2012", new String[]{}, new String[]{}),
                new Medicalrecord("Reginold","Walker",  "08/30/1979", new String[]{"thradox:700mg"}, new String[]{"illisoxian"}),
                new Medicalrecord("Jamie",   "Peters",  "03/06/1982", new String[]{}, new String[]{}),
                new Medicalrecord("Sophia",  "Zemicks", "03/06/1988", new String[]{"aznol:60mg"}, new String[]{"peanut", "shellfish"})
        );
    }

    private Data buildData() {
        return new Data(mockPersons, mockFirestations, mockMedicalRecords);
    }

    // ======================= Tests for getHouseholdsByStations =======================

    @Test
    @DisplayName("Returns households grouped by address for a single station")
    void getHouseholdsByStations_shouldReturnHouseholdsForSingleStation() throws Exception {
        when(readFromFileUtil.readObjectFromInputStream(any(), any())).thenReturn(buildData());

        FloodResponse response = fireStationService.getHouseholdsByStations("3");

        assertNotNull(response);
        assertNotNull(response.getHouseholds());
        // Station 3 covers 1509 Culver St and 834 Binoc Ave → 2 households
        assertEquals(2, response.getHouseholds().size());
    }

    @Test
    @DisplayName("Returns households for multiple stations")
    void getHouseholdsByStations_shouldReturnHouseholdsForMultipleStations() throws Exception {
        when(readFromFileUtil.readObjectFromInputStream(any(), any())).thenReturn(buildData());

        FloodResponse response = fireStationService.getHouseholdsByStations("1,2");

        assertNotNull(response);
        // Station 1 → 908 73rd St, Station 2 → 892 Downing Ct → 2 households
        assertEquals(2, response.getHouseholds().size());
    }

    @Test
    @DisplayName("Residents are grouped correctly under their address")
    void getHouseholdsByStations_shouldGroupResidentsByAddress() throws Exception {
        when(readFromFileUtil.readObjectFromInputStream(any(), any())).thenReturn(buildData());

        FloodResponse response = fireStationService.getHouseholdsByStations("3");

        FloodHousehold culverHousehold = response.getHouseholds().stream()
                .filter(h -> h.getAddress().equals("1509 Culver St"))
                .findFirst().orElse(null);

        assertNotNull(culverHousehold);
        assertEquals(3, culverHousehold.getResidents().size());

        FloodHousehold binocHousehold = response.getHouseholds().stream()
                .filter(h -> h.getAddress().equals("834 Binoc Ave"))
                .findFirst().orElse(null);

        assertNotNull(binocHousehold);
        assertEquals(1, binocHousehold.getResidents().size());
        assertEquals("Tessa", binocHousehold.getResidents().get(0).getFirstName());
    }

    @Test
    @DisplayName("Each resident contains name, phone, age, medications and allergies")
    void getHouseholdsByStations_shouldReturnCompleteResidentInfo() throws Exception {
        when(readFromFileUtil.readObjectFromInputStream(any(), any())).thenReturn(buildData());

        FloodResponse response = fireStationService.getHouseholdsByStations("3");

        FloodHousehold culverHousehold = response.getHouseholds().stream()
                .filter(h -> h.getAddress().equals("1509 Culver St"))
                .findFirst().orElse(null);

        assertNotNull(culverHousehold);
        FloodResident john = culverHousehold.getResidents().stream()
                .filter(r -> r.getFirstName().equals("John"))
                .findFirst().orElse(null);

        assertNotNull(john);
        assertEquals("John",         john.getFirstName());
        assertEquals("Boyd",         john.getLastName());
        assertEquals("841-874-6512", john.getPhone());
        assertEquals(42,             john.getAge());
        assertEquals(2,              john.getMedications().size());
        assertTrue(john.getMedications().contains("aznol:350mg"));
        assertTrue(john.getMedications().contains("hydrapermazol:100mg"));
        assertEquals(1,              john.getAllergies().size());
        assertTrue(john.getAllergies().contains("nillacilan"));
    }

    @Test
    @DisplayName("Resident with no medications returns empty medications list")
    void getHouseholdsByStations_shouldReturnEmptyMedicationsWhenNone() throws Exception {
        when(readFromFileUtil.readObjectFromInputStream(any(), any())).thenReturn(buildData());

        FloodResponse response = fireStationService.getHouseholdsByStations("3");

        FloodHousehold culverHousehold = response.getHouseholds().stream()
                .filter(h -> h.getAddress().equals("1509 Culver St"))
                .findFirst().orElse(null);

        assertNotNull(culverHousehold);
        FloodResident tenley = culverHousehold.getResidents().stream()
                .filter(r -> r.getFirstName().equals("Tenley"))
                .findFirst().orElse(null);

        assertNotNull(tenley);
        assertTrue(tenley.getMedications().isEmpty());
        assertTrue(tenley.getAllergies().contains("peanut"));
    }

    @Test
    @DisplayName("Returns empty response for a station that covers no addresses")
    void getHouseholdsByStations_shouldReturnEmptyForUnknownStation() throws Exception {
        when(readFromFileUtil.readObjectFromInputStream(any(), any())).thenReturn(buildData());

        FloodResponse response = fireStationService.getHouseholdsByStations("99");

        assertNotNull(response);
        assertTrue(response.getHouseholds().isEmpty());
    }

    @Test
    @DisplayName("Returns empty response when all station numbers are invalid")
    void getHouseholdsByStations_shouldReturnEmptyForAllInvalidStationNumbers() throws Exception {
        FloodResponse response = fireStationService.getHouseholdsByStations("abc,xyz");

        assertNotNull(response);
        assertTrue(response.getHouseholds().isEmpty());
        // No file read should happen when no valid station numbers are parsed
        verify(readFromFileUtil, never()).readObjectFromInputStream(any(), any());
    }

    @Test
    @DisplayName("Handles station numbers with surrounding whitespace")
    void getHouseholdsByStations_shouldHandleSpacedStationNumbers() throws Exception {
        when(readFromFileUtil.readObjectFromInputStream(any(), any())).thenReturn(buildData());

        FloodResponse response = fireStationService.getHouseholdsByStations(" 1 , 2 ");

        assertNotNull(response);
        assertEquals(2, response.getHouseholds().size());
    }

    @Test
    @DisplayName("Resident with no medical record gets age=-1 and empty lists")
    void getHouseholdsByStations_shouldHandleMissingMedicalRecord() throws Exception {
        List<Person> persons = List.of(
                new Person("Unknown", "Person", "1509 Culver St", "Culver", "97451", "000-000-0000", "u@email.com")
        );
        Data data = new Data(persons, List.of(new Firestation("1509 Culver St", 3)), new ArrayList<>());
        when(readFromFileUtil.readObjectFromInputStream(any(), any())).thenReturn(data);

        FloodResponse response = fireStationService.getHouseholdsByStations("3");

        assertNotNull(response);
        assertEquals(1, response.getHouseholds().size());
        FloodResident resident = response.getHouseholds().get(0).getResidents().get(0);
        assertEquals(-1, resident.getAge());
        assertTrue(resident.getMedications().isEmpty());
        assertTrue(resident.getAllergies().isEmpty());
    }

    @Test
    @DisplayName("Returns empty response gracefully on exception")
    void getHouseholdsByStations_shouldReturnEmptyOnException() throws Exception {
        when(readFromFileUtil.readObjectFromInputStream(any(), any()))
                .thenThrow(new RuntimeException("File read error"));

        FloodResponse response = fireStationService.getHouseholdsByStations("1");

        assertNotNull(response);
        assertTrue(response.getHouseholds().isEmpty());
    }

    @Test
    @DisplayName("Age is calculated correctly from birthdate")
    void getHouseholdsByStations_shouldCalculateAgeCorrectly() throws Exception {
        when(readFromFileUtil.readObjectFromInputStream(any(), any())).thenReturn(buildData());

        FloodResponse response = fireStationService.getHouseholdsByStations("1");

        FloodHousehold household = response.getHouseholds().stream()
                .filter(h -> h.getAddress().equals("908 73rd St"))
                .findFirst().orElse(null);

        assertNotNull(household);
        FloodResident reginold = household.getResidents().stream()
                .filter(r -> r.getFirstName().equals("Reginold"))
                .findFirst().orElse(null);

        assertNotNull(reginold);
        assertEquals(46, reginold.getAge()); // born 08/30/1979, today 2026
        assertTrue(reginold.getMedications().contains("thradox:700mg"));
        assertTrue(reginold.getAllergies().contains("illisoxian"));
    }

    @Test
    @DisplayName("Only persons at covered addresses are included — others are excluded")
    void getHouseholdsByStations_shouldExcludePersonsAtUncoveredAddresses() throws Exception {
        when(readFromFileUtil.readObjectFromInputStream(any(), any())).thenReturn(buildData());

        // Station 2 only covers 892 Downing Ct
        FloodResponse response = fireStationService.getHouseholdsByStations("2");

        assertNotNull(response);
        assertEquals(1, response.getHouseholds().size());
        assertEquals("892 Downing Ct", response.getHouseholds().get(0).getAddress());
        assertEquals("Sophia", response.getHouseholds().get(0).getResidents().get(0).getFirstName());
    }
}
