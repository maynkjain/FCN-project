package wsn;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import net.tinyos.prowler.RadioModel;
import net.tinyos.prowler.RayleighRadioModel;
import net.tinyos.prowler.Simulator;
import java.text.SimpleDateFormat;
import java.util.*;


public class WsnProject extends Application {
    public static int W = 600;
    public static int H = 600;

    private static TaskQueue taskQueue = new TaskQueue();

    private static TaskQueue nodeShares = new TaskQueue();
    public BaseStation baseStation;

    List<Cluster> clusters;
    private static List<TaskQueue> clustersShare = new ArrayList<>();

    java.util.Timer timer;

    private SensorNode[] sensorNodes = new SensorNode[100];

    public void handleResize(int w, int h) {
        W = w;
        H = h;
    }



    public static void addShare(Runnable share) { nodeShares.add(share); }
    public static void addTask(Runnable task) {
        taskQueue.add(task);
    }

    public static void runTasks() {
        while (!taskQueue.isEmpty()) {
            Runnable task = taskQueue.remove();
            task.run();
        }
    }

    public static void main(String []args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Group group = new Group();
        Rectangle rectangle = new Rectangle(W, H, Color.WHITE);
        ObservableList list = group.getChildren();
        list.add(rectangle);

        Line xLine = new Line();
        xLine.setStartX(0);
        xLine.setEndX(W);
        xLine.setStartY(H / 2);
        xLine.setEndY(H / 2);

        Line yLine = new Line();
        yLine.setStartX(W / 2);
        yLine.setStartY(0);
        yLine.setEndX(W / 2);
        yLine.setEndY(H);

        xLine.setFill(Color.LIGHTGRAY);
        yLine.setFill(Color.LIGHTGRAY);

        List<Line> connections = new ArrayList<>();

        for (int i = 0; i < sensorNodes.length * sensorNodes.length; i++) {
            connections.add(new Line(0, 0, 0, 0));
        }

        list.addAll(xLine, yLine);

        // pool of connections
        ConnectionInstances.setConnectionLines(connections);
        Line line = new Line(0, 0, 40, 40);
        line.setFill(Color.BLACK);
        list.addAll(connections);

        Simulator simulator = new Simulator();
        RadioModel radioModel = new RayleighRadioModel(simulator);

        for (int i = 0; i < sensorNodes.length; i++) {
            sensorNodes[i] = SensorNode.createSensor(simulator, radioModel);
            simulator.addNode(sensorNodes[i]);
        }

        WsnProject self = this;
        timer = new Timer();

        // broadcast messages
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(self::update);
                // run the pending tasks
                runTasks();
            }
        }, 0, 100);

        list.addAll(sensorNodes);

        // create base station
        baseStation = BaseStation.getInstance(simulator, radioModel);
        baseStation.setRadius(10.0);
        baseStation.setFill(Color.BROWN);
        baseStation.setTranslateX(W);
        baseStation.setTranslateY(H);
        list.add(baseStation);

        // local events
        Timer localTimer = new Timer();

        localTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                possibleTransfers();
            }
        }, 0, 10);

        // timer for each node
        Timer nodeTimer = new Timer();
        nodeTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                sharing();
            }
        }, 0, 1);

        Scene scene = new Scene(group);
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.widthProperty().addListener((observable, oldValue, newValue) -> {
            primaryStage.setWidth(newValue.intValue());
            W = newValue.intValue();
            xLine.setEndX(W);
            yLine.setStartX(W / 2);
            yLine.setEndX(W / 2);
            update();
        });

        primaryStage.heightProperty().addListener(((observable, oldValue, newValue) -> {
            primaryStage.setHeight(newValue.intValue());
            H = newValue.intValue();
            yLine.setEndY(H);
            xLine.setStartY(H / 2);
            xLine.setEndY(H /2);
            update();
        }));

        primaryStage.setTitle("WSN Project");
//        primaryStage.getIcons().add(new Image());

        primaryStage.onCloseRequestProperty().addListener(((observable, oldValue, newValue) -> {
            System.exit(0);
        }));
        clusters = new ArrayList<>();
        clusters.add(Cluster.createCluster(new Rectangle(0, 0, W / 2, H / 2), this));
        clusters.add(Cluster.createCluster(new Rectangle(0, H / 2, W / 2, H / 2), this));
        clusters.add(Cluster.createCluster(new Rectangle(W / 2, 0, W / 2, H / 2), this));
        clusters.add(Cluster.createCluster(new Rectangle(W / 2, H / 2, W / 2, H / 2), this));
    }

    public void update() {
        for (int i = 0; i < sensorNodes.length; i++) {
            sensorNodes[i].update();
        }



        groupNodes();
    }

    public void groupNodes() {
        for (Cluster cluster : clusters) {
            cluster.reset();
        }

        for (int i = 0; i < sensorNodes.length; i++) {
            SensorNode sensor = sensorNodes[i];

            int x = (int)sensor.getTranslateX();
            int y = (int)sensor.getTranslateY();

            if (x <= W / 2 && y <= H / 2) {
                clusters.get(0).addChildNode(sensor);
                sensor.setCluster(clusters.get(0));
            } else if (x > W / 2 && y <= H / 2) {
                clusters.get(1).addChildNode(sensor);
                sensor.setCluster(clusters.get(1));
            } else if (x <= W / 2 && y > H / 2) {
                clusters.get(2).addChildNode(sensor);
                sensor.setCluster(clusters.get(2));
            } else if (x > W / 2 && y > H / 2) {
                clusters.get(3).addChildNode(sensor);
                sensor.setCluster(clusters.get(3));
            } else {
                System.out.println("OOPS");
            }
        }

        for (Cluster cluster : clusters) {
            if (cluster.chooseHead()) {

            }
        }
        dead = 0;
        clusters.forEach(cluster -> {
            dead += cluster.getDeadNodeSize();
        });
        Long currentTime = System.currentTimeMillis();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm:ss");
        Date date = new Date(currentTime);
        String time = simpleDateFormat.format(date);
        System.out.println(time + ", " + dead);
    }

    private long dead;

    void possibleTransfers() {
        for (Cluster cluster : clusters) {
            cluster.transferData();
        }
    }

    void sharing() {
        if (nodeShares.isEmpty()) {
            return;
        }
        {
            nodeShares.remove().run();
        };
    }
}
