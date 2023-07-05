package org.uav.parser;

import org.uav.queue.DroneRequestReplyMessage;

import java.util.Scanner;

public class DroneRequestReplyParser implements MessageParser<DroneRequestReplyMessage>{
    @Override
    public DroneRequestReplyMessage parse(String input) {
        Scanner scanner = new Scanner(input);
        scanner.useDelimiter(",");
        int droneId = Integer.parseInt(scanner.next());
        int steerPort = Integer.parseInt(scanner.next());
        int utilsPort = Integer.parseInt(scanner.next());
        return new DroneRequestReplyMessage(droneId, steerPort, utilsPort);
    }
}

