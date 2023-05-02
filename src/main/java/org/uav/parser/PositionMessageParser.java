package org.uav.parser;

import org.joml.Vector3f;
import org.uav.queue.PositionMessage;

import java.util.Scanner;

public class PositionMessageParser implements MessageParser<PositionMessage> {
    @Override
    public PositionMessage parse(String input) {
        Scanner scanner = new Scanner(input);
        scanner.useDelimiter("[a-zA-Z:, ]+");
        float x = Float.parseFloat(scanner.next());
        float y = Float.parseFloat(scanner.next());
        float z = Float.parseFloat(scanner.next());

        float rx = Float.parseFloat(scanner.next());
        float ry = Float.parseFloat(scanner.next());
        float rz = Float.parseFloat(scanner.next());

        return new PositionMessage(new Vector3f(x, y, z), new Vector3f(rx, ry, rz));
    }
}
