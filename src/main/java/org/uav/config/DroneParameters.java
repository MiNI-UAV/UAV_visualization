package org.uav.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.joml.Vector3f;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DroneParameters {
    Rotors rotors;
    Control control;

    @Getter
    @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Rotors {
        List<Vector3f> positions;
        List<Integer> direction;

        public void setPositions(String[] strs) {
            positions = Arrays.stream(strs)
                    .map(s -> Arrays.stream(s.split(" "))
                            .map(Float::parseFloat)
                            .toList())
                    .map(floats -> new Vector3f(
                            floats.get(0),
                            floats.get(1),
                            floats.get(2)))
                    .toList();
        }
    }

    @Value
    @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Control {
        float maxSpeed; // Radians per Second
    }

    public static DroneParameters load(String path) {
        try {
            ObjectMapper mapper = new XmlMapper();
            return mapper.readValue(new File(System.getProperty("user.dir") + "/" + path), DroneParameters.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
