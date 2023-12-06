package org.uav.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.Primitives;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Arrays;

public class FileMapper {
    public static <T> T load(Class<T> targetClass, Path path, ObjectMapper mapper) {
        try {
            return mapper.readValue(new File(path.toString()), targetClass);
        } catch (IOException e) {
            throw new RuntimeException("Could not load configuration file: " + e.getMessage());
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

    public static void validateNullable(Object o) {
        var c = o.getClass();
        if(c.isPrimitive() || Primitives.isWrapperType(c) || c.isArray() || c.isEnum()) return;
        var fields = o.getClass().getDeclaredFields();
        Arrays.stream(fields).forEach(f -> {
            try {
                if(f.trySetAccessible()) {
                    var child = f.get(o);
                    if(child == null) {
                        if(isFieldNullable(f))
                            return;
                        throw new IOException(f.getName() + " field is null!");
                    }
                    validateNullable(child);
                }
            } catch (IllegalAccessException | IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static boolean isFieldNullable(Field f) {
        return Arrays.stream(f.getDeclaredAnnotations()).anyMatch(a -> a.annotationType().equals(Nullable.class));
    }
}
