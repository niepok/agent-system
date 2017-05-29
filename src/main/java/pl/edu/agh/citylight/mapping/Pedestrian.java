package pl.edu.agh.citylight.mapping;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;

public class Pedestrian extends MobileWaypoint {
    Pedestrian(GeoPosition startPosition, GeoPosition targetPosition, JXMapViewer mapViewer) {
        super(startPosition, targetPosition, mapViewer, 5000);
    }
}
