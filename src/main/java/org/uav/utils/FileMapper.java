package org.uav.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class FileMapper {
    public static <T> T load(Class<T> targetClass, Path path, ObjectMapper mapper) {
        try {
            return mapper.readValue(new File(path.toString()), targetClass);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> void save(T object, Path path, ObjectMapper mapper) {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        try {
            mapper.writeValue(new File(path.toString()), object);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
