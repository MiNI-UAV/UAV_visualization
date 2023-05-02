package org.uav.parser;

public interface MessageParser<T> {
    T parse(String input);
}
