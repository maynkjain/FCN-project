package wsn;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class Cluster {
    public static final int DEFAULT_SIZE = 1;

    static Color[] colors = new Color[]{
        Color.GREEN, Color.BLUE, Color.LIGHTPINK, Color.BLACK
    };
    List<SensorNode> cluster;
    SensorNode clusterHead;
    int id = 0;
    Rectangle dimensions;
    private WsnProject parent;

    // current connection;
    ConnectionLine connectionLine;

    private static int idCounter = 0;

    private Cluster(List<SensorNode> cluster, SensorNode clusterHead) {
        this.cluster = cluster;
        this.clusterHead = clusterHead;
    }

    private Cluster(int id, Rectangle dimensions, WsnProject parent) {
        this.cluster = new ArrayList<>();
        this.clusterHead = null;
        this.id = id;
        this.dimensions = dimensions;
        this.parent = parent;
    }

    public static Cluster createCluster(Rectangle dimensions, WsnProject parent) {
        return new Cluster(idCounter++, dimensions, parent);
    }

    public void addChildNode(SensorNode sensorNode) {
        this.cluster.add(sensorNode);
    }

    public List<SensorNode> getCluster() {
        return cluster;
    }

    public SensorNode getClusterHead() {
        return clusterHead;
    }

    public void reset() {
        if (clusterHead != null) {
            clusterHead.markNotHead();
            clusterHead.setRadius(4.0);
        }
        cluster.clear();
        clusterHead = null;

    }

    public void setCluster(List<SensorNode> cluster) {
        this.cluster = cluster;
    }

    public void setClusterHead(SensorNode clusterHead) {
        this.clusterHead = clusterHead;
    }

    boolean shouldUpdate(SensorNode sensorNode) {
        return (sensorNode.getTranslateX() > (dimensions.getX() + dimensions.getWidth()))
                || (sensorNode.getTranslateX() < dimensions.getX())
                || (sensorNode.getTranslateY() > (dimensions.getY() + dimensions.getHeight()))
                || (sensorNode.getTranslateY() < dimensions.getY());
    }

    void update() {
        Platform.runLater(() -> parent.update());
    }

    private SensorNode selectHead() {
        // if the previous head is already in this cluster
        if (cluster.stream().filter(SensorNode::isDead).toArray().length == cluster.size())
            return null;

        Comparator<SensorNode> comparator = new Comparator<SensorNode>() {
            @Override
            public int compare(SensorNode o1, SensorNode o2) {
                return o2.compare(o1);
            }
        };
        PriorityQueue<SensorNode> sensorNodes = new PriorityQueue<>(comparator);
        sensorNodes.addAll(cluster);

        return sensorNodes.remove();
    }

    public long getDeadNodeSize() {
        return cluster.stream().filter(SensorNode::isDead).count();
    }

    public boolean chooseHead() {
        cluster.forEach(sensorNode -> {
            if (!sensorNode.isDead())
                sensorNode.setFill(colors[id]);
        });

        clusterHead = selectHead();
        //clusterHead = cluster.get((int)Math.random() * cluster.size());

        if (clusterHead == null) {
            return false;
        }

        clusterHead.setRadius(7);
        cluster.forEach(sensorNode -> { sensorNode.setWasHead(false); });
        clusterHead.markAsHead(this);
        return true;
    }

    public void transferData() {
        cluster.forEach(sensorNode -> {
            WsnProject.addShare(() -> {
                if (sensorNode.isHead()) {
                    return;
                }

                ConnectionInstances.getConnectionLines().forEach(ConnectionLine::breakConnection);
                sensorNode.sendMessage(Packet.newInstance(DEFAULT_SIZE), clusterHead.getApplication(SimpleApplication.class));
                // forward to the base station

                SimpleApplication application = (SimpleApplication) clusterHead.getApplication(SimpleApplication.class);
                while (!application.messages.isEmpty()) {
                    clusterHead.sendMessage(application.messages.remove(), parent.baseStation.getApplication(BaseStationApplication.class));
                }
            });
        });
    }

    public WsnProject getParent() {
        return parent;
    }

    @Override
    public String toString() {
        return "Cluster{ " + cluster.size() + " nodes, head: " + clusterHead + "}";
    }
}
