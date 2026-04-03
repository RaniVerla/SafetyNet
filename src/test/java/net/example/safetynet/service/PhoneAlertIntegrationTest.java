package net.example.safetynet.service;

import net.example.safetynet.model.PhoneAlertResponse;
import net.example.safetynet.service.FireStationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PhoneAlertIntegrationTest {

    @Autowired
    private FireStationService fireStationService;

    @Test
    void getPhoneNumbersByStationNumber_shouldReturnPhoneNumbersForStation1() {
        // When
        List<String> phoneNumbers = fireStationService.getPhoneNumbersByStationNumber("1");

        // Then
        assertNotNull(phoneNumbers);
        // Station 1 covers: 644 Gershwin Cir, 908 73rd St, 947 E. Rose Dr
        // Expected phones: Peter Duncan (841-874-6512), Reginold Walker (841-874-8547),
        // Jamie Peters (841-874-7462), Brian Stelzer (841-874-7784), Shawna Stelzer (841-874-7784), Kendrik Stelzer (841-874-7784)
        assertEquals(6, phoneNumbers.size());
        assertTrue(phoneNumbers.contains("841-874-6512"));
        assertTrue(phoneNumbers.contains("841-874-8547"));
        assertTrue(phoneNumbers.contains("841-874-7462"));
        assertTrue(phoneNumbers.contains("841-874-7784"));
    }

    @Test
    void getPhoneNumbersByStationNumber_shouldReturnPhoneNumbersForStation3() {
        // When
        List<String> phoneNumbers = fireStationService.getPhoneNumbersByStationNumber("3");

        // Then
        assertNotNull(phoneNumbers);
        // Station 3 covers: 1509 Culver St, 29 15th St, 834 Binoc Ave, 748 Townings Dr, 112 Steppes Pl
        assertTrue(phoneNumbers.size() > 5); // Should have multiple phone numbers
        assertTrue(phoneNumbers.contains("841-874-6512")); // John Boyd
        assertTrue(phoneNumbers.contains("841-874-6513")); // Jacob Boyd
    }

    @Test
    void getPhoneNumbersByStationNumber_shouldReturnEmptyListForInvalidStation() {
        // When
        List<String> phoneNumbers = fireStationService.getPhoneNumbersByStationNumber("99");

        // Then
        assertNotNull(phoneNumbers);
        assertTrue(phoneNumbers.isEmpty());
    }
}
