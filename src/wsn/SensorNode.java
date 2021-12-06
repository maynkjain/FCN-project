package wsn;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import net.tinyos.prowler.Application;
import net.tinyos.prowler.Node;
import net.tinyos.prowler.RadioModel;
import net.tinyos.prowler.Simulator;

import java.util.Timer;

public class SensorNode extends Node {
    private Timer timer = new Timer();
    private int id;
    private int timesHead;
    private double currX = 0;
    private double currY = 0;
    private CircleMoveTransition circleMoveTransition;

    private boolean head = false;
    private boolean wasHead = false;
    private Long mobility = 0l;
    private Cluster cluster;
    private double battery = 100.0;

    private static int idCounter = 0;

    private SensorNode(int id, Simulator simulator, RadioModel radioModel) {
        super(simulator, radioModel);

        // install a simple application to the sensor node
        addApplication(new SimpleApplication(this));
        setTranslateX(Math.random() * WsnProject.W);
        setTranslateY(Math.random() * WsnProject.H);

        setPosition(getTranslateX(), getTranslateY(), 0);

        simulator.addNode(this);
        this.setRadius(4);
        setFill(Color.BLACK);
        this.id = id;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
        this.battery -= Math.random() * 1;
        //this.battery -= 20;
    }

    public static SensorNode createSensor(Simulator simulator, RadioModel radioModel) throws Exception {
        return new SensorNode(idCounter++, simulator, radioModel);
    }

    public void update() {
        if (battery <= 0) {
            setFill(Color.LIGHTGRAY);
            return;
        }
        currX = (Math.random() * 5);
        currY = (Math.random() * 5);

        if (Math.random() - 0.5 > 0) {
            currX = -currX;
        }

        if (Math.random() - 0.5 > 0) {
            currY = -currY;
        }

        mobility += (long)Math.sqrt(Math.pow(currX, 2) + Math.pow(currY, 2));
        double newPositionX = getTranslateX() + currX;
        double newPositionY = getTranslateY() + currY;

        if (newPositionX > WsnProject.W || newPositionX < 0) {
            newPositionX = getTranslateX() - currX;
        }

        if (newPositionY > WsnProject.H || newPositionY < 0) {
            newPositionY = getTranslateY() - currY;
        }

        setTranslateX(newPositionX);
        setTranslateY(newPositionY);
        setPosition(newPositionX, newPositionY, 0);
    }

    public boolean isDead() {
        return battery <= 1;
    }

    public void markAsHead(Cluster cluster) {
        timesHead++;
        this.head = true;
        this.cluster = cluster;
        this.battery -= Math.random() * 5;
        setFill(Color.CORAL);
    }

    public void markNotHead() {

        this.head = false;
        this.wasHead = true;
        this.setRadius(4.0);
    }

    public boolean isWasHead() {
        return wasHead;
    }

    private int compareAll(SensorNode other) {
        if (battery <= 1 && other.battery > 1) {
            return -1;
        } else if (other.battery <= 1 && this.battery > 1) {
            return 1;
        } else if (battery <= 1 && other.battery <= 1) {
            return 0;
        }

        if (battery < other.battery) {
            return -1;
        } else if (battery > other.battery){
            return 1;
        }

        if (mobility > other.mobility) {
            return -1;
        } else if (mobility < other.mobility) {
            return 1;
        }


        if (this.timesHead > 500) {
            return -1;
        } else if (other.timesHead > 500) {
            return 1;
        } else if (this.wasHead || this.timesHead > other.timesHead) {
            return -1;
        } else if (other.wasHead || other.timesHead > this.timesHead) {
            return 1;
        } else {
            return 0;
        }
    }

    public int compare(SensorNode other) {
        return compareBattery(other);
    }

    private int compareBattery(SensorNode other) {
        return Double.compare(battery, other.battery);
    }

    private int compareTimesHead(SensorNode other) {
        return -Integer.compare(other.timesHead, this.timesHead);
    }

    private int compareToMobility(SensorNode other) {
        if (mobility > other.mobility) {
            return -1;
        } else if (mobility < other.mobility) {
            return 1;
        } else {
            return 0;
        }
    }

    public boolean isHead() {
        return head;
    }

    public void setWasHead(boolean wasHead) {
        this.wasHead = wasHead;
    }

    @Override
    public String toString() {
        return "SensorNode{id=" + id + ", timesHead=" + timesHead + "}";
    }

    @Override
    protected void receptionBegin(double strength, Object stream) {
        getApplication(SensorNode.class).sendMessage(stream);
    }

    @Override
    protected void receptionEnd(double strength, Object stream) {
        getApplication(SensorNode.class).sendMessageDone();
    }

    @Override
    public boolean sendMessage(Object message, Application app) {
        // forward the message to the base station
        try {
            ConnectionLine connectionLine = ConnectionInstances.getFreeConnectionLine();

            int counter = 2;
            while (connectionLine == null && counter-- > 0) {
                Log.i("Network is busy.");
                connectionLine = ConnectionInstances.getFreeConnectionLine();
                try {
                    wait(100);
                } catch (Exception e) {
                    Log.i("Error while waiting for connection.");
                }
            }

            if (connectionLine == null) {
                // we will try in next broadcast
                return false;
            }
            final ConnectionLine line = connectionLine;
            line.createConnection(this, (SensorNode) app.getNode());
            app.receiveMessage(message, this);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
