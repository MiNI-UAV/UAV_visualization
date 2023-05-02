package org.uav.serializer;

public interface MessageSerializer<T> {
    String serialize(T obj);
}
