package net.example.safetynet.service;


import lombok.extern.slf4j.Slf4j;
import net.example.safetynet.model.Firestation;
import net.example.safetynet.model.Person;
import net.example.safetynet.utils.ReadFromFileUtil;
import net.example.safetynet.utils.WriteToFileUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
public class FireStationService {

    private final ReadFromFileUtil readFromFileUtil;
    private final WriteToFileUtil writeToFileUtil;

    String filePath="src/main/resources/safetynet/firestation.json";

    public FireStationService(ReadFromFileUtil readFromFileUtil, WriteToFileUtil writeToFileUtil) {
        this.readFromFileUtil = readFromFileUtil;
        this.writeToFileUtil = writeToFileUtil;
    }

    public ResponseEntity<String> addFireStation(Firestation firestation) {
        try{
            Set<Firestation> firestationList=readFromFileUtil.readFromFile(filePath);
            firestationList.add(firestation);
            writeToFileUtil.writeToFile(firestationList,filePath);
            return ResponseEntity.status(HttpStatus.CREATED).body("Fire Station added successfully");
        }
        catch (Exception e)
        {
            log.error("Error occurred while writing to file :{}" ,e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving person :" +e.getMessage());
        }
    }

    public Set<Firestation> getAllFireStations() {
        return readFromFileUtil.readFromFile(filePath);
    }
}
