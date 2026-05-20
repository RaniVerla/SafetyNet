package net.example.safetynet.service;

import lombok.extern.slf4j.Slf4j;
import net.example.safetynet.model.Data;
import net.example.safetynet.model.Firestation;
import net.example.safetynet.model.Medicalrecord;
import net.example.safetynet.model.Person;
import net.example.safetynet.utils.ReadFromFileUtil;
import net.example.safetynet.utils.WriteToFileUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DataService {

    private final ReadFromFileUtil readFromFileUtil;
    private final WriteToFileUtil writeToFileUtil;

    private final ClassPathResource personResource = new ClassPathResource("/safetynet/person.json");
    private final ClassPathResource firestationResource = new ClassPathResource("/safetynet/firestation.json");
    private final ClassPathResource medicalRecordResource = new ClassPathResource("/safetynet/medicalrecord.json");

    private final File dataFilePath = new File("src/main/resources/safetynet/data.json");

    public DataService(ReadFromFileUtil readFromFileUtil, WriteToFileUtil writeToFileUtil) {
        this.readFromFileUtil = readFromFileUtil;
        this.writeToFileUtil = writeToFileUtil;
    }

    /**
     * Reads all data from person, firestation and medicalrecord JSON files,
     * merges them into a single Data object, saves it to data.json, and returns it.
     */
    public ResponseEntity<?> aggregateAndSaveData() {
        try {
            List<Person> persons = readFromResource(personResource, new TypeReference<List<Person>>() {});
            List<Firestation> firestations = readFromResource(firestationResource, new TypeReference<List<Firestation>>() {});
            List<Medicalrecord> medicalRecords = readFromResource(medicalRecordResource, new TypeReference<List<Medicalrecord>>() {});

            log.info("Read {} persons, {} firestations, {} medical records",
                    persons.size(), firestations.size(), medicalRecords.size());

            Data data = new Data(persons, firestations, medicalRecords);

            writeToFileUtil.writeObjectToFile(data, dataFilePath);

            log.info("Successfully saved aggregated data to {}", dataFilePath.getPath());
            return ResponseEntity.status(HttpStatus.OK).body(data);

        } catch (Exception e) {
            log.error("Error aggregating data: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error aggregating data: " + e.getMessage());
        }
    }

    private <T> List<T> readFromResource(ClassPathResource resource, TypeReference<List<T>> typeReference) throws Exception {
        if (!resource.exists()) {
            log.error("Resource {} does not exist", resource.getPath());
            return new ArrayList<>();
        }
        try (InputStream inputStream = resource.getInputStream()) {
            return readFromFileUtil.readFromInputStream(inputStream, typeReference);
        }
    }
}
