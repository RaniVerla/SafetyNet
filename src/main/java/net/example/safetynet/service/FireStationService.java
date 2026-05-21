package net.example.safetynet.service;


import lombok.extern.slf4j.Slf4j;
import net.example.safetynet.model.Data;
import net.example.safetynet.model.FireAlertResponse;
import net.example.safetynet.model.Firestation;
import net.example.safetynet.model.FireStationResidents;
import net.example.safetynet.model.FloodHousehold;
import net.example.safetynet.model.FloodResident;
import net.example.safetynet.model.FloodResponse;
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
import java.util.Map;
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
    private final ClassPathResource dataResource = new ClassPathResource("/safetynet/data.json");

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
     * Get residents covered by a fire station.
     * Data is sourced from data.json which contains persons, firestations and medicalrecords.
     *
     * @param stationNumber the station number
     * @return FireStationResidents containing residents list and adult/child counts
     */
    public FireStationResidents getResidentsByStationNumber(String stationNumber) {
        try {
            // Validate station number format up front
            int searchStation;
            try {
                searchStation = Integer.parseInt(stationNumber.trim());
            } catch (NumberFormatException e) {
                log.error("Invalid station number format: {}", stationNumber);
                return new FireStationResidents(new ArrayList<>(), 0, 0);
            }

            // Read all data from data.json
            Data data = readDataJson();
            List<Firestation> fireStationList = data.getFirestations() != null ? data.getFirestations() : new ArrayList<>();
            List<Person> personList = data.getPersons() != null ? data.getPersons() : new ArrayList<>();
            List<Medicalrecord> medicalRecordList = data.getMedicalrecords() != null ? data.getMedicalrecords() : new ArrayList<>();

            log.info("Loaded from data.json — firestations: {}, persons: {}, medicalrecords: {}",
                    fireStationList.size(), personList.size(), medicalRecordList.size());

            // Collect all addresses covered by the requested station
            Set<String> coveredAddresses = fireStationList.stream()
                    .filter(fs -> fs.getStation() != null && fs.getStation() == searchStation)
                    .map(Firestation::getAddress)
                    .collect(Collectors.toSet());

            log.info("Addresses covered by station {}: {}", stationNumber, coveredAddresses);

            // Build resident list and tally adults / children
            List<FireStationResidents.ResidentInfo> residents = new ArrayList<>();
            int adultCount = 0;
            int childCount = 0;

            for (Person person : personList) {
                if (coveredAddresses.contains(person.getAddress())) {
                    residents.add(new FireStationResidents.ResidentInfo(
                            person.getFirstName(),
                            person.getLastName(),
                            person.getAddress(),
                            person.getPhone()
                    ));

                    if (isChild(person, medicalRecordList)) {
                        childCount++;
                    } else {
                        adultCount++;
                    }
                }
            }

            log.info("Found {} residents for station {} — adults: {}, children: {}",
                    residents.size(), stationNumber, adultCount, childCount);
            return new FireStationResidents(residents, adultCount, childCount);

        } catch (Exception e) {
            log.error("Error occurred while retrieving residents for station {}: {}", stationNumber, e.getMessage());
            return new FireStationResidents(new ArrayList<>(), 0, 0);
        }
    }

    /**
     * Get residents at a specific address along with the fire station number serving it.
     * Each resident includes name, phone, age, medications and allergies.
     * Data is sourced from data.json.
     *
     * @param address the address to query
     * @return FireAlertResponse containing station number and list of residents with medical info
     */
    public FireAlertResponse getResidentsByAddress(String address) {
        try {
            Data data = readDataJson();
            List<Firestation> fireStationList = data.getFirestations() != null ? data.getFirestations() : new ArrayList<>();
            List<Person> personList = data.getPersons() != null ? data.getPersons() : new ArrayList<>();
            List<Medicalrecord> medicalRecordList = data.getMedicalrecords() != null ? data.getMedicalrecords() : new ArrayList<>();

            // Find the station number serving this address (first match)
            int stationNumber = fireStationList.stream()
                    .filter(fs -> fs.getAddress() != null && fs.getAddress().equalsIgnoreCase(address))
                    .map(Firestation::getStation)
                    .findFirst()
                    .orElse(0);

            log.info("Station {} serves address '{}'", stationNumber, address);

            // Build resident list for the address
            List<FireAlertResponse.FireAlertResident> residents = personList.stream()
                    .filter(p -> p.getAddress() != null && p.getAddress().equalsIgnoreCase(address))
                    .map(person -> {
                        Medicalrecord record = medicalRecordList.stream()
                                .filter(m -> m.getFirstName().equalsIgnoreCase(person.getFirstName())
                                        && m.getLastName().equalsIgnoreCase(person.getLastName()))
                                .findFirst()
                                .orElse(null);

                        int age = record != null ? getAge(record) : -1;
                        List<String> medications = record != null && record.getMedications() != null
                                ? List.of(record.getMedications()) : new ArrayList<>();
                        List<String> allergies = record != null && record.getAllergies() != null
                                ? List.of(record.getAllergies()) : new ArrayList<>();

                        return new FireAlertResponse.FireAlertResident(
                                person.getFirstName(),
                                person.getLastName(),
                                person.getPhone(),
                                age,
                                medications,
                                allergies
                        );
                    })
                    .collect(Collectors.toList());

            log.info("Found {} residents at address '{}'", residents.size(), address);
            return new FireAlertResponse(stationNumber, residents);

        } catch (Exception e) {
            log.error("Error retrieving residents at address '{}': {}", address, e.getMessage());
            return new FireAlertResponse(0, new ArrayList<>());
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
     * Get phone numbers of residents served by a fire station
     *
     * @param stationNumber the station number
     * @return list of phone numbers of residents served by the fire station
     */
    public List<String> getPhoneNumbersByStationNumber(String stationNumber) {
        try {
            // Validate station number format up front
            int searchStation;
            try {
                searchStation = Integer.parseInt(stationNumber.trim());
            } catch (NumberFormatException e) {
                log.error("Invalid station number format: {}", stationNumber);
                return new ArrayList<>();
            }

            // Read all data from data.json
            Data data = readDataJson();
            List<Firestation> fireStationList = data.getFirestations() != null ? data.getFirestations() : new ArrayList<>();
            List<Person> personList = data.getPersons() != null ? data.getPersons() : new ArrayList<>();

            log.info("Loaded from data.json — firestations: {}, persons: {}",
                    fireStationList.size(), personList.size());

            // Collect all addresses covered by the requested station
            Set<String> coveredAddresses = fireStationList.stream()
                    .filter(fs -> fs.getStation() != null && fs.getStation() == searchStation)
                    .map(Firestation::getAddress)
                    .collect(Collectors.toSet());

            log.info("Addresses covered by station {}: {}", stationNumber, coveredAddresses);

            // Collect phone numbers of persons at covered addresses
            List<String> phoneNumbers = personList.stream()
                    .filter(p -> coveredAddresses.contains(p.getAddress()))
                    .map(Person::getPhone)
                    .collect(Collectors.toList());

            log.info("Found {} phone numbers for station {}", phoneNumbers.size(), stationNumber);
            return phoneNumbers;

        } catch (Exception e) {
            log.error("Error occurred while retrieving phone numbers for station {}: {}", stationNumber, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get all households served by one or more fire stations, grouped by address.
     * Each household lists residents with name, phone, age, medications and allergies.
     * Accepts a comma-separated list of station numbers.
     * Data is sourced from data.json.
     *
     * @param stationNumbers comma-separated station numbers, e.g. "1,2,3"
     * @return FloodResponse containing households grouped by address
     */
    public FloodResponse getHouseholdsByStations(String stationNumbers) {
        try {
            // Parse station numbers
            Set<Integer> stationSet = new java.util.HashSet<>();
            for (String s : stationNumbers.split(",")) {
                try {
                    stationSet.add(Integer.parseInt(s.trim()));
                } catch (NumberFormatException e) {
                    log.warn("Skipping invalid station number: '{}'", s.trim());
                }
            }

            if (stationSet.isEmpty()) {
                log.warn("No valid station numbers provided: '{}'", stationNumbers);
                return new FloodResponse(new ArrayList<>());
            }

            // Read all data from data.json
            Data data = readDataJson();
            List<Firestation> fireStationList = data.getFirestations() != null ? data.getFirestations() : new ArrayList<>();
            List<Person> personList = data.getPersons() != null ? data.getPersons() : new ArrayList<>();
            List<Medicalrecord> medicalRecordList = data.getMedicalrecords() != null ? data.getMedicalrecords() : new ArrayList<>();

            log.info("Loaded from data.json — firestations: {}, persons: {}, medicalrecords: {}",
                    fireStationList.size(), personList.size(), medicalRecordList.size());

            // Collect all addresses covered by the requested stations
            Set<String> coveredAddresses = fireStationList.stream()
                    .filter(fs -> fs.getStation() != null && stationSet.contains(fs.getStation()))
                    .map(Firestation::getAddress)
                    .collect(Collectors.toSet());

            log.info("Addresses covered by stations {}: {}", stationNumbers, coveredAddresses);

            // Group persons by address, build FloodHousehold per address
            Map<String, List<Person>> personsByAddress = personList.stream()
                    .filter(p -> coveredAddresses.contains(p.getAddress()))
                    .collect(Collectors.groupingBy(Person::getAddress));

            List<FloodHousehold> households = personsByAddress.entrySet().stream()
                    .map(entry -> {
                        String address = entry.getKey();
                        List<FloodResident> residents = entry.getValue().stream()
                                .map(person -> {
                                    Medicalrecord record = medicalRecordList.stream()
                                            .filter(m -> m.getFirstName().equalsIgnoreCase(person.getFirstName())
                                                    && m.getLastName().equalsIgnoreCase(person.getLastName()))
                                            .findFirst()
                                            .orElse(null);

                                    int age = record != null ? getAge(record) : -1;
                                    List<String> medications = record != null && record.getMedications() != null
                                            ? List.of(record.getMedications()) : new ArrayList<>();
                                    List<String> allergies = record != null && record.getAllergies() != null
                                            ? List.of(record.getAllergies()) : new ArrayList<>();

                                    return new FloodResident(
                                            person.getFirstName(),
                                            person.getLastName(),
                                            person.getPhone(),
                                            age,
                                            medications,
                                            allergies
                                    );
                                })
                                .collect(Collectors.toList());

                        return new FloodHousehold(address, residents);
                    })
                    .collect(Collectors.toList());

            log.info("Found {} households for stations {}", households.size(), stationNumbers);
            return new FloodResponse(households);

        } catch (Exception e) {
            log.error("Error retrieving households for stations '{}': {}", stationNumbers, e.getMessage());
            return new FloodResponse(new ArrayList<>());
        }
    }

    /**
     * Calculate age in years from a medical record's birthdate
     */
    private int getAge(Medicalrecord medicalRecord) {
        if (medicalRecord == null || medicalRecord.getBirthdate() == null) {
            return -1;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            LocalDate birthDate = LocalDate.parse(medicalRecord.getBirthdate(), formatter);
            return (int) ChronoUnit.YEARS.between(birthDate, LocalDate.now());
        } catch (Exception e) {
            log.error("Error parsing birthdate: {}", e.getMessage());
            return -1;
        }
    }

    /**
     * Helper method to read the aggregated data.json file as a Data object
     */
    private Data readDataJson() throws Exception {
        if (!dataResource.exists()) {
            log.error("data.json resource does not exist at {}", dataResource.getPath());
            return new Data(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }
        try (InputStream inputStream = dataResource.getInputStream()) {
            return readFromFileUtil.readObjectFromInputStream(inputStream, Data.class);
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
