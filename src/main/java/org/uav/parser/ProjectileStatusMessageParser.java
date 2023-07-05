package org.uav.parser;

import org.joml.Vector3f;
import org.uav.model.ProjectileStatus;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class ProjectileStatusMessageParser implements MessageParser<List<ProjectileStatus>>{
    @Override
    public List<ProjectileStatus> parse(String input) {
        return Arrays.stream(input.split(";")).skip(1).map(this::toProjectile).toList();
    }

    private ProjectileStatus toProjectile(String input) {
        Scanner scanner = new Scanner(input);
        scanner.useDelimiter(",");

        return new ProjectileStatus(
                Integer.parseInt(scanner.next()),
                new Vector3f(
                        Float.parseFloat(scanner.next()),
                        Float.parseFloat(scanner.next()),
                        Float.parseFloat(scanner.next())),
                new Vector3f(
                        Float.parseFloat(scanner.next()),
                        Float.parseFloat(scanner.next()),
                        Float.parseFloat(scanner.next())
                )
        );
    }
}
