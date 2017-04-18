package pl.edu.agh.citylight;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.geom.Point2D;

class StreetLight extends DefaultWaypoint {

    StreetLight(GeoPosition geoPosition){
        super(geoPosition);
    }

    double distance(GeoPosition otherPosition, JXMapViewer mapViewer) {
        Point2D thisPoint = mapViewer.convertGeoPositionToPoint(this.getPosition());
        Point2D otherPoint = mapViewer.convertGeoPositionToPoint(otherPosition);
        return thisPoint.distance(otherPoint);
    }
}