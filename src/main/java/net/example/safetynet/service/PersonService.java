package net.example.safetynet.service;

import lombok.extern.slf4j.Slf4j;
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
public class PersonService {
    private final ReadFromFileUtil readFromFileUtil;
    private final WriteToFileUtil writeToFileUtil;

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
}
