package net.example.safetynet.service;

import net.example.safetynet.model.Firestation;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FireStationServiceTest {

    @Mock
    private ReadFromFileUtil readFromFileUtil;

    @Mock
    private WriteToFileUtil writeToFileUtil;

    @InjectMocks
    private FireStationService fireStationService;

    @Test
    @DisplayName("Testing the success path")
    void addFireStation() throws Exception {

        Firestation newFireStation = new Firestation("stn1", "1509 Culver St");


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

        Firestation newFireStation = new Firestation("stn1", "1509 Culver St");

        when(readFromFileUtil.readFromFile(any(), any()))
                .thenReturn(null);

        ResponseEntity<String> response =
                fireStationService.addFireStation(newFireStation);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void deleteFireStation_success() {

        Firestation existing = new Firestation();
        existing.setStation("1");

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
        existing.setStation("1");

        List<Firestation> list = new ArrayList<>();
        list.add(existing);

        Firestation updated = new Firestation();
        updated.setStation("1");
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

}