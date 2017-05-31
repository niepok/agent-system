package pl.edu.agh.citylight.mapping;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.geom.Point2D;
import java.util.List;

public class Car extends MobileWaypoint {
    Car(List<GeoPosition> path, JXMapViewer mapViewer) {
        super(path, mapViewer, 100);
    }
}
