package pl.edu.agh.citylight.mapping;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;

public class Car extends Waypoint2D {
    Car(GeoPosition position, JXMapViewer mapViewer) {
        super(position, mapViewer);
    }
}
