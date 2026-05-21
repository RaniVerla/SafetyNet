package net.example.safetynet.service;

import net.example.safetynet.model.Data;
import net.example.safetynet.model.Firestation;
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
class PhoneAlertServiceTest {

    @Mock
    private ReadFromFileUtil readFromFileUtil;

    @Mock
    private WriteToFileUtil writeToFileUtil;

    @InjectMocks
    private FireStationService fireStationService;

    private List<Firestation> mockFirestations;
    private List<Person> mockPersons;

    @BeforeEach
    void setUp() {
        mockFirestations = List.of(
                new Firestation("644 Gershwin Cir", 1),
                new Firestation("908 73rd St",      1),
                new Firestation("947 E. Rose Dr",   1),
                new Firestation("1509 Culver St",   3),
                new Firestation("834 Binoc Ave",    3)
        );

        mockPersons = List.of(
                new Person("Peter",    "Duncan",  "644 Gershwin Cir", "Culver", "97451", "841-874-6512", "peter@email.com"),
                new Person("Reginold", "Walker",  "908 73rd St",      "Culver", "97451", "841-874-8547", "reg@email.com"),
                new Person("Jamie",    "Peters",  "908 73rd St",      "Culver", "97451", "841-874-7462", "jpeter@email.com"),
                new Person("Brian",    "Stelzer", "947 E. Rose Dr",   "Culver", "97451", "841-874-7784", "bstel@email.com"),
                new Person("John",     "Boyd",    "1509 Culver St",   "Culver", "97451", "841-874-6512", "jaboyd@email.com"),
                new Person("Tessa",    "Carman",  "834 Binoc Ave",    "Culver", "97451", "841-874-6512", "tenz@email.com")
        );
    }

    private Data buildData(List<Person> persons, List<Firestation> firestations) {
        return new Data(persons, firestations, new ArrayList<>());
    }

    // ======================= Tests for getPhoneNumbersByStationNumber =======================

    @Test
    @DisplayName("Returns phone numbers for all persons covered by station 1")
    void getPhoneNumbers_shouldReturnPhonesForValidStation() throws Exception {
        when(readFromFileUtil.readObjectFromInputStream(any(), any()))
                .thenReturn(buildData(mockPersons, mockFirestations));

        List<String> phones = fireStationService.getPhoneNumbersByStationNumber("1");

        assertNotNull(phones);
        assertEquals(4, phones.size());
        assertTrue(phones.contains("841-874-6512")); // Peter Duncan
        assertTrue(phones.contains("841-874-8547")); // Reginold Walker
        assertTrue(phones.contains("841-874-7462")); // Jamie Peters
        assertTrue(phones.contains("841-874-7784")); // Brian Stelzer
    }

    @Test
    @DisplayName("Returns phone numbers for all persons covered by station 3")
    void getPhoneNumbers_shouldReturnPhonesForStation3() throws Exception {
        when(readFromFileUtil.readObjectFromInputStream(any(), any()))
                .thenReturn(buildData(mockPersons, mockFirestations));

        List<String> phones = fireStationService.getPhoneNumbersByStationNumber("3");

        assertNotNull(phones);
        assertEquals(2, phones.size());
        assertTrue(phones.contains("841-874-6512")); // John Boyd / Tessa Carman
    }

    @Test
    @DisplayName("Returns empty list when station number does not match any firestation")
    void getPhoneNumbers_shouldReturnEmptyForNonExistentStation() throws Exception {
        when(readFromFileUtil.readObjectFromInputStream(any(), any()))
                .thenReturn(buildData(mockPersons, mockFirestations));

        List<String> phones = fireStationService.getPhoneNumbersByStationNumber("99");

        assertNotNull(phones);
        assertTrue(phones.isEmpty());
    }

    @Test
    @DisplayName("Returns empty list for invalid (non-numeric) station number")
    void getPhoneNumbers_shouldReturnEmptyForInvalidStationNumber() throws Exception {
        List<String> phones = fireStationService.getPhoneNumbersByStationNumber("abc");

        assertNotNull(phones);
        assertTrue(phones.isEmpty());
        // Format validation happens before any file read
        verify(readFromFileUtil, never()).readObjectFromInputStream(any(), any());
    }

    @Test
    @DisplayName("Returns empty list when firestations list is empty")
    void getPhoneNumbers_shouldReturnEmptyWhenNoFirestations() throws Exception {
        when(readFromFileUtil.readObjectFromInputStream(any(), any()))
                .thenReturn(buildData(mockPersons, new ArrayList<>()));

        List<String> phones = fireStationService.getPhoneNumbersByStationNumber("1");

        assertNotNull(phones);
        assertTrue(phones.isEmpty());
    }

    @Test
    @DisplayName("Returns empty list when persons list is empty")
    void getPhoneNumbers_shouldReturnEmptyWhenNoPersons() throws Exception {
        when(readFromFileUtil.readObjectFromInputStream(any(), any()))
                .thenReturn(buildData(new ArrayList<>(), mockFirestations));

        List<String> phones = fireStationService.getPhoneNumbersByStationNumber("1");

        assertNotNull(phones);
        assertTrue(phones.isEmpty());
    }

    @Test
    @DisplayName("Includes duplicate phone numbers when multiple persons share the same phone")
    void getPhoneNumbers_shouldIncludeDuplicatesWhenPersonsSharePhone() throws Exception {
        List<Person> persons = List.of(
                new Person("Shawna",  "Stelzer", "947 E. Rose Dr", "Culver", "97451", "841-874-7784", "ssanw@email.com"),
                new Person("Kendrik", "Stelzer", "947 E. Rose Dr", "Culver", "97451", "841-874-7784", "bstel@email.com"),
                new Person("Brian",   "Stelzer", "947 E. Rose Dr", "Culver", "97451", "841-874-7784", "bstel@email.com")
        );
        List<Firestation> firestations = List.of(new Firestation("947 E. Rose Dr", 1));

        when(readFromFileUtil.readObjectFromInputStream(any(), any()))
                .thenReturn(buildData(persons, firestations));

        List<String> phones = fireStationService.getPhoneNumbersByStationNumber("1");

        // All three persons are returned even though they share the same number
        assertNotNull(phones);
        assertEquals(3, phones.size());
        assertEquals(3, phones.stream().filter("841-874-7784"::equals).count());
    }

    @Test
    @DisplayName("Only returns phones for persons at addresses covered by the requested station")
    void getPhoneNumbers_shouldOnlyReturnPhonesForCoveredAddresses() throws Exception {
        List<Person> persons = List.of(
                new Person("John",  "Boyd",   "1509 Culver St",   "Culver", "97451", "841-874-6512", "jaboyd@email.com"),
                new Person("Other", "Person", "489 Manchester St","Culver", "97451", "841-874-9845", "other@email.com")
        );
        List<Firestation> firestations = List.of(
                new Firestation("1509 Culver St",   3),
                new Firestation("489 Manchester St", 4)
        );

        when(readFromFileUtil.readObjectFromInputStream(any(), any()))
                .thenReturn(buildData(persons, firestations));

        List<String> phones = fireStationService.getPhoneNumbersByStationNumber("3");

        assertNotNull(phones);
        assertEquals(1, phones.size());
        assertTrue(phones.contains("841-874-6512"));
        assertFalse(phones.contains("841-874-9845"));
    }

    @Test
    @DisplayName("Returns empty list gracefully when an exception occurs during data read")
    void getPhoneNumbers_shouldReturnEmptyOnException() throws Exception {
        when(readFromFileUtil.readObjectFromInputStream(any(), any()))
                .thenThrow(new RuntimeException("File read error"));

        List<String> phones = fireStationService.getPhoneNumbersByStationNumber("1");

        assertNotNull(phones);
        assertTrue(phones.isEmpty());
    }

    @Test
    @DisplayName("Station number with surrounding whitespace is handled correctly")
    void getPhoneNumbers_shouldHandleStationNumberWithWhitespace() throws Exception {
        when(readFromFileUtil.readObjectFromInputStream(any(), any()))
                .thenReturn(buildData(mockPersons, mockFirestations));

        List<String> phones = fireStationService.getPhoneNumbersByStationNumber("  1  ");

        assertNotNull(phones);
        assertEquals(4, phones.size());
    }
}
