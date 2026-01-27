package net.example.safetynet.service;

import lombok.extern.slf4j.Slf4j;
import net.example.safetynet.model.Person;
import net.example.safetynet.utils.ReadFromFileUtil;
import net.example.safetynet.utils.WriteToFileUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

@Slf4j
@Service
public class PersonService {
    private final ReadFromFileUtil readFromFileUtil;
    private final WriteToFileUtil writeToFileUtil;

    String filePath="C:/Users/raniv/safetnet_json/person.json";

    public PersonService(ReadFromFileUtil readFromFileUtil, WriteToFileUtil writeToFileUtil) {
        this.readFromFileUtil = readFromFileUtil;
        this.writeToFileUtil = writeToFileUtil;
    }

    public ResponseEntity<String> addPerson(List<Person> person) {

        try{
            List<Person> personList=readFromFileUtil.readFromFile(filePath);
            personList.addAll(person);
            writeToFileUtil.writeToFile(personList,filePath);
            return ResponseEntity.ok("Person added successfully");
        }
        catch (Exception e)
        {
            log.error("Error occurred while writing to file :{}" ,e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving person :" +e.getMessage());
        }


    }

    public List<Person> getAllPersons() {
        List<Person> personList=readFromFileUtil.readFromFile(filePath);
        return personList;
    }
}
