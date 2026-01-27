package net.example.safetynet.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class WriteToFileUtil {

    public <T> void writeToFile(List<T> objects, String filePath)
    {
        try(FileWriter file=new FileWriter(filePath)){

            ObjectMapper mapper = new ObjectMapper();
            String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(objects);
            file.write(jsonString);

        }catch(IOException e)
        {
            log.error(e.getMessage());
        }
    }
}
