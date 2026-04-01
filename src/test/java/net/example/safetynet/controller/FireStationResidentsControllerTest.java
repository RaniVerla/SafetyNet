package net.example.safetynet.controller;

import net.example.safetynet.model.FireStationResidents;
import net.example.safetynet.service.FireStationService;
import org.junit.jupiter.api.BeforeEach;
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

/**
 * Test class for FireStationResidentsController
 * Tests the endpoint: GET /firestation?stationNumber=<station_number>
 */
@ExtendWith(MockitoExtension.class)
class FireStationResidentsControllerTest {

    @Mock
    private FireStationService fireStationService;

    @InjectMocks
    private FireStationResidentsController controller;

    private FireStationResidents testResponse;
    private List<FireStationResidents.ResidentInfo> testResidents;

    @BeforeEach
    void setUp() {
        // Initialize test data
        testResidents = new ArrayList<>();
        testResidents.add(new FireStationResidents.ResidentInfo(
                "John",
                "Boyd",
                "1509 Culver St",
                "841-874-6513"
        ));
        testResidents.add(new FireStationResidents.ResidentInfo(
                "Jacob",
                "Boyd",
                "1509 Culver St",
                "841-874-6513"
        ));
        testResidents.add(new FireStationResidents.ResidentInfo(
                "Tenley",
                "Boyd",
                "1509 Culver St",
                "841-874-6513"
        ));

        testResponse = new FireStationResidents(testResidents, 2, 1);
    }

    /**
     * Test: Get residents by valid station number
     * Verifies that residents are returned with correct information
     */
    @Test
    void getResidentsByStation_WithValidStationNumber() {
        // Arrange
        String stationNumber = "3";
        when(fireStationService.getResidentsByStationNumber(stationNumber))
                .thenReturn(testResponse);

        // Act
        FireStationResidents response = controller.getResidentsByStation(stationNumber);

        // Assert
        assertNotNull(response);
        assertEquals(3, response.getResidents().size());
        assertEquals(2, response.getAdultCount());
        assertEquals(1, response.getChildCount());
        verify(fireStationService, times(1))
                .getResidentsByStationNumber(stationNumber);
    }

    /**
     * Test: Verify resident information structure
     * Ensures each resident contains required fields: firstName, lastName, address, phone
     */
    @Test
    void getResidentsByStation_VerifyResidentInformation() {
        // Arrange
        String stationNumber = "1";
        when(fireStationService.getResidentsByStationNumber(stationNumber))
                .thenReturn(testResponse);

        // Act
        FireStationResidents response = controller.getResidentsByStation(stationNumber);

        // Assert
        FireStationResidents.ResidentInfo resident = response.getResidents().get(0);
        assertNotNull(resident.getFirstName());
        assertNotNull(resident.getLastName());
        assertNotNull(resident.getAddress());
        assertNotNull(resident.getPhone());

        assertEquals("John", resident.getFirstName());
        assertEquals("Boyd", resident.getLastName());
        assertEquals("1509 Culver St", resident.getAddress());
        assertEquals("841-874-6513", resident.getPhone());
    }

    /**
     * Test: Station with no residents
     * Verifies that an empty residents list is returned with 0 adults and 0 children
     */
    @Test
    void getResidentsByStation_WithNoResidents() {
        // Arrange
        String stationNumber = "99";
        FireStationResidents emptyResponse = new FireStationResidents(
                new ArrayList<>(), 0, 0
        );
        when(fireStationService.getResidentsByStationNumber(stationNumber))
                .thenReturn(emptyResponse);

        // Act
        FireStationResidents response = controller.getResidentsByStation(stationNumber);

        // Assert
        assertNotNull(response);
        assertEquals(0, response.getResidents().size());
        assertEquals(0, response.getAdultCount());
        assertEquals(0, response.getChildCount());
    }

    /**
     * Test: Correct adult count calculation
     * Verifies that adults (persons older than 18) are counted correctly
     */
    @Test
    void getResidentsByStation_VerifyAdultCount() {
        // Arrange
        String stationNumber = "3";
        FireStationResidents response = new FireStationResidents(testResidents, 2, 1);
        when(fireStationService.getResidentsByStationNumber(stationNumber))
                .thenReturn(response);

        // Act
        FireStationResidents result = controller.getResidentsByStation(stationNumber);

        // Assert
        assertEquals(2, result.getAdultCount());
    }

    /**
     * Test: Correct child count calculation
     * Verifies that children (persons 18 years or younger) are counted correctly
     */
    @Test
    void getResidentsByStation_VerifyChildCount() {
        // Arrange
        String stationNumber = "3";
        FireStationResidents response = new FireStationResidents(testResidents, 2, 1);
        when(fireStationService.getResidentsByStationNumber(stationNumber))
                .thenReturn(response);

        // Act
        FireStationResidents result = controller.getResidentsByStation(stationNumber);

        // Assert
        assertEquals(1, result.getChildCount());
    }

    /**
     * Test: Multiple residents at same station
     * Verifies that all residents at a station are included in the response
     */
    @Test
    void getResidentsByStation_WithMultipleResidents() {
        // Arrange
        String stationNumber = "2";
        List<FireStationResidents.ResidentInfo> residents = new ArrayList<>();
        residents.add(new FireStationResidents.ResidentInfo("Alice", "Smith", "123 Main St", "555-1234"));
        residents.add(new FireStationResidents.ResidentInfo("Bob", "Smith", "123 Main St", "555-1234"));
        residents.add(new FireStationResidents.ResidentInfo("Charlie", "Smith", "123 Main St", "555-1234"));
        residents.add(new FireStationResidents.ResidentInfo("Diana", "Jones", "456 Oak Ave", "555-5678"));

        FireStationResidents response = new FireStationResidents(residents, 3, 1);
        when(fireStationService.getResidentsByStationNumber(stationNumber))
                .thenReturn(response);

        // Act
        FireStationResidents result = controller.getResidentsByStation(stationNumber);

        // Assert
        assertEquals(4, result.getResidents().size());
        assertEquals(3, result.getAdultCount());
        assertEquals(1, result.getChildCount());
    }

    /**
     * Test: Resident list contains correct addresses
     * Verifies that residents are from the correct station's coverage area
     */
    @Test
    void getResidentsByStation_VerifyResidentAddresses() {
        // Arrange
        String stationNumber = "1";
        List<FireStationResidents.ResidentInfo> residents = new ArrayList<>();
        residents.add(new FireStationResidents.ResidentInfo("Person1", "Last1", "644 Gershwin Cir", "111-1111"));
        residents.add(new FireStationResidents.ResidentInfo("Person2", "Last2", "908 73rd St", "222-2222"));

        FireStationResidents response = new FireStationResidents(residents, 2, 0);
        when(fireStationService.getResidentsByStationNumber(stationNumber))
                .thenReturn(response);

        // Act
        FireStationResidents result = controller.getResidentsByStation(stationNumber);

        // Assert
        assertEquals(2, result.getResidents().size());
        assertTrue(result.getResidents().stream()
                .allMatch(r -> r.getAddress().equals("644 Gershwin Cir") || r.getAddress().equals("908 73rd St")));
    }

    /**
     * Test: Station number parameter is passed correctly
     * Verifies that the controller passes the station number to the service
     */
    @Test
    void getResidentsByStation_ParameterPassedToService() {
        // Arrange
        String stationNumber = "4";
        when(fireStationService.getResidentsByStationNumber(stationNumber))
                .thenReturn(new FireStationResidents(new ArrayList<>(), 0, 0));

        // Act
        controller.getResidentsByStation(stationNumber);

        // Assert
        verify(fireStationService, times(1))
                .getResidentsByStationNumber("4");
    }

    /**
     * Test: Multiple requests with different station numbers
     * Verifies that the controller correctly handles multiple requests with different parameters
     */
    @Test
    void getResidentsByStation_MultipleRequestsWithDifferentStations() {
        // Arrange
        String station1 = "1";
        String station2 = "2";

        FireStationResidents response1 = new FireStationResidents(
                List.of(new FireStationResidents.ResidentInfo("John", "Doe", "123 St", "111-1111")),
                1, 0
        );
        FireStationResidents response2 = new FireStationResidents(
                List.of(new FireStationResidents.ResidentInfo("Jane", "Smith", "456 Ave", "222-2222")),
                1, 0
        );

        when(fireStationService.getResidentsByStationNumber(station1))
                .thenReturn(response1);
        when(fireStationService.getResidentsByStationNumber(station2))
                .thenReturn(response2);

        // Act
        FireStationResidents result1 = controller.getResidentsByStation(station1);
        FireStationResidents result2 = controller.getResidentsByStation(station2);

        // Assert
        assertEquals("John", result1.getResidents().get(0).getFirstName());
        assertEquals("Jane", result2.getResidents().get(0).getFirstName());
        verify(fireStationService, times(1)).getResidentsByStationNumber(station1);
        verify(fireStationService, times(1)).getResidentsByStationNumber(station2);
    }

    /**
     * Test: Count totals are zero when no residents
     * Verifies that adult and child counts are 0 when no residents exist
     */
    @Test
    void getResidentsByStation_ZeroCountsWhenNoResidents() {
        // Arrange
        String stationNumber = "99";
        FireStationResidents emptyResponse = new FireStationResidents(
                new ArrayList<>(), 0, 0
        );
        when(fireStationService.getResidentsByStationNumber(stationNumber))
                .thenReturn(emptyResponse);

        // Act
        FireStationResidents result = controller.getResidentsByStation(stationNumber);

        // Assert
        assertEquals(0, result.getAdultCount());
        assertEquals(0, result.getChildCount());
        assertTrue(result.getResidents().isEmpty());
    }

    /**
     * Test: Only children at a station
     * Verifies correct counts when only children reside in the coverage area
     */
    @Test
    void getResidentsByStation_OnlyChildrenAtStation() {
        // Arrange
        String stationNumber = "5";
        List<FireStationResidents.ResidentInfo> childrenOnly = new ArrayList<>();
        childrenOnly.add(new FireStationResidents.ResidentInfo("Child1", "Last1", "999 Kid St", "555-9999"));
        childrenOnly.add(new FireStationResidents.ResidentInfo("Child2", "Last2", "999 Kid St", "555-9999"));

        FireStationResidents response = new FireStationResidents(childrenOnly, 0, 2);
        when(fireStationService.getResidentsByStationNumber(stationNumber))
                .thenReturn(response);

        // Act
        FireStationResidents result = controller.getResidentsByStation(stationNumber);

        // Assert
        assertEquals(0, result.getAdultCount());
        assertEquals(2, result.getChildCount());
        assertEquals(2, result.getResidents().size());
    }

    /**
     * Test: Only adults at a station
     * Verifies correct counts when only adults reside in the coverage area
     */
    @Test
    void getResidentsByStation_OnlyAdultsAtStation() {
        // Arrange
        String stationNumber = "6";
        List<FireStationResidents.ResidentInfo> adultsOnly = new ArrayList<>();
        adultsOnly.add(new FireStationResidents.ResidentInfo("Adult1", "Last1", "888 Senior Ln", "555-8888"));
        adultsOnly.add(new FireStationResidents.ResidentInfo("Adult2", "Last2", "888 Senior Ln", "555-8888"));
        adultsOnly.add(new FireStationResidents.ResidentInfo("Adult3", "Last3", "888 Senior Ln", "555-8888"));

        FireStationResidents response = new FireStationResidents(adultsOnly, 3, 0);
        when(fireStationService.getResidentsByStationNumber(stationNumber))
                .thenReturn(response);

        // Act
        FireStationResidents result = controller.getResidentsByStation(stationNumber);

        // Assert
        assertEquals(3, result.getAdultCount());
        assertEquals(0, result.getChildCount());
        assertEquals(3, result.getResidents().size());
    }

    /**
     * Test: Service is called exactly once per request
     * Verifies that the controller calls the service exactly one time
     */
    @Test
    void getResidentsByStation_ServiceCalledOncePerRequest() {
        // Arrange
        String stationNumber = "3";
        when(fireStationService.getResidentsByStationNumber(stationNumber))
                .thenReturn(testResponse);

        // Act
        controller.getResidentsByStation(stationNumber);

        // Assert
        verify(fireStationService, times(1))
                .getResidentsByStationNumber(stationNumber);
        verifyNoMoreInteractions(fireStationService);
    }

    /**
     * Test: Response structure is not null
     * Verifies that the response object and its fields are never null
     */
    @Test
    void getResidentsByStation_ResponseStructureIsValid() {
        // Arrange
        String stationNumber = "2";
        when(fireStationService.getResidentsByStationNumber(stationNumber))
                .thenReturn(testResponse);

        // Act
        FireStationResidents result = controller.getResidentsByStation(stationNumber);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getResidents());
        assertNotNull(result.getAdultCount());
        assertNotNull(result.getChildCount());
    }

    /**
     * Test: Resident information is complete for each person
     * Verifies that all required fields are populated for each resident
     */
    @Test
    void getResidentsByStation_EachResidentHasCompleteInfo() {
        // Arrange
        String stationNumber = "1";
        when(fireStationService.getResidentsByStationNumber(stationNumber))
                .thenReturn(testResponse);

        // Act
        FireStationResidents result = controller.getResidentsByStation(stationNumber);

        // Assert
        for (FireStationResidents.ResidentInfo resident : result.getResidents()) {
            assertNotNull(resident.getFirstName());
            assertFalse(resident.getFirstName().isEmpty());
            assertNotNull(resident.getLastName());
            assertFalse(resident.getLastName().isEmpty());
            assertNotNull(resident.getAddress());
            assertFalse(resident.getAddress().isEmpty());
            assertNotNull(resident.getPhone());
            assertFalse(resident.getPhone().isEmpty());
        }
    }
}

