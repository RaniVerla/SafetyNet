package net.example.safetynet.service;

import net.example.safetynet.model.ChildAlertResponse;
import net.example.safetynet.model.Medicalrecord;
import net.example.safetynet.model.Person;
import net.example.safetynet.utils.ReadFromFileUtil;
import net.example.safetynet.utils.WriteToFileUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tools.jackson.core.type.TypeReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonServiceTest {
    @Mock
    private ReadFromFileUtil readFromFileUtil;

    @Mock
    private WriteToFileUtil writeToFileUtil;

    @InjectMocks
    private PersonService personService;

    @Test
    @DisplayName("Testing the success path")
    void addPerson_shouldAddSuccessfully() throws Exception {
        List<Person> persons = new ArrayList<>();

        Person newPerson = new Person("John", "Boyd",
                "1509 Culver St", "Culver", "97451",
                "841-874-6512", "john@email.com");


        doNothing().when(writeToFileUtil)
                .writeToFile(any(), any());

        ResponseEntity<String> response =
                personService.addPerson(newPerson);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        verify(readFromFileUtil, times(1))
                .readFromFile(any(), any());

        verify(writeToFileUtil, times(1))
                .writeToFile(any(), any());
    }

    @Test
    @DisplayName("Testing the Internal server error")
    void addPerson_5XX_Error() throws Exception {
        List<Person> persons = new ArrayList<>();

        Person newPerson = new Person("John", "Boyd",
                "1509 Culver St", "Culver", "97451",
                "841-874-6512", "john@email.com");

        when(readFromFileUtil.readFromFile(any(), any()))
                .thenReturn(null);

        ResponseEntity<String> response =
                personService.addPerson(newPerson);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());


    }

    @Test
    void deletePerson_shouldRemoveSuccessfully() throws Exception {
        List<Person> persons = new ArrayList<>();
        persons.add(new Person("John", "Boyd", "1509 Culver St", "Culver", "97451", "841-874-6512", "john@email.com"));

        when(readFromFileUtil.<Person>readFromFile(any(), any())).thenReturn(persons);
        doNothing().when(writeToFileUtil).writeToFile(any(), any());

        ResponseEntity<String> response = personService.deletePerson("Boyd", "John");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(writeToFileUtil, times(1)).writeToFile(any(), any());
    }

    @Test
    void getAllPersons_shouldReturnList() throws Exception {

        // Arrange
        List<Person> persons = new ArrayList<>();
        persons.add(new Person("John", "Boyd", "1509 Culver St", "Culver", "97451",
                "841-874-6512", "john@email.com"));

        when(readFromFileUtil.<Person>readFromFile(any(), any())).thenReturn(persons);

        // Act
        List<Person> result = personService.getAllPersons();

        // Assert
        assertEquals(1, result.size());
        assertEquals("John", result.get(0).getFirstName());
        assertEquals("Boyd", result.get(0).getLastName());

        // Optional: verify readFromFile was called
        verify(readFromFileUtil, times(1)).readFromFile(any(), any());
    }


    @Test
    void updatePerson_shouldUpdateSuccessfully() throws Exception {

        // Arrange
        List<Person> persons = new ArrayList<>();
        Person existingPerson = new Person("John", "Boyd",
                "1509 Culver St", "Culver", "97451",
                "841-874-6512", "john@email.com");
        persons.add(existingPerson);

        Person updatedPerson = new Person("John", "Boyd",
                "New Address", "Culver", "97451",
                "841-874-6512", "john@email.com");

        when(readFromFileUtil.<Person>readFromFile(any(), any()))
                .thenReturn(persons);

        doNothing().when(writeToFileUtil).writeToFile(any(), any());

        // Act
        ResponseEntity<String> response =
                personService.updatePerson(updatedPerson, "Boyd", "John");

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Person updated successfully", response.getBody());

        // Verify write called
        verify(writeToFileUtil, times(1)).writeToFile(any(), any());

    }

    @Test
    void deleteFireStation_notFound() {

        when(readFromFileUtil.readFromFile(any(), any()))
                .thenReturn(new ArrayList<>());

        ResponseEntity<String> response =
                personService.deletePerson("lname","fname");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("getChildrenAtAddress should return children at 1509 Culver St")
    void getChildrenAtAddress_shouldReturnChildrenAtValidAddress() throws Exception {
        // Given
        String address = "1509 Culver St";

        List<Person> persons = Arrays.asList(
                new Person("John",   "Boyd", "1509 Culver St", "Culver", "97451", "841-874-6512", "jaboyd@email.com"),
                new Person("Jacob",  "Boyd", "1509 Culver St", "Culver", "97451", "841-874-6513", "drk@email.com"),
                new Person("Tenley", "Boyd", "1509 Culver St", "Culver", "97451", "841-874-6512", "tenz@email.com"),
                new Person("Roger",  "Boyd", "1509 Culver St", "Culver", "97451", "841-874-6512", "jaboyd@email.com"),
                new Person("Felicia","Boyd", "1509 Culver St", "Culver", "97451", "841-874-6544", "jaboyd@email.com")
        );
        List<Medicalrecord> medicalRecords = Arrays.asList(
                new Medicalrecord("John",   "Boyd", "03/06/1984", new String[]{"aznol:350mg"}, new String[]{"nillacilan"}),
                new Medicalrecord("Jacob",  "Boyd", "03/06/1989", new String[]{"pharmacol:5000mg"}, new String[]{}),
                new Medicalrecord("Tenley", "Boyd", "02/18/2012", new String[]{}, new String[]{"peanut"}),  // child ~14
                new Medicalrecord("Roger",  "Boyd", "09/06/2017", new String[]{}, new String[]{}),          // child ~8
                new Medicalrecord("Felicia","Boyd", "01/08/1986", new String[]{"tetracyclaz:650mg"}, new String[]{"xilliathal"})
        );

        net.example.safetynet.model.Data data = new net.example.safetynet.model.Data(persons, new ArrayList<>(), medicalRecords);
        when(readFromFileUtil.readObjectFromInputStream(any(), any())).thenReturn(data);

        // When
        ChildAlertResponse response = personService.getChildrenAtAddress(address);

        // Then
        assertNotNull(response);
        assertNotNull(response.getChildren());
        assertEquals(2, response.getChildren().size());

        ChildAlertResponse.ChildInfo child1 = response.getChildren().get(0);
        assertEquals("Tenley", child1.getFirstName());
        assertEquals("Boyd",   child1.getLastName());
        assertEquals(14,       child1.getAge());
        assertEquals(4,        child1.getHouseholdMembers().size()); // John, Jacob, Roger, Felicia

        ChildAlertResponse.ChildInfo child2 = response.getChildren().get(1);
        assertEquals("Roger", child2.getFirstName());
        assertEquals("Boyd",  child2.getLastName());
        assertEquals(8,       child2.getAge());
        assertEquals(4,       child2.getHouseholdMembers().size()); // John, Jacob, Tenley, Felicia
    }

    @Test
    @DisplayName("getChildrenAtAddress should return empty list when no children at address")
    void getChildrenAtAddress_shouldReturnEmptyWhenNoChildren() throws Exception {
        // Given — only adults at this address
        String address = "748 Townings Dr";

        List<Person> persons = Arrays.asList(
                new Person("Foster", "Shepard",  "748 Townings Dr", "Culver", "97451", "841-874-6544", "jaboyd@email.com"),
                new Person("Clive",  "Ferguson", "748 Townings Dr", "Culver", "97451", "841-874-6741", "clivfd@ymail.com")
        );
        List<Medicalrecord> medicalRecords = Arrays.asList(
                new Medicalrecord("Foster", "Shepard",  "01/08/1980", new String[]{}, new String[]{}),
                new Medicalrecord("Clive",  "Ferguson", "03/06/1994", new String[]{}, new String[]{})
        );

        net.example.safetynet.model.Data data = new net.example.safetynet.model.Data(persons, new ArrayList<>(), medicalRecords);
        when(readFromFileUtil.readObjectFromInputStream(any(), any())).thenReturn(data);

        // When
        ChildAlertResponse response = personService.getChildrenAtAddress(address);

        // Then
        assertNotNull(response);
        assertTrue(response.getChildren().isEmpty());
    }

    @Test
    @DisplayName("getChildrenAtAddress should return empty list for nonexistent address")
    void getChildrenAtAddress_shouldReturnEmptyForNonexistentAddress() throws Exception {
        // Given
        net.example.safetynet.model.Data data = new net.example.safetynet.model.Data(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        when(readFromFileUtil.readObjectFromInputStream(any(), any())).thenReturn(data);

        // When
        ChildAlertResponse response = personService.getChildrenAtAddress("Nonexistent Address");

        // Then
        assertNotNull(response);
        assertTrue(response.getChildren().isEmpty());
    }

    @Test
    @DisplayName("getChildrenAtAddress should handle case insensitive address matching")
    void getChildrenAtAddress_shouldHandleCaseInsensitiveMatching() throws Exception {
        // Given — address passed in lowercase, stored in mixed case
        List<Person> persons = Arrays.asList(
                new Person("Tenley", "Boyd", "1509 Culver St", "Culver", "97451", "841-874-6512", "tenz@email.com")
        );
        List<Medicalrecord> medicalRecords = Arrays.asList(
                new Medicalrecord("Tenley", "Boyd", "02/18/2012", new String[]{}, new String[]{"peanut"})
        );

        net.example.safetynet.model.Data data = new net.example.safetynet.model.Data(persons, new ArrayList<>(), medicalRecords);
        when(readFromFileUtil.readObjectFromInputStream(any(), any())).thenReturn(data);

        // When
        ChildAlertResponse response = personService.getChildrenAtAddress("1509 culver st");

        // Then
        assertNotNull(response);
        assertEquals(1, response.getChildren().size());
        assertEquals("Tenley", response.getChildren().get(0).getFirstName());
    }

    @Test
    @DisplayName("getChildrenAtAddress should handle exception gracefully")
    void getChildrenAtAddress_shouldHandleExceptionGracefully() throws Exception {
        // Given
        when(readFromFileUtil.readObjectFromInputStream(any(), any()))
                .thenThrow(new RuntimeException("File read error"));

        // When
        ChildAlertResponse response = personService.getChildrenAtAddress("1509 Culver St");

        // Then
        assertNotNull(response);
        assertTrue(response.getChildren().isEmpty());
    }

    @Test
    @DisplayName("getChildrenAtAddress — child with no medical record should not appear in results")
    void getChildrenAtAddress_personWithNoMedicalRecord_shouldNotBeCountedAsChild() throws Exception {
        // Given — person exists but has no matching medical record
        List<Person> persons = Arrays.asList(
                new Person("Unknown", "Person", "123 Test St", "Culver", "97451", "000-000-0000", "u@email.com")
        );

        net.example.safetynet.model.Data data = new net.example.safetynet.model.Data(persons, new ArrayList<>(), new ArrayList<>());
        when(readFromFileUtil.readObjectFromInputStream(any(), any())).thenReturn(data);

        // When
        ChildAlertResponse response = personService.getChildrenAtAddress("123 Test St");

        // Then — age returns -1 when no record found, so person is excluded from children list
        assertNotNull(response);
        assertTrue(response.getChildren().isEmpty());
    }

    @Test
    @DisplayName("getChildrenAtAddress — household members list excludes the child itself")
    void getChildrenAtAddress_householdMembersShouldExcludeTheChild() throws Exception {
        // Given
        List<Person> persons = Arrays.asList(
                new Person("Kendrik", "Stelzer", "947 E. Rose Dr", "Culver", "97451", "841-874-7784", "bstel@email.com"),
                new Person("Brian",   "Stelzer", "947 E. Rose Dr", "Culver", "97451", "841-874-7784", "bstel@email.com"),
                new Person("Shawna",  "Stelzer", "947 E. Rose Dr", "Culver", "97451", "841-874-7784", "ssanw@email.com")
        );
        List<Medicalrecord> medicalRecords = Arrays.asList(
                new Medicalrecord("Kendrik", "Stelzer", "03/06/2014", new String[]{"noxidian:100mg"}, new String[]{}), // child ~12
                new Medicalrecord("Brian",   "Stelzer", "12/06/1975", new String[]{"ibupurin:200mg"}, new String[]{"nillacilan"}),
                new Medicalrecord("Shawna",  "Stelzer", "07/08/1980", new String[]{}, new String[]{})
        );

        net.example.safetynet.model.Data data = new net.example.safetynet.model.Data(persons, new ArrayList<>(), medicalRecords);
        when(readFromFileUtil.readObjectFromInputStream(any(), any())).thenReturn(data);

        // When
        ChildAlertResponse response = personService.getChildrenAtAddress("947 E. Rose Dr");

        // Then
        assertEquals(1, response.getChildren().size());
        ChildAlertResponse.ChildInfo child = response.getChildren().get(0);
        assertEquals("Kendrik", child.getFirstName());

        // Household members must NOT include Kendrik himself
        List<String> memberNames = child.getHouseholdMembers().stream()
                .map(ChildAlertResponse.HouseholdMember::getFirstName)
                .toList();
        assertFalse(memberNames.contains("Kendrik"));
        assertTrue(memberNames.contains("Brian"));
        assertTrue(memberNames.contains("Shawna"));
        assertEquals(2, child.getHouseholdMembers().size());
    }

    @Test
    @DisplayName("getChildrenAtAddress — only child at address has empty household members list")
    void getChildrenAtAddress_onlyChildAtAddress_hasEmptyHouseholdMembers() throws Exception {
        // Given — single child, no other residents
        List<Person> persons = Arrays.asList(
                new Person("Zach", "Zemicks", "892 Downing Ct", "Culver", "97451", "841-874-7512", "zarc@email.com")
        );
        List<Medicalrecord> medicalRecords = Arrays.asList(
                new Medicalrecord("Zach", "Zemicks", "03/06/2017", new String[]{}, new String[]{}) // child ~9
        );

        net.example.safetynet.model.Data data = new net.example.safetynet.model.Data(persons, new ArrayList<>(), medicalRecords);
        when(readFromFileUtil.readObjectFromInputStream(any(), any())).thenReturn(data);

        // When
        ChildAlertResponse response = personService.getChildrenAtAddress("892 Downing Ct");

        // Then
        assertEquals(1, response.getChildren().size());
        assertEquals("Zach", response.getChildren().get(0).getFirstName());
        assertTrue(response.getChildren().get(0).getHouseholdMembers().isEmpty());
    }

    @Test
    @DisplayName("Testing getPersonInfoByLastName with valid last name")
    void getPersonInfoByLastName_shouldReturnPersonInfoForValidLastName() throws Exception {
        // Given
        String lastName = "Boyd";
        List<Person> persons = Arrays.asList(
                new Person("John", "Boyd", "1509 Culver St", "Culver", "97451", "841-874-6512", "jaboyd@email.com"),
                new Person("Jacob", "Boyd", "1509 Culver St", "Culver", "97451", "841-874-6513", "drk@email.com")
        );
        List<Medicalrecord> medicalRecords = Arrays.asList(
                new Medicalrecord("John", "Boyd", "03/06/1984", new String[]{"aznol:350mg", "hydrapermazol:100mg"}, new String[]{"nillacilan"}),
                new Medicalrecord("Jacob", "Boyd", "03/06/1989", new String[]{"pharmacol:5000mg"}, new String[]{})
        );

        when(readFromFileUtil.readFromFile(any(), any(TypeReference.class))).thenReturn(persons);
        when(readFromFileUtil.readFromInputStream(any(), any(TypeReference.class))).thenReturn(medicalRecords);

        // When
        var response = personService.getPersonInfoByLastName(lastName);

        // Then
        assertNotNull(response);
        assertEquals(2, response.getPersonInfos().size());
        assertEquals("John", response.getPersonInfos().get(0).getFirstName());
        assertEquals("Boyd", response.getPersonInfos().get(0).getLastName());
        assertEquals(42, response.getPersonInfos().get(0).getAge()); // Assuming current year 2026, 2026-1984=42
        assertEquals("jaboyd@email.com", response.getPersonInfos().get(0).getEmail());
        assertEquals(List.of("aznol:350mg", "hydrapermazol:100mg"), response.getPersonInfos().get(0).getMedications());
        assertEquals(List.of("nillacilan"), response.getPersonInfos().get(0).getAllergies());
        assertEquals("Jacob", response.getPersonInfos().get(1).getFirstName());
        assertEquals("Boyd", response.getPersonInfos().get(1).getLastName());
        assertEquals(37, response.getPersonInfos().get(1).getAge()); // Assuming current year 2026, 2026-1989=37
        assertEquals("drk@email.com", response.getPersonInfos().get(1).getEmail());
        assertEquals(List.of("pharmacol:5000mg"), response.getPersonInfos().get(1).getMedications());
        assertTrue(response.getPersonInfos().get(1).getAllergies().isEmpty());
    }

    @Test
    @DisplayName("Testing getPersonInfoByLastName with no matches")
    void getPersonInfoByLastName_shouldReturnEmptyForNonexistentLastName() throws Exception {
        // Given
        String lastName = "Nonexistent";
        List<Person> persons = Arrays.asList(
                new Person("John", "Boyd", "1509 Culver St", "Culver", "97451", "841-874-6512", "jaboyd@email.com")
        );
        List<Medicalrecord> medicalRecords = Arrays.asList(
                new Medicalrecord("John", "Boyd", "03/06/1984", new String[]{}, new String[]{})
        );

        when(readFromFileUtil.readFromFile(any(), any(TypeReference.class))).thenReturn(persons);
        when(readFromFileUtil.readFromInputStream(any(), any(TypeReference.class))).thenReturn(medicalRecords);

        // When
        var response = personService.getPersonInfoByLastName(lastName);

        // Then
        assertNotNull(response);
        assertTrue(response.getPersonInfos().isEmpty());
    }

    @Test
    @DisplayName("Testing getPersonInfoByLastName handles exception")
    void getPersonInfoByLastName_shouldHandleExceptionGracefully() throws Exception {
        // Given
        String lastName = "Boyd";

        // Mock exception during file reading
        when(readFromFileUtil.readFromFile(any(), any(TypeReference.class)))
                .thenThrow(new RuntimeException("File read error"));

        // When
        var response = personService.getPersonInfoByLastName(lastName);

        // Then
        assertNotNull(response);
        assertNotNull(response.getPersonInfos());
        assertTrue(response.getPersonInfos().isEmpty());
    }

    // ======================= Tests for getEmailsByCity =======================

    @Test
    @DisplayName("getEmailsByCity — returns emails for persons in the given city")
    void getEmailsByCity_shouldReturnEmailsForMatchingCity() {
        // Given
        List<Person> persons = Arrays.asList(
                new Person("John",   "Boyd",    "1509 Culver St", "Culver", "97451", "841-874-6512", "jaboyd@email.com"),
                new Person("Jacob",  "Boyd",    "1509 Culver St", "Culver", "97451", "841-874-6513", "drk@email.com"),
                new Person("Tenley", "Boyd",    "1509 Culver St", "Culver", "97451", "841-874-6512", "tenz@email.com"),
                new Person("Lily",   "Cooper",  "489 Manchester St", "Springfield", "97451", "841-874-9845", "lily@email.com")
        );
        when(readFromFileUtil.readFromFile(any(), any())).thenReturn((List) persons);

        // When
        List<String> emails = personService.getEmailsByCity("Culver");

        // Then
        assertNotNull(emails);
        assertEquals(3, emails.size());
        assertTrue(emails.contains("jaboyd@email.com"));
        assertTrue(emails.contains("drk@email.com"));
        assertTrue(emails.contains("tenz@email.com"));
        assertFalse(emails.contains("lily@email.com"));
    }

    @Test
    @DisplayName("getEmailsByCity — returns empty list when no persons match the city")
    void getEmailsByCity_shouldReturnEmptyListForUnknownCity() {
        // Given
        List<Person> persons = Arrays.asList(
                new Person("John", "Boyd", "1509 Culver St", "Culver", "97451", "841-874-6512", "jaboyd@email.com")
        );
        when(readFromFileUtil.readFromFile(any(), any())).thenReturn((List) persons);

        // When
        List<String> emails = personService.getEmailsByCity("UnknownCity");

        // Then
        assertNotNull(emails);
        assertTrue(emails.isEmpty());
    }

    @Test
    @DisplayName("getEmailsByCity — deduplicates emails when multiple persons share the same email")
    void getEmailsByCity_shouldReturnDistinctEmails() {
        // Given — John and Roger share the same email address
        List<Person> persons = Arrays.asList(
                new Person("John",   "Boyd", "1509 Culver St", "Culver", "97451", "841-874-6512", "jaboyd@email.com"),
                new Person("Roger",  "Boyd", "1509 Culver St", "Culver", "97451", "841-874-6512", "jaboyd@email.com"),
                new Person("Felicia","Boyd", "1509 Culver St", "Culver", "97451", "841-874-6544", "felicia@email.com")
        );
        when(readFromFileUtil.readFromFile(any(), any())).thenReturn((List) persons);

        // When
        List<String> emails = personService.getEmailsByCity("Culver");

        // Then — jaboyd@email.com must appear only once
        assertNotNull(emails);
        assertEquals(2, emails.size());
        assertEquals(1, emails.stream().filter("jaboyd@email.com"::equals).count());
        assertTrue(emails.contains("felicia@email.com"));
    }

    @Test
    @DisplayName("getEmailsByCity — city matching is case-insensitive")
    void getEmailsByCity_shouldBeCaseInsensitive() {
        // Given
        List<Person> persons = Arrays.asList(
                new Person("John", "Boyd", "1509 Culver St", "Culver", "97451", "841-874-6512", "jaboyd@email.com")
        );
        when(readFromFileUtil.readFromFile(any(), any())).thenReturn((List) persons);

        // When — querying with different casing
        List<String> emails = personService.getEmailsByCity("culver");

        // Then
        assertNotNull(emails);
        assertEquals(1, emails.size());
        assertTrue(emails.contains("jaboyd@email.com"));
    }

    @Test
    @DisplayName("getEmailsByCity — returns empty list when person has null city")
    void getEmailsByCity_shouldSkipPersonsWithNullCity() {
        // Given — one person has null city
        Person personWithNullCity = new Person();
        personWithNullCity.setFirstName("Unknown");
        personWithNullCity.setLastName("Person");
        personWithNullCity.setCity(null);
        personWithNullCity.setEmail("unknown@email.com");

        List<Person> persons = Arrays.asList(
                new Person("John", "Boyd", "1509 Culver St", "Culver", "97451", "841-874-6512", "jaboyd@email.com"),
                personWithNullCity
        );
        when(readFromFileUtil.readFromFile(any(), any())).thenReturn((List) persons);

        // When
        List<String> emails = personService.getEmailsByCity("Culver");

        // Then — null city person is skipped, no NullPointerException
        assertNotNull(emails);
        assertEquals(1, emails.size());
        assertTrue(emails.contains("jaboyd@email.com"));
        assertFalse(emails.contains("unknown@email.com"));
    }

    @Test
    @DisplayName("getEmailsByCity — returns empty list when person list is empty")
    void getEmailsByCity_shouldReturnEmptyListWhenNoPersons() {
        // Given
        when(readFromFileUtil.readFromFile(any(), any())).thenReturn(new ArrayList<>());

        // When
        List<String> emails = personService.getEmailsByCity("Culver");

        // Then
        assertNotNull(emails);
        assertTrue(emails.isEmpty());
    }

    @Test
    @DisplayName("getEmailsByCity — returns empty list on exception")
    void getEmailsByCity_shouldReturnEmptyListOnException() {
        // Given
        when(readFromFileUtil.readFromFile(any(), any()))
                .thenThrow(new RuntimeException("File read error"));

        // When
        List<String> emails = personService.getEmailsByCity("Culver");

        // Then
        assertNotNull(emails);
        assertTrue(emails.isEmpty());
    }

    @Test
    @DisplayName("getEmailsByCity — readFromFile is called exactly once")
    void getEmailsByCity_shouldCallReadFromFileExactlyOnce() {
        // Given
        when(readFromFileUtil.readFromFile(any(), any())).thenReturn(new ArrayList<>());

        // When
        personService.getEmailsByCity("Culver");

        // Then
        verify(readFromFileUtil, times(1)).readFromFile(any(), any());
    }
}
