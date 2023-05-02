package org.opengl.parser;

import org.opengl.model.PropellerMessage;

import java.util.Scanner;


public class PropellerMessageParser implements MessageParser<PropellerMessage> {
    @Override
    public PropellerMessage parse(String input) {
        Scanner scanner = new Scanner(input);
        scanner.useDelimiter("[a-zA-Z:, ]+");
        var propellerMessage = new PropellerMessage();
        while(scanner.hasNext())
            propellerMessage.propellers.add(Float.parseFloat(scanner.next()));

        return propellerMessage;
    }
}
