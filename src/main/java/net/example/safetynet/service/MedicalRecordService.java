package net.example.safetynet.service;


import lombok.extern.slf4j.Slf4j;
import net.example.safetynet.model.Medicalrecord;
import net.example.safetynet.model.Person;
import net.example.safetynet.utils.ReadFromFileUtil;
import net.example.safetynet.utils.WriteToFileUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
public class MedicalRecordService {

    private final ReadFromFileUtil readFromFileUtil;
    private final WriteToFileUtil writeToFileUtil;

    String filePath="src/main/resources/safetynet/medicalrecord.json";

    public MedicalRecordService(ReadFromFileUtil readFromFileUtil, WriteToFileUtil writeToFileUtil) {
        this.readFromFileUtil = readFromFileUtil;
        this.writeToFileUtil = writeToFileUtil;
    }

    public ResponseEntity<String> addMedicalRecord(Medicalrecord medicalrecord) {

        try{
            Set<Medicalrecord> medicalrecordList=readFromFileUtil.readFromFile(filePath);
            medicalrecordList.add(medicalrecord);
            writeToFileUtil.writeToFile(medicalrecordList,filePath);
            return ResponseEntity.status(HttpStatus.CREATED).body("Medical Record added successfully");
        }
        catch (Exception e)
        {
            log.error("Error occurred while writing to file :{}" ,e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving person :" +e.getMessage());
        }
    }

    public Set<Medicalrecord> getMedicalRecords() {
        return readFromFileUtil.readFromFile(filePath);
    }
}
