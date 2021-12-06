package wsn;

import net.tinyos.prowler.Application;
import net.tinyos.prowler.Node;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class SimpleApplication extends Application {
    Queue<Object> messages = new ArrayDeque<>();

    public SimpleApplication(Node node) {
        super(node);
    }

    @Override
    public boolean sendMessage(Object message) {
        super.sendMessage(message);
        messages.add(message);
        return true;
    }

    @Override
    public void receiveMessage(Object message, Node sender) {
        super.receiveMessage(message, sender);
        messages.add(message);
    }

    public Queue<Object> getMessages() {
        return messages;
    }
}
