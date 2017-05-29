package pl.edu.agh.citylight.mapping;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.geom.Point2D;

public class Car extends MobileWaypoint {
    Car(GeoPosition startPosition, GeoPosition targetPosition, JXMapViewer mapViewer) {
        super(startPosition, targetPosition, mapViewer, 1000);
    }
}
