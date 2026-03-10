package net.example.safetynet.service;


import lombok.extern.slf4j.Slf4j;
import net.example.safetynet.model.Firestation;
import net.example.safetynet.model.Medicalrecord;
import net.example.safetynet.model.Person;
import net.example.safetynet.utils.ReadFromFileUtil;
import net.example.safetynet.utils.WriteToFileUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;

import java.io.File;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class MedicalRecordService {

    private final ReadFromFileUtil readFromFileUtil;
    private final WriteToFileUtil writeToFileUtil;

    File filePath = new File("src/main/resources/safetynet/medicalrecord.json");

    public MedicalRecordService(ReadFromFileUtil readFromFileUtil, WriteToFileUtil writeToFileUtil) {
        this.readFromFileUtil = readFromFileUtil;
        this.writeToFileUtil = writeToFileUtil;
    }

    public ResponseEntity<String> addMedicalRecord(Medicalrecord medicalrecord) {

        try {
            List<Medicalrecord> medicalrecordList = readFromFileUtil.readFromFile(filePath, new TypeReference<List<Medicalrecord>>() {
            });
            medicalrecordList.add(medicalrecord);
            writeToFileUtil.writeToFile(medicalrecordList, filePath);
            return ResponseEntity.status(HttpStatus.CREATED).body("Medical Record added successfully");
        } catch (Exception e) {
            log.error("Error occurred while writing to file :{}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving Medical Record :" + e.getMessage());
        }
    }

    public List<Medicalrecord> getMedicalRecords() {
        return readFromFileUtil.readFromFile(filePath, new TypeReference<List<Medicalrecord>>() {
        });
    }

    public ResponseEntity<String> updateMedicalRecord(Medicalrecord updatedMedicalRecord, String firstName, String lastName) {
        try {
            List<Medicalrecord> medicalRecordList = readFromFileUtil.readFromFile(filePath, new TypeReference<List<Medicalrecord>>() {
            });
            Medicalrecord existingMedicalRecord = medicalRecordList.stream()
                    .filter(p -> firstName.equals(updatedMedicalRecord.getFirstName()))
                    .filter(p -> lastName.equals(updatedMedicalRecord.getLastName()))
                    .findFirst()
                    .orElse(null);
            log.debug("existsing Medicalrecord :{}", existingMedicalRecord);
            if (existingMedicalRecord != null) {
                existingMedicalRecord.setFirstName(updatedMedicalRecord.getFirstName());
                existingMedicalRecord.setLastName(updatedMedicalRecord.getLastName());


                medicalRecordList.add(existingMedicalRecord);
                writeToFileUtil.writeToFile(medicalRecordList, filePath);

                return ResponseEntity.status(HttpStatus.CREATED).body("Medical Record updated successfully");
            } else {
                log.error("Medical Record doesn't exist ");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Medical Record for " + firstName + " " + lastName + " doesn't exists");
            }
        } catch (Exception e) {
            log.error("Error occurred while writing to file :{}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving Medical Record :" + e.getMessage());
        }
    }

    public ResponseEntity<String> deleteMedicalRecord(String firstName, String lastName) {

        try {
            List<Medicalrecord> medicalRecordList = readFromFileUtil.readFromFile(filePath, new TypeReference<List<Medicalrecord>>() {
            });

            Medicalrecord deleteMedicalRecord = medicalRecordList.stream()
                    .filter(p -> lastName.equalsIgnoreCase(p.getLastName())
                            && firstName.equalsIgnoreCase((p.getFirstName())))
                    .findFirst()
                    .orElse(null);
            log.info("Delete Medical Record :{} ", deleteMedicalRecord);
            if (deleteMedicalRecord != null) {
                medicalRecordList.remove(deleteMedicalRecord);
                writeToFileUtil.writeToFile(medicalRecordList, filePath);
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Medical Record deleted successfully");
            } else {
                log.error("Medical Record doesn't exist ");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Medical Record with name " + firstName + " " + lastName + " doesn't exists");
            }
        } catch (Exception e) {
            log.error("Error occurred while writing to file :{}", e.getStackTrace());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting Medical Record :" + e.getMessage());
        }
    }
}
