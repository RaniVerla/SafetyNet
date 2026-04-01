package net.example.safetynet.service;


import lombok.extern.slf4j.Slf4j;
import net.example.safetynet.model.Firestation;
import net.example.safetynet.model.FireStationResidents;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FireStationService {

    private final ReadFromFileUtil readFromFileUtil;
    private final WriteToFileUtil writeToFileUtil;

    // Use ClassPathResource for reading data files
    private final ClassPathResource firestationResource = new ClassPathResource("/safetynet/firestation.json");
    private final ClassPathResource personResource = new ClassPathResource("/safetynet/person.json");
    private final ClassPathResource medicalRecordResource = new ClassPathResource("/safetynet/medicalrecord.json");

    // Keep File for writing (assuming write operations use file system)
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
                    .filter(p -> p.getStation().equals(updatedFirestation.getStation()))
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
                    .filter(p -> p.getStation().toString().equalsIgnoreCase(station))
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
            log.error("Error occurred while writing to file :{}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting Firestation :" + e.getMessage());
        }
    }

    /**
     * Get residents covered by a fire station
     *
     * @param stationNumber the station number
     * @return FireStationResidents containing residents list and adult/child counts
     */
    public FireStationResidents getResidentsByStationNumber(String stationNumber) {
        try {
            // Get all fire stations from classpath
            List<Firestation> fireStationList = readFromResource(firestationResource, new TypeReference<List<Firestation>>() {});

            log.info("Read {} fire stations from classpath", fireStationList.size());

            // Debug: Log all fire stations with station numbers
            for (Firestation fs : fireStationList) {
                log.info("Fire Station - Address: '{}', Station: '{}' (Type: {})",
                    fs.getAddress(), fs.getStation(), fs.getStation().getClass().getSimpleName());
            }

            // Get all persons from classpath
            List<Person> personList = readFromResource(personResource, new TypeReference<List<Person>>() {});

            // Get all medical records from classpath
            List<Medicalrecord> medicalRecordList = readFromResource(medicalRecordResource, new TypeReference<List<Medicalrecord>>() {});

            // Get addresses covered by this station
            log.info("Looking for station: '{}' (length: {})", stationNumber, stationNumber.length());

            Set<String> addressesCoveredByStation = new java.util.HashSet<>();
            try {
                Integer searchStation = Integer.parseInt(stationNumber.trim());
                for (Firestation fs : fireStationList) {
                    Integer trimmedStation = fs.getStation();
                    log.info("Comparing firestation '{}' with searchterm '{}'", trimmedStation, searchStation);
                    if (trimmedStation != null && trimmedStation.equals(searchStation)) {
                        log.info("  MATCH! Adding address: {}", fs.getAddress());
                        addressesCoveredByStation.add(fs.getAddress());
                    } else {
                        log.info("  No match");
                    }
                }
            } catch (NumberFormatException e) {
                log.error("Invalid station number format: {}", stationNumber);
            }

            log.info("Addresses covered by station {}: {}", stationNumber, addressesCoveredByStation);

            // Filter persons by addresses
            List<FireStationResidents.ResidentInfo> residents = new ArrayList<>();
            int adultCount = 0;
            int childCount = 0;

            for (Person person : personList) {
                if (addressesCoveredByStation.contains(person.getAddress())) {
                    FireStationResidents.ResidentInfo residentInfo = new FireStationResidents.ResidentInfo(
                            person.getFirstName(),
                            person.getLastName(),
                            person.getAddress(),
                            person.getPhone()
                    );
                    residents.add(residentInfo);

                    // Check if person is a child (18 years or younger)
                    if (isChild(person, medicalRecordList)) {
                        childCount++;
                    } else {
                        adultCount++;
                    }
                }
            }

            log.info("Found {} residents for station {}", residents.size(), stationNumber);
            return new FireStationResidents(residents, adultCount, childCount);
        } catch (Exception e) {
            log.error("Error occurred while retrieving residents for station {}: {}", stationNumber, e.getMessage());
            return new FireStationResidents(new ArrayList<>(), 0, 0);
        }
    }

    /**
     * Check if a person is a child (18 years or younger)
     *
     * @param person the person to check
     * @param medicalRecordList the list of medical records
     * @return true if person is 18 or younger, false otherwise
     */
    private boolean isChild(Person person, List<Medicalrecord> medicalRecordList) {
        Medicalrecord medicalRecord = medicalRecordList.stream()
                .filter(m -> m.getFirstName().equalsIgnoreCase(person.getFirstName())
                        && m.getLastName().equalsIgnoreCase(person.getLastName()))
                .findFirst()
                .orElse(null);

        if (medicalRecord == null || medicalRecord.getBirthdate() == null) {
            log.warn("No medical record found for {} {}", person.getFirstName(), person.getLastName());
            return false;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            LocalDate birthDate = LocalDate.parse(medicalRecord.getBirthdate(), formatter);
            LocalDate today = LocalDate.now();
            long age = ChronoUnit.YEARS.between(birthDate, today);
            return age <= 18;
        } catch (Exception e) {
            log.error("Error parsing birthdate for {} {}: {}", person.getFirstName(), person.getLastName(), e.getMessage());
            return false;
        }
    }

    /**
     * Helper method to read from ClassPathResource
     */
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
