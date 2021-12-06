package wsn;

import javafx.scene.shape.Line;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConnectionInstances {
    static private List<ConnectionLine> connectionLines;

    public static List<ConnectionLine> getConnectionLines() {
        return connectionLines;
    }

    static void setConnectionLines(List<Line> lines) {
        connectionLines = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            connectionLines.add(new ConnectionLine(lines.get(i)));
        }
    }

    static ConnectionLine getFreeConnectionLine() {
        for (ConnectionLine connectionLine : connectionLines) {
            if (connectionLine.isNotBusy()) {
                return connectionLine;
            }
        }
        return null;
    }
}
