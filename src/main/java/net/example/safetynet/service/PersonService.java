package net.example.safetynet.service;

import lombok.extern.slf4j.Slf4j;
import net.example.safetynet.model.ChildAlertResponse;
import net.example.safetynet.model.Data;
import net.example.safetynet.model.Medicalrecord;
import net.example.safetynet.model.Person;
import net.example.safetynet.model.PersonInfo;
import net.example.safetynet.model.PersonInfoLastNameResponse;
import net.example.safetynet.utils.ReadFromFileUtil;
import net.example.safetynet.utils.WriteToFileUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
public class PersonService {
    private final ReadFromFileUtil readFromFileUtil;
    private final WriteToFileUtil writeToFileUtil;

    // Use ClassPathResource for reading data files
    private final ClassPathResource personResource = new ClassPathResource("/safetynet/person.json");
    private final ClassPathResource medicalRecordResource = new ClassPathResource("/safetynet/medicalrecord.json");
    private final ClassPathResource dataResource = new ClassPathResource("/safetynet/data.json");

    // Keep File for writing (assuming write operations use file system)
    File filePath = new File("src/main/resources/safetynet/person.json");

    public PersonService(ReadFromFileUtil readFromFileUtil, WriteToFileUtil writeToFileUtil) {
        this.readFromFileUtil = readFromFileUtil;
        this.writeToFileUtil = writeToFileUtil;
    }

    public ResponseEntity<String> addPerson(Person person) {

        try {
            List<Person> personList = readFromFileUtil.readFromFile(filePath, new TypeReference<List<Person>>() {
            });
            personList.add(person);
            writeToFileUtil.writeToFile(personList, filePath);
            return ResponseEntity.status(HttpStatus.CREATED).body("Person added successfully");
        } catch (Exception e) {
            log.error("Error occurred while writing to file :{}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving person :" + e.getMessage());
        }
    }

    public List<Person> getAllPersons() {
        return readFromFileUtil.readFromFile(filePath, new TypeReference<List<Person>>() {
        });
    }

    public ResponseEntity<String> updatePerson(Person updatedperson, String lastName, String firstName) {

        try {
            List<Person> personList = readFromFileUtil.readFromFile(filePath, new TypeReference<List<Person>>() {
            });

            log.info("Person List :{}", personList);

            log.info("firstName :{} lastName :{} ", firstName, lastName);

            Person existingPerson = personList.stream()
                    .filter(p -> p.getFirstName().equalsIgnoreCase(firstName) && p.getLastName().equalsIgnoreCase(lastName))
                    .findFirst()
                    .orElse(null);

            log.debug("existing Person :{}", existingPerson);

            if (existingPerson != null) {
                existingPerson.setFirstName(updatedperson.getFirstName());
                existingPerson.setLastName(updatedperson.getLastName());
                existingPerson.setCity(updatedperson.getCity());
                existingPerson.setAddress(updatedperson.getAddress());
                existingPerson.setPhone(updatedperson.getPhone());
                existingPerson.setEmail(updatedperson.getEmail());
                existingPerson.setZip(updatedperson.getZip());

//                personList.add(existingPerson);
                writeToFileUtil.writeToFile(personList, filePath);

                return ResponseEntity.status(HttpStatus.CREATED).body("Person updated successfully");
            } else {
                log.error("Person doesn't exist ");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Person with name " + firstName + " " + lastName + " doesn't exists");
            }
        } catch (Exception e) {
            log.error("Error occurred while writing to file :{}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving person :" + e.getMessage());
        }
    }

    public ResponseEntity<String> deletePerson(String lastName, String firstName) {

        try {
            List<Person> personList = readFromFileUtil.readFromFile(filePath, new TypeReference<List<Person>>() {
            });
            log.info("Person List :{} ", personList);
            Person deletePerson = personList.stream()
                    .filter(p -> lastName.equalsIgnoreCase(p.getLastName())
                            && firstName.equalsIgnoreCase((p.getFirstName())))
                    .findFirst()
                    .orElse(null);
            log.info("Delete Person :{} ", deletePerson);
            if (deletePerson != null) {
                personList.remove(deletePerson);
                writeToFileUtil.writeToFile(personList, filePath);
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Person deleted successfully");
            } else {
                log.error("Person doesn't exist ");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Person with name " + firstName + " " + lastName + " doesn't exists");
            }
        } catch (Exception e) {
            log.error("Error occurred while writing to file :{}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting person :" + e.getMessage());
        }
    }

    public ChildAlertResponse getChildrenAtAddress(String address) {
        try {
            // Load all data from data.json
            Data data = readDataJson();
            List<Person> personList = data.getPersons() != null ? data.getPersons() : new ArrayList<>();
            List<Medicalrecord> medicalRecordList = data.getMedicalrecords() != null ? data.getMedicalrecords() : new ArrayList<>();

            log.info("Loaded from data.json — persons: {}, medicalrecords: {}",
                    personList.size(), medicalRecordList.size());

            String normalizedAddress = normalize(address);

            // All people living at the given address
            List<Person> household = personList.stream()
                    .filter(p -> normalize(p.getAddress()).equals(normalizedAddress))
                    .collect(Collectors.toList());

            log.info("Household members at '{}': {}", address, household.size());

            if (household.isEmpty()) {
                return new ChildAlertResponse(new ArrayList<>());
            }

            List<ChildAlertResponse.ChildInfo> children = new ArrayList<>();

            for (Person person : household) {
                int age = getAge(person, medicalRecordList);
                if (age >= 0 && age <= 18) {
                    // Other household members (everyone else at the address)
                    List<ChildAlertResponse.HouseholdMember> otherMembers = household.stream()
                            .filter(p -> !p.equals(person))
                            .map(p -> new ChildAlertResponse.HouseholdMember(p.getFirstName(), p.getLastName()))
                            .collect(Collectors.toList());

                    children.add(new ChildAlertResponse.ChildInfo(
                            person.getFirstName(),
                            person.getLastName(),
                            age,
                            otherMembers
                    ));
                }
            }

            log.info("Found {} children at address '{}'", children.size(), address);
            return new ChildAlertResponse(children);

        } catch (Exception e) {
            log.error("Error getting children at address {}: {}", address, e.getMessage());
            return new ChildAlertResponse(new ArrayList<>());
        }
    }

    private String normalize(String input) {
        return input == null ? "" : input.trim().toLowerCase();
    }

    private int getAge(Person person, List<Medicalrecord> medicalRecordList) {
        Medicalrecord medicalRecord = medicalRecordList.stream()
                .filter(m -> m.getFirstName().equalsIgnoreCase(person.getFirstName())
                        && m.getLastName().equalsIgnoreCase(person.getLastName()))
                .findFirst()
                .orElse(null);

        if (medicalRecord == null || medicalRecord.getBirthdate() == null) {
            log.warn("No medical record found for {} {}", person.getFirstName(), person.getLastName());
            return -1; // or some default
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            LocalDate birthDate = LocalDate.parse(medicalRecord.getBirthdate(), formatter);
            LocalDate today = LocalDate.now();
            return (int) ChronoUnit.YEARS.between(birthDate, today);
        } catch (Exception e) {
            log.error("Error parsing birthdate for {} {}: {}", person.getFirstName(), person.getLastName(), e.getMessage());
            return -1;
        }
    }

    private <T> List<T> readFromResource(ClassPathResource resource, TypeReference<List<T>> typeReference) throws Exception {
        if (!resource.exists()) {
            log.error("Resource {} does not exist", resource.getPath());
            return new ArrayList<>();
        }
        try (java.io.InputStream inputStream = resource.getInputStream()) {
            return readFromFileUtil.readFromInputStream(inputStream, typeReference);
        }
    }

    private Data readDataJson() throws Exception {
        if (!dataResource.exists()) {
            log.error("data.json resource does not exist at {}", dataResource.getPath());
            return new Data(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }
        try (java.io.InputStream inputStream = dataResource.getInputStream()) {
            return readFromFileUtil.readObjectFromInputStream(inputStream, Data.class);
        }
    }

    public List<String> getEmailsByCity(String city) {
        try {
            log.info("Fetching emails for city: {}", city);

            // Read all data from data.json
            Data data = readDataJson();
            List<Person> personList = data.getPersons() != null ? data.getPersons() : new ArrayList<>();

            log.info("Loaded {} persons from data.json", personList.size());

            return personList.stream()
                    .filter(p -> p.getCity() != null && p.getCity().equalsIgnoreCase(city))
                    .map(Person::getEmail)
                    .distinct()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error occurred while fetching emails for city {}: {}", city, e.getMessage());
            return new ArrayList<>();
        }
    }

    public PersonInfoLastNameResponse getPersonInfoByLastName(String lastName) {
        try {
            log.info("Fetching person info for last name: {}", lastName);

            // Read all data from data.json
            Data data = readDataJson();
            List<Person> personList = data.getPersons() != null ? data.getPersons() : new ArrayList<>();
            List<Medicalrecord> medicalRecordList = data.getMedicalrecords() != null ? data.getMedicalrecords() : new ArrayList<>();

            log.info("Loaded {} persons, {} medical records from data.json",
                    personList.size(), medicalRecordList.size());

            List<PersonInfo> personInfos = personList.stream()
                    .filter(p -> p.getLastName() != null && p.getLastName().equalsIgnoreCase(lastName))
                    .map(person -> {
                        Medicalrecord medicalRecord = medicalRecordList.stream()
                                .filter(m -> m.getFirstName().equalsIgnoreCase(person.getFirstName())
                                        && m.getLastName().equalsIgnoreCase(person.getLastName()))
                                .findFirst()
                                .orElse(null);

                        int age = -1;
                        List<String> medications = new ArrayList<>();
                        List<String> allergies = new ArrayList<>();

                        if (medicalRecord != null) {
                            age = getAge(person, medicalRecordList);
                            if (medicalRecord.getMedications() != null) {
                                medications = List.of(medicalRecord.getMedications());
                            }
                            if (medicalRecord.getAllergies() != null) {
                                allergies = List.of(medicalRecord.getAllergies());
                            }
                        }

                        return new PersonInfo(
                                person.getFirstName(),
                                person.getLastName(),
                                person.getAddress(),
                                age,
                                person.getEmail(),
                                medications,
                                allergies
                        );
                    })
                    .collect(Collectors.toList());

            return new PersonInfoLastNameResponse(personInfos);
        } catch (Exception e) {
            log.error("Error occurred while fetching person info for last name {}: {}", lastName, e.getMessage());
            return new PersonInfoLastNameResponse(new ArrayList<>());
        }
    }
}
