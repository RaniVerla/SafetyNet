package net.example.safetynet.service;

import lombok.extern.slf4j.Slf4j;
import net.example.safetynet.model.Person;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.FileWriter;
import java.io.IOException;

@Slf4j
@Service
public class PersonService {
    public ResponseEntity<String> addPerson(Person person) {

        try(FileWriter file=new FileWriter("C:/Users/raniv/safetnet_json/person.json"))
        {
            ObjectMapper mapper=new ObjectMapper();
            String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(person);
            file.write(jsonString);
            return ResponseEntity.ok("Person data save successfully");

        }catch(IOException e)
        {
            log.error("Exception occured while writing the file :{} ",e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving Json :" +e.getMessage());
        }
    }
}
