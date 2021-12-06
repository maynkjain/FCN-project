package wsn;

import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

public class ConnectionLine {
    private Line line;
    private boolean busy = false;

    public ConnectionLine(Line line) {
        this.line = line;
    }

    public void createConnection(SensorNode a, SensorNode b) {
        line.setStartX(a.getTranslateX());
        line.setStartY(a.getTranslateY());
        line.setEndX(b.getTranslateX());
        line.setEndY(b.getTranslateY());
        line.setFill(Color.BLACK);
        busy = true;
    }

    public void breakConnection() {
        line.setStartX(0);
        line.setEndX(0);
        line.setStartY(0);
        line.setEndY(0);
        busy = false;
    }

    public boolean isBusy() {
        return busy;
    }

    public boolean isNotBusy() {
        return !isBusy();
    }
}
