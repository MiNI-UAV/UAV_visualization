package org.opengl.parser;

public interface MessageParser<T> {
    T parse(String input);
}
