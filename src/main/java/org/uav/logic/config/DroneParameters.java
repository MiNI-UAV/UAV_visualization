package org.uav.logic.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Getter
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DroneParameters {
    Rotors rotors;
    @JsonDeserialize(using = ProjectileDeserializer.class)
    List<Projectile> ammo;
    @JsonDeserialize(using = ProjectileDeserializer.class)
    List<Projectile> cargo;

    public static class ProjectileDeserializer extends JsonDeserializer<List<Projectile>> {
        @Override
        public List<Projectile> deserialize(JsonParser jp, DeserializationContext cntx)
                throws IOException {
            List<Projectile> projectile = new ArrayList<>();
            TreeNode s = jp.readValueAsTree();
            for (Iterator<String> it = s.fieldNames(); it.hasNext(); ) {
                String field = it.next();
                if(field.equals("no")) continue;
                var e = s.get(field).traverse(jp.getCodec()).readValueAs(Projectile.class);
                e.setName(field);
                projectile.add(e);
            }
            return projectile;
        }
    }

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

    @Data
    @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Projectile {
        @JsonIgnore
        String name;
        float reload;
        @JacksonXmlProperty(localName = "ammount")
        int amount;
    }
}
