package net.example.safetynet.controller;

import net.example.safetynet.model.Firestation;
import net.example.safetynet.service.FireStationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FireStationControllerTest {

    @Mock
    private FireStationService fireStationService;

    @InjectMocks
    private FireStationController fireStationController;

    @Test
    void addFirestation_directCall() {

        Firestation firestation = new Firestation();

        when(fireStationService.addFireStation(any()))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED)
                        .body("Fire station added successfully"));

        ResponseEntity<String> response = fireStationController.addFireStation(firestation);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Fire station added successfully", response.getBody());

        verify(fireStationService, times(1))
                .addFireStation(firestation);
    }

    @Test
    void updateFirestation_directCall() {

        Firestation firestation = new Firestation();

        when(fireStationService.updateFireStation(any(),any()))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED)
                        .body("Fire station updated successfully"));

        ResponseEntity<String> response = fireStationController.updateFireStation(firestation,"1");

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Fire station updated successfully", response.getBody());

        verify(fireStationService, times(1))
                .updateFireStation(firestation, "1");
    }

    @Test
    void deleteFirestation_directCall() {


        when(fireStationService.deleteFireStation(any()))
                .thenReturn(ResponseEntity.status(HttpStatus.NO_CONTENT)
                        .body("Fire Station deleted successfully"));

        ResponseEntity<String> response = fireStationController.deleteFireStation("123");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertEquals("Fire Station deleted successfully", response.getBody());

        verify(fireStationService, times(1))
                .deleteFireStation("123");
    }

    @Test
    void getAllFirestation_directCall() {

        List<Firestation> firestations = List.of(new Firestation());

        when(fireStationService.getAllFireStations()).thenReturn(firestations);

        List<Firestation> response = fireStationController.getFireStations();

        assertEquals(1, response.size());
        verify(fireStationService, times(1)).getAllFireStations();
    }
}