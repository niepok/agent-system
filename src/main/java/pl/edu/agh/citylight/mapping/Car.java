package pl.edu.agh.citylight.mapping;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import pl.edu.agh.citylight.agents.CarAgent;

public class Car extends Waypoint2D {

    private double speed = 10.0;

    private CarAgent agent;

    public CarAgent getAgent() {
        return agent;
    }

    public void setAgent(CarAgent agent) {
        this.agent = agent;
    }

    Car(GeoPosition position, JXMapViewer mapViewer) {
        super(position, mapViewer);
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }
}
