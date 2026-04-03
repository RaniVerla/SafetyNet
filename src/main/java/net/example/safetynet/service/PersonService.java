package net.example.safetynet.service;

import lombok.extern.slf4j.Slf4j;
import net.example.safetynet.model.ChildAlertResponse;
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
            log.info("Step 1: Reading person resource...");
            List<Person> personList = readFromResource(personResource, new TypeReference<List<Person>>() {});
            log.info("Step 2: Total persons read: {}", personList.size());

            List<Medicalrecord> medicalRecordList = readFromResource(medicalRecordResource, new TypeReference<List<Medicalrecord>>() {});
            log.info("Step 3: Total medical records read: {}", medicalRecordList.size());


            log.info("===== Searching for address: '{}' =====", address);

            String normalizedAddress = normalize(address);

            // Get all persons at the address
            List<Person> household =  personList.stream()
                    .filter(p -> {
                        boolean match = normalize(p.getAddress()).equals(normalizedAddress);
                        if (match) {
                            log.debug("Matched: {} {}", p.getFirstName(), p.getLastName());
                        }
                        return match;
                    })
                    .collect(Collectors.toList());

            household.forEach(p -> log.info("Household member: {} {}", p.getFirstName(), p.getLastName()));
            household.forEach(System.out::println);

            // Log household members' first and last names
            log.info("Household members at address {}: ", address);
            for (Person person : household) {
                log.info("  - {} {}", person.getFirstName(), person.getLastName());
            }

            List<ChildAlertResponse.ChildInfo> children = new ArrayList<>();

            for (Person person : household) {
                int age = getAge(person, medicalRecordList);
                if (age <= 18) {
                    // This is a child
                    List<ChildAlertResponse.HouseholdMember> members = household.stream()
                            .filter(p -> !p.equals(person)) // exclude the child
                            .map(p -> new ChildAlertResponse.HouseholdMember(p.getFirstName(), p.getLastName()))
                            .collect(Collectors.toList());

                    children.add(new ChildAlertResponse.ChildInfo(person.getFirstName(), person.getLastName(), age, members));
                }
            }

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
}
