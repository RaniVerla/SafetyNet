package net.example.safetynet.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class ReadFromFileUtil {

    private final ObjectMapper mapper = new ObjectMapper();

    public <T> List<T> readFromFile(File filePath, TypeReference<List<T>> typeReference) {

        log.info("Attempting to read from :{} ", filePath);

        if (!filePath.exists()) {
            log.warn("File doesn't exist :{}", filePath);
            return new ArrayList<>();
        }

        if (filePath.length() == 0) {
            log.warn("File is empty :{}", filePath);
            return new ArrayList<>();
        }

        try {
            List<T> readList = mapper.readValue(filePath, typeReference);
            return readList;
        } catch (Exception ex) {
            log.error("Error reading from file :{}", ex.getMessage());
            throw ex;
        }
    }

    public <T> List<T> readFromInputStream(InputStream inputStream, TypeReference<List<T>> typeReference) {
        try {
            return mapper.readValue(inputStream, typeReference);
        } catch (Exception ex) {
            log.error("Error reading from input stream :{}", ex.getMessage());
            throw ex;
        }
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

}
