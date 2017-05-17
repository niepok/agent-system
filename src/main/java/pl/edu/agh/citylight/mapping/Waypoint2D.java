package pl.edu.agh.citylight.mapping;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.geom.Point2D;

public class Waypoint2D extends DefaultWaypoint {
    private JXMapViewer mapViewer;

    Waypoint2D(GeoPosition position, JXMapViewer mapViewer) {
        super(position);
        this.mapViewer = mapViewer;
    }

    double distance(GeoPosition otherPosition) {
        Point2D thisPoint = mapViewer.convertGeoPositionToPoint(this.getPosition());
        Point2D otherPoint = mapViewer.convertGeoPositionToPoint(otherPosition);
        return thisPoint.distance(otherPoint);
    }
}
