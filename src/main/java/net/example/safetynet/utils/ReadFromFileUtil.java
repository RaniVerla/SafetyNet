package net.example.safetynet.utils;

import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.HashSet;

@Component
public class ReadFromFileUtil {

    public <T> HashSet<T> readFromFile(String filePath)
    {
        ObjectMapper mapper=new ObjectMapper();
        File file=new File(filePath);
        if(!file.exists() || file.length()==0)
        {
            return new HashSet<>();
        }
        return mapper.readValue(file, new TypeReference<HashSet<T>>() {
        });
    }
}
