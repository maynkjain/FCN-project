package wsn;

import javafx.animation.Transition;
import javafx.application.Platform;

public class CircleMoveTransition implements Runnable {
    SensorNode sensorNode;

    private double startX;
    private double startY;
    private double endX;
    private double endY;

    double interpolation = 0;
    final int TICKS_PER_SECOND = 25;
    final int SKIP_TICKS = 1000 / TICKS_PER_SECOND;
    final int MAX_FRAMESKIP = 5;

    public CircleMoveTransition(SensorNode sensorNode) {
        this.sensorNode = sensorNode;
    }

    @Override
    public void run() {
        double next_game_tick = System.currentTimeMillis();

        int loops;
    }
}
