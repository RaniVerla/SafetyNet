package net.example.safetynet.utils;

import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.ArrayList;

@Component
public class ReadFromFileUtil {

    public <T> ArrayList<T>  readFromFile(String filePath)
    {
        ObjectMapper mapper=new ObjectMapper();
        File file=new File(filePath);
        if(!file.exists() || file.length()==0)
        {
            return new ArrayList<>();
        }
        return mapper.readValue(file, new TypeReference<ArrayList<T>>() {
        });
    }
}
