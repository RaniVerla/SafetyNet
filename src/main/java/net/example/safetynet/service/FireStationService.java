package net.example.safetynet.service;


import lombok.extern.slf4j.Slf4j;
import net.example.safetynet.model.Firestation;
import net.example.safetynet.model.Person;
import net.example.safetynet.utils.ReadFromFileUtil;
import net.example.safetynet.utils.WriteToFileUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;

import java.io.File;
import java.util.List;

@Slf4j
@Service
public class FireStationService {

    private final ReadFromFileUtil readFromFileUtil;
    private final WriteToFileUtil writeToFileUtil;

    File filePath = new File("src/main/resources/safetynet/firestation.json");

    public FireStationService(ReadFromFileUtil readFromFileUtil, WriteToFileUtil writeToFileUtil) {
        this.readFromFileUtil = readFromFileUtil;
        this.writeToFileUtil = writeToFileUtil;
    }

    public ResponseEntity<String> addFireStation(Firestation firestation) {
        try {
            List<Firestation> firestationList = readFromFileUtil.readFromFile(filePath, new TypeReference<List<Firestation>>() {
            });
            firestationList.add(firestation);
            writeToFileUtil.writeToFile(firestationList, filePath);
            return ResponseEntity.status(HttpStatus.CREATED).body("Fire Station added successfully");
        } catch (Exception e) {
            log.error("Error occurred while writing to file :{}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving Fire Station :" + e.getMessage());
        }
    }

    public List<Firestation> getAllFireStations() {
        return readFromFileUtil.readFromFile(filePath, new TypeReference<List<Firestation>>() {
        });
    }

    public ResponseEntity<String> updateFireStation(Firestation updatedFirestation, String station) {

        try {
            List<Firestation> fireStationList = readFromFileUtil.readFromFile(filePath, new TypeReference<List<Firestation>>() {
            });
            Firestation existingFireStation = fireStationList.stream()
                    .filter(p -> station.equals(updatedFirestation.getStation()))
                    .findFirst()
                    .orElse(null);
            log.debug("existsing Fire Station :{}", existingFireStation);
            if (existingFireStation != null) {
                existingFireStation.setStation(updatedFirestation.getStation());
                existingFireStation.setAddress(updatedFirestation.getAddress());

                fireStationList.add(existingFireStation);
                writeToFileUtil.writeToFile(fireStationList, filePath);

                return ResponseEntity.status(HttpStatus.CREATED).body("Fire station updated successfully");
            } else {
                log.error("Fire station doesn't exist ");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Fire station doesn't exists");
            }
        } catch (Exception e) {
            log.error("Error occurred while writing to file :{}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving Fire station :" + e.getMessage());
        }
    }

    public ResponseEntity<String> deleteFireStation(String station) {
        try {
            List<Firestation> fireStationList = readFromFileUtil.readFromFile(filePath, new TypeReference<List<Firestation>>() {
            });
            log.info("Person List :{} ", fireStationList);
            Firestation deleteFireStation = fireStationList.stream()
                    .filter(p -> p.getStation().equalsIgnoreCase(station))
                    .findFirst()
                    .orElse(null);
            log.info("Delete FireStation :{} ", deleteFireStation);
            if (deleteFireStation != null) {
                fireStationList.remove(deleteFireStation);
                writeToFileUtil.writeToFile(fireStationList, filePath);
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Fire Station deleted successfully");
            } else {
                log.error("Firestation doesn't exist ");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Firestation with name " + station + " doesn't exists");
            }
        } catch (Exception e) {
            log.error("Error occurred while writing to file :{}", e.getStackTrace());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting Firestation :" + e.getMessage());
        }
    }
}
