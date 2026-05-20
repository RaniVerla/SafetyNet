package net.example.safetynet.service;

import net.example.safetynet.model.Firestation;
import net.example.safetynet.model.FireStationResidents;
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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FireStationServiceTestNew {

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
        // Initialize mock data
        mockFirestations = new ArrayList<>();
        mockFirestations.add(new Firestation("1509 Culver St", 1));
        mockFirestations.add(new Firestation("834 Binoc Ave", 3));
        mockFirestations.add(new Firestation("748 Townings Dr", 3));

        mockPersons = new ArrayList<>();
        mockPersons.add(createPerson("John", "Boyd", "1509 Culver St", "841-874-6513"));
        mockPersons.add(createPerson("Jacob", "Boyd", "1509 Culver St", "841-874-6513"));
        mockPersons.add(createPerson("Tenley", "Boyd", "1509 Culver St", "841-874-6513"));
        mockPersons.add(createPerson("John", "Doe", "834 Binoc Ave", "555-1234"));

        mockMedicalRecords = new ArrayList<>();
        mockMedicalRecords.add(createMedicalRecord("John", "Boyd", "03/06/1984"));
        mockMedicalRecords.add(createMedicalRecord("Jacob", "Boyd", "03/06/1989"));
        mockMedicalRecords.add(createMedicalRecord("Tenley", "Boyd", "02/18/2012")); // Child
        mockMedicalRecords.add(createMedicalRecord("John", "Doe", "05/15/1980"));
    }

    // Helper methods to create test objects
    private Person createPerson(String firstName, String lastName, String address, String phone) {
        Person person = new Person();
        person.setFirstName(firstName);
        person.setLastName(lastName);
        person.setAddress(address);
        person.setPhone(phone);
        return person;
    }

    private Medicalrecord createMedicalRecord(String firstName, String lastName, String birthdate) {
        Medicalrecord record = new Medicalrecord();
        record.setFirstName(firstName);
        record.setLastName(lastName);
        record.setBirthdate(birthdate);
        return record;
    }

    @Test
    @DisplayName("Testing the success path")
    void addFireStation() throws Exception {

        Firestation newFireStation = new Firestation("1509 Culver St", 1);

        doNothing().when(writeToFileUtil)
                .writeToFile(any(), any());

        ResponseEntity<String> response =
                fireStationService.addFireStation(newFireStation);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        verify(readFromFileUtil, times(1))
                .readFromFile(any(), any());

        verify(writeToFileUtil, times(1))
                .writeToFile(any(), any());
    }

    @Test
    @DisplayName("Testing the Internal server error")
    void addFirestation_5XX_Error() throws Exception {

        Firestation newFireStation = new Firestation("1509 Culver St", 1);

        when(readFromFileUtil.readFromFile(any(), any()))
                .thenReturn(null);

        ResponseEntity<String> response =
                fireStationService.addFireStation(newFireStation);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void deleteFireStation_success() {

        Firestation existing = new Firestation();
        existing.setStation(1);

        List<Firestation> list = new ArrayList<>();
        list.add(existing);

        when(readFromFileUtil.readFromFile(any(), any()))
                .thenReturn((List)list);

        ResponseEntity<String> response =
                fireStationService.deleteFireStation("1");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verify(writeToFileUtil, times(1))
                .writeToFile(any(), any());
    }

    @Test
    void getAllFireStations_success() {

        List<Firestation> list = List.of(new Firestation(), new Firestation());

        when(readFromFileUtil.readFromFile(any(), any()))
                .thenReturn((List)list);

        List<Firestation> result = fireStationService.getAllFireStations();

        assertEquals(2, result.size());

        verify(readFromFileUtil, times(1))
                .readFromFile(any(), any());
    }

    @Test
    void updateFireStation_success() {

        Firestation existing = new Firestation();
        existing.setStation(1);

        List<Firestation> list = new ArrayList<>();
        list.add(existing);

        Firestation updated = new Firestation();
        updated.setStation(1);
        updated.setAddress("New Address");

        when(readFromFileUtil.readFromFile(any(), any()))
                .thenReturn((List)list);

        ResponseEntity<String> response =
                fireStationService.updateFireStation(updated, "1");

        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        verify(writeToFileUtil, times(1))
                .writeToFile(any(), any());
    }

    @Test
    void deleteFireStation_notFound() {

        when(readFromFileUtil.readFromFile(any(), any()))
                .thenReturn(new ArrayList<>());

        ResponseEntity<String> response =
                fireStationService.deleteFireStation("1");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ======================= Tests for getResidentsByStationNumber =======================

    /**
     * Test: Get residents for a valid station number
     * Verifies that residents from the correct station are returned
     */
    @Test
    @DisplayName("Get residents for station 1 - should return residents at covered addresses")
    void getResidentsByStationNumber_ValidStation() throws Exception {
        // Given
        net.example.safetynet.model.Data data = new net.example.safetynet.model.Data(
                mockPersons, mockFirestations, mockMedicalRecords);
        when(readFromFileUtil.readObjectFromInputStream(any(), any())).thenReturn(data);

        // When
        FireStationResidents result = fireStationService.getResidentsByStationNumber("1");

        // Then
        assertNotNull(result);
        assertNotNull(result.getResidents());
        // Station 1 covers "1509 Culver St" — John, Jacob, Tenley Boyd
        assertEquals(3, result.getResidents().size());
    }

    /**
     * Test: Get residents for a station with no matching addresses
     * Verifies that an empty list is returned for a non-existent station
     */
    @Test
    @DisplayName("Get residents for non-existent station - should return empty list")
    void getResidentsByStationNumber_NoResidents() throws Exception {
        // Given
        net.example.safetynet.model.Data data = new net.example.safetynet.model.Data(
                mockPersons, mockFirestations, mockMedicalRecords);
        when(readFromFileUtil.readObjectFromInputStream(any(), any())).thenReturn(data);

        // When
        FireStationResidents result = fireStationService.getResidentsByStationNumber("99");

        // Then
        assertNotNull(result);
        assertTrue(result.getResidents().isEmpty());
        assertEquals(0, result.getAdultCount());
        assertEquals(0, result.getChildCount());
    }

    /**
     * Test: Adult count calculation
     * Verifies that adults (over 18 years) are counted correctly
     */
    @Test
    @DisplayName("Adult count should be calculated correctly")
    void getResidentsByStationNumber_AdultCountCorrect() throws Exception {
        // Given — station 1 covers "1509 Culver St": John (1984), Jacob (1989) are adults; Tenley (2012) is a child
        net.example.safetynet.model.Data data = new net.example.safetynet.model.Data(
                mockPersons, mockFirestations, mockMedicalRecords);
        when(readFromFileUtil.readObjectFromInputStream(any(), any())).thenReturn(data);

        // When
        FireStationResidents result = fireStationService.getResidentsByStationNumber("1");

        // Then
        assertEquals(2, result.getAdultCount());
    }

    /**
     * Test: Child count calculation
     * Verifies that children (18 years or younger) are counted correctly
     */
    @Test
    @DisplayName("Child count should be calculated correctly")
    void getResidentsByStationNumber_ChildCountCorrect() throws Exception {
        // Given — Tenley Boyd (02/18/2012) is the only child at station 1
        net.example.safetynet.model.Data data = new net.example.safetynet.model.Data(
                mockPersons, mockFirestations, mockMedicalRecords);
        when(readFromFileUtil.readObjectFromInputStream(any(), any())).thenReturn(data);

        // When
        FireStationResidents result = fireStationService.getResidentsByStationNumber("1");

        // Then
        assertEquals(1, result.getChildCount());
    }

    /**
     * Test: Resident information structure
     * Verifies that each resident has all required fields populated
     */
    @Test
    @DisplayName("Each resident should have complete information")
    void getResidentsByStationNumber_ResidentInfoComplete() throws Exception {
        // Given
        List<Firestation> firestations = List.of(new Firestation("892 Downing Ct", 2));
        List<Person> persons = List.of(
                new Person("Sophia", "Zemicks", "892 Downing Ct", "Culver", "97451", "841-874-7878", "soph@email.com")
        );
        List<Medicalrecord> medicalRecords = List.of(
                createMedicalRecord("Sophia", "Zemicks", "03/06/1988")
        );

        net.example.safetynet.model.Data data = new net.example.safetynet.model.Data(
                persons, firestations, medicalRecords);
        when(readFromFileUtil.readObjectFromInputStream(any(), any())).thenReturn(data);

        // When
        FireStationResidents result = fireStationService.getResidentsByStationNumber("2");

        // Then
        assertNotNull(result);
        assertEquals(1, result.getResidents().size());

        FireStationResidents.ResidentInfo resident = result.getResidents().get(0);
        assertEquals("Sophia", resident.getFirstName());
        assertEquals("Zemicks", resident.getLastName());
        assertEquals("892 Downing Ct", resident.getAddress());
        assertEquals("841-874-7878", resident.getPhone());
    }

    /**
     * Test: Invalid station number format
     * Verifies that a non-numeric station number is handled gracefully
     */
    @Test
    @DisplayName("Invalid station number format should return empty response")
    void getResidentsByStationNumber_InvalidStationNumber() throws Exception {
        // When — invalid format is caught before any file read
        FireStationResidents result = fireStationService.getResidentsByStationNumber("abc");

        // Then
        assertNotNull(result);
        assertTrue(result.getResidents().isEmpty());
        assertEquals(0, result.getAdultCount());
        assertEquals(0, result.getChildCount());
        verify(readFromFileUtil, never()).readObjectFromInputStream(any(), any());
    }

    /**
     * Test: Exception during file reading
     * Verifies that a valid empty response is returned even when an exception occurs
     */
    @Test
    @DisplayName("Should return valid empty response on exception")
    void getResidentsByStationNumber_HandlesException() throws Exception {
        // Given
        when(readFromFileUtil.readObjectFromInputStream(any(), any()))
                .thenThrow(new RuntimeException("File read error"));

        // When
        FireStationResidents result = fireStationService.getResidentsByStationNumber("1");

        // Then
        assertNotNull(result);
        assertNotNull(result.getResidents());
        assertTrue(result.getResidents().isEmpty());
        assertEquals(0, result.getAdultCount());
        assertEquals(0, result.getChildCount());
    }

    /**
     * Test: Empty fire stations list
     * Verifies handling when there are no fire stations in the data source
     */
    @Test
    @DisplayName("Should return empty response when fire stations list is empty")
    void getResidentsByStationNumber_EmptyDatabase() throws Exception {
        // Given — data.json has no firestations
        net.example.safetynet.model.Data data = new net.example.safetynet.model.Data(
                mockPersons, new ArrayList<>(), mockMedicalRecords);
        when(readFromFileUtil.readObjectFromInputStream(any(), any())).thenReturn(data);

        // When
        FireStationResidents result = fireStationService.getResidentsByStationNumber("1");

        // Then
        assertNotNull(result);
        assertTrue(result.getResidents().isEmpty());
        assertEquals(0, result.getAdultCount());
        assertEquals(0, result.getChildCount());
    }

    /**
     * Test: Correct address returned for each resident
     * Verifies that returned residents belong to addresses covered by the requested station
     */
    @Test
    @DisplayName("Residents should have addresses covered by the requested station")
    void getResidentsByStationNumber_ResidentsHaveCorrectAddress() throws Exception {
        // Given — station 3 covers "834 Binoc Ave" and "748 Townings Dr"
        List<Person> persons = List.of(
                new Person("John",  "Doe",    "834 Binoc Ave",  "Culver", "97451", "555-1234", "jdoe@email.com"),
                new Person("Jane",  "Doe",    "748 Townings Dr","Culver", "97451", "555-5678", "jane@email.com"),
                new Person("Other", "Person", "1509 Culver St", "Culver", "97451", "555-9999", "other@email.com")
        );
        List<Medicalrecord> medicalRecords = List.of(
                createMedicalRecord("John",  "Doe",    "05/15/1980"),
                createMedicalRecord("Jane",  "Doe",    "07/22/1990"),
                createMedicalRecord("Other", "Person", "01/01/1985")
        );

        net.example.safetynet.model.Data data = new net.example.safetynet.model.Data(
                persons, mockFirestations, medicalRecords);
        when(readFromFileUtil.readObjectFromInputStream(any(), any())).thenReturn(data);

        // When
        FireStationResidents result = fireStationService.getResidentsByStationNumber("3");

        // Then
        assertNotNull(result);
        assertEquals(2, result.getResidents().size());
        result.getResidents().forEach(r ->
                assertTrue(
                        r.getAddress().equals("834 Binoc Ave") || r.getAddress().equals("748 Townings Dr"),
                        "Unexpected address: " + r.getAddress()
                )
        );
    }

    /**
     * Test: Multiple addresses covered by the same station
     * Verifies that residents from all addresses under a station are included
     */
    @Test
    @DisplayName("Should return residents from all addresses covered by the station")
    void getResidentsByStationNumber_MultipleAddresses() throws Exception {
        // Given — station 3 covers two addresses
        List<Person> persons = List.of(
                new Person("Alice", "Smith", "834 Binoc Ave",   "Culver", "97451", "841-111-1111", "alice@email.com"),
                new Person("Bob",   "Jones", "748 Townings Dr", "Culver", "97451", "841-222-2222", "bob@email.com")
        );
        List<Medicalrecord> medicalRecords = List.of(
                createMedicalRecord("Alice", "Smith", "04/10/1990"),
                createMedicalRecord("Bob",   "Jones", "11/20/1985")
        );

        net.example.safetynet.model.Data data = new net.example.safetynet.model.Data(
                persons, mockFirestations, medicalRecords);
        when(readFromFileUtil.readObjectFromInputStream(any(), any())).thenReturn(data);

        // When
        FireStationResidents result = fireStationService.getResidentsByStationNumber("3");

        // Then
        assertNotNull(result);
        assertEquals(2, result.getResidents().size());
        assertEquals(2, result.getAdultCount());
        assertEquals(0, result.getChildCount());
    }

    /**
     * Test: Person without a medical record is treated as adult
     * Verifies the fallback behaviour when no medical record is found
     */
    @Test
    @DisplayName("Person with no medical record should be counted as adult")
    void getResidentsByStationNumber_NoMedicalRecord_CountedAsAdult() throws Exception {
        // Given — no medical records provided
        List<Firestation> firestations = List.of(new Firestation("1509 Culver St", 1));
        List<Person> persons = List.of(
                new Person("Unknown", "Person", "1509 Culver St", "Culver", "97451", "841-000-0000", "unknown@email.com")
        );

        net.example.safetynet.model.Data data = new net.example.safetynet.model.Data(
                persons, firestations, new ArrayList<>());
        when(readFromFileUtil.readObjectFromInputStream(any(), any())).thenReturn(data);

        // When
        FireStationResidents result = fireStationService.getResidentsByStationNumber("1");

        // Then
        assertNotNull(result);
        assertEquals(1, result.getResidents().size());
        // No medical record → isChild returns false → counted as adult
        assertEquals(1, result.getAdultCount());
        assertEquals(0, result.getChildCount());
    }

}

