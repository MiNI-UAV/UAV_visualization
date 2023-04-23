package org.opengl.parser;

import org.joml.Vector3f;
import org.opengl.model.DroneStatus;

import java.util.Scanner;

public class MessageParserDroneStatus implements MessageParser<DroneStatus> {
    @Override
    public DroneStatus parse(String input) {
        Scanner scanner = new Scanner(input);
        scanner.useDelimiter("[a-zA-Z:, ]+");
        float x = Float.parseFloat(scanner.next());
        float y = Float.parseFloat(scanner.next());
        float z = Float.parseFloat(scanner.next());

        float rx = Float.parseFloat(scanner.next());
        float ry = Float.parseFloat(scanner.next());
        float rz = Float.parseFloat(scanner.next());

        return new DroneStatus(new Vector3f(x, y, z), new Vector3f(rx, ry, rz));
    }
}
