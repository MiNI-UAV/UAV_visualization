package org.opengl.serializer;

public interface MessageSerializer<T> {
    String serialize(T obj);
}
