package org.uav.parser;

import org.uav.status.DroneStatus;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class DroneStatusMessageParser implements MessageParser<List<DroneStatus>> {
    @Override
    public List<DroneStatus> parse(String input) {
        return Arrays.stream(input.split(";")).map(this::toDrone).toList();
    }

    private DroneStatus toDrone(String input) {
        var drone = new DroneStatus();
        Scanner scanner = new Scanner(input);
        scanner.useDelimiter(",");
        scanner.next();

        drone.position.x = Float.parseFloat(scanner.next());
        drone.position.y = Float.parseFloat(scanner.next());
        drone.position.z = Float.parseFloat(scanner.next());

        drone.rotation.x = Float.parseFloat(scanner.next());
        drone.rotation.y = Float.parseFloat(scanner.next());
        drone.rotation.z = Float.parseFloat(scanner.next());

        drone.linearVelocity.x = Float.parseFloat(scanner.next());
        drone.linearVelocity.y = Float.parseFloat(scanner.next());
        drone.linearVelocity.z = Float.parseFloat(scanner.next());

        drone.angularVelocity.x = Float.parseFloat(scanner.next());
        drone.angularVelocity.y = Float.parseFloat(scanner.next());
        drone.angularVelocity.z = Float.parseFloat(scanner.next());

        while (scanner.hasNext()){
            drone.propellers.add(Float.parseFloat(scanner.next()));
        }

        return drone;
    }
}
