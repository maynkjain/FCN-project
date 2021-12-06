package wsn;

import net.tinyos.prowler.Application;
import net.tinyos.prowler.Node;

public class BaseStationApplication extends Application {
    public BaseStationApplication(Node node) {
        super(node);
    }

    @Override
    public boolean sendMessage(Object message) {
        // broadcast message
        return true;
    }

    @Override
    public void receiveMessage(Object message, Node sender) {
        Log.i("BS: Received message " + message);
    }
}
