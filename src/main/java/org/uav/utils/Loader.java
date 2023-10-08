package org.uav.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class Loader {
    public static <T> T load(Class<T> targetClass, Path path, ObjectMapper mapper) {
        try {
            return mapper.readValue(new File(path.toString()), targetClass);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
