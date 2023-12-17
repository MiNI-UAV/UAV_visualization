package org.uav.logic.messages;

import lombok.Getter;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

@Getter
public class MessageBoard {

    List<Pair<Float, Message>> messages;
    List<Pair<Float, Message>> criticalMessages;
    Semaphore semaphore;

    public MessageBoard() {
        messages = new ArrayList<>();
        criticalMessages = new ArrayList<>();
        semaphore = new Semaphore(1, true);
    }

    public Consumer<Message> produceSubscriber() {
        return message -> {
            try {
                semaphore.acquire();
                if(message.isCritical())
                    criticalMessages.removeIf(p -> p.getValue1().getCategory().equals(message.getCategory()));
                (message.isCritical()? criticalMessages: messages).add(Pair.with((float)glfwGetTime(), message));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                semaphore.release();
            }
        };
    }

    public void deprecateMessages() {
        try {
            semaphore.acquire();
            messages.removeIf(p ->  getTimeLeft(p) < 0);
            criticalMessages.removeIf(p ->  getTimeLeft(p) < 0);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            semaphore.release();
        }
    }

    public float getTimeLeft(Pair<Float, Message> p) {
        return p.getValue1().getShowTimeS() - ((float)glfwGetTime() - p.getValue0());
    }

}
