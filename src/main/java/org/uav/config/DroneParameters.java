package org.uav.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.joml.Vector3f;

import java.util.Arrays;
import java.util.List;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DroneParameters {
    Rotors rotors;

    @Getter
    @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Rotors {
        @JacksonXmlElementWrapper(useWrapping = false)
        List<Rotor> rotor;

        @Getter
        @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Rotor {
            Vector3f position;
            Integer direction;
            Float maxSpeed;
            Float hoverSpeed;

            public void setPosition(String str) {

                var fields = Arrays.stream(str.split(","))
                                .map(Float::parseFloat).toArray(Float[]::new);
                position = new Vector3f(fields[0],fields[1], fields[2]);
            }
        }
    }
}
