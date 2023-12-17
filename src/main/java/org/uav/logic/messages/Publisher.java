package org.uav.logic.messages;

import java.util.List;
import java.util.function.Consumer;

public interface Publisher {

    List<Consumer<Message>> getSubscribers();

    default void subscribe(Consumer<Message> subscriber) {
        getSubscribers().add(subscriber);
    }

    default void notifySubscriber(Message message)
    {
        for (Consumer<Message> subscriber : getSubscribers()) {
            subscriber.accept(message);
        }
    }
}
