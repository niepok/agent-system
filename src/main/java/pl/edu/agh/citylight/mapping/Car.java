package pl.edu.agh.citylight.mapping;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;

public class Car extends Waypoint2D {
    private double speed = 10.0;


    public double getSpeed() {
        return speed;
    }

    Car(GeoPosition position, JXMapViewer mapViewer) {
        super(position, mapViewer);
    }
}
