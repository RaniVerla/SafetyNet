package net.example.safetynet.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class WriteToFileUtil {

    private final ObjectMapper mapper = new ObjectMapper();

    public <T> void writeToFile(List<T> objects, File filePath) {
        try {
            if (!filePath.exists()) {
                boolean created = filePath.mkdirs();
                log.info("created directory :{}c -sucess", filePath.getAbsolutePath());
            }

            mapper.writerWithDefaultPrettyPrinter().writeValue(filePath, objects);

            log.info("Successfully updated the file :{}", filePath.getAbsolutePath());

        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public <T> void writeObjectToFile(T object, File filePath) {
        try {
            if (!filePath.exists()) {
                filePath.getParentFile().mkdirs();
                log.info("Created directory: {}", filePath.getParentFile().getAbsolutePath());
            }

            mapper.writerWithDefaultPrettyPrinter().writeValue(filePath, object);

            log.info("Successfully updated the file :{}", filePath.getAbsolutePath());

        } catch (Exception e) {
            log.error("Error writing object to file: {}", e.getMessage());
        }
    }
}
