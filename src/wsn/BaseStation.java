package wsn;

import net.tinyos.prowler.Application;
import net.tinyos.prowler.Node;
import net.tinyos.prowler.RadioModel;
import net.tinyos.prowler.Simulator;

public class BaseStation extends Node {
    static BaseStation baseStation;
    public BaseStation(Simulator sim, RadioModel radioModel) {
        super(sim, radioModel);
    }

    public static BaseStation getInstance(Simulator sim, RadioModel radioModel) {
        if (baseStation == null) {
            baseStation = new BaseStation(sim, radioModel);
        }
        return baseStation;
    }

    @Override
    protected void receptionBegin(double strength, Object stream) {
        Log.i("Recieved message at strength " + strength);
    }

    @Override
    protected void receptionEnd(double strength, Object stream) {
        Log.i("Reception ended.");
    }

    @Override
    public boolean sendMessage(Object message, Application app) {
        Log.i("Sending message to " + app);
        app.receiveMessage(message, app.getSender());
        return false;
    }
}
