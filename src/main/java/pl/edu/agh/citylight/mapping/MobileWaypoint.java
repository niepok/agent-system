package pl.edu.agh.citylight.mapping;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.geom.Point2D;

public class MobileWaypoint extends Waypoint2D {
    private double speed = 5.0;
    private GeoPosition targetPosition;
    private JXMapViewer mapViewer;
    private Vector2D deltaPosition;
    private double divider;

    public void setDivider(double divider) {
        this.divider = divider;
    }

    public double getSpeed() {
        return speed;
    }

    GeoPosition move() {
        setPosition(deltaPosition.translateGeoPosition(getPosition()));
        return getPosition();
    }

    MobileWaypoint(GeoPosition startPosition, GeoPosition targetPosition, JXMapViewer mapViewer) {
        this(startPosition, mapViewer);
        this.targetPosition = targetPosition;
        this.deltaPosition = calculateDelta(startPosition, targetPosition);
    }

    private MobileWaypoint(GeoPosition startPosition, JXMapViewer mapViewer) {
        super(startPosition, mapViewer);
        this.mapViewer = mapViewer;
    }

    private Vector2D calculateDelta(GeoPosition startPosition, GeoPosition targetPosition) {
        Point2D start = mapViewer.convertGeoPositionToPoint(startPosition);
        Point2D end = mapViewer.convertGeoPositionToPoint(targetPosition);
        double x = (end.getX() - start.getX()) / divider;
        double y = (end.getY() - start.getY()) / divider;
        return new Vector2D(x, y);
    }

    private class Vector2D {
        double x;
        double y;

        Vector2D(double x, double y) {
            this.x = x;
            this.y = y;
        }

        GeoPosition translateGeoPosition(GeoPosition geoPosition) {
            Point2D point = mapViewer.convertGeoPositionToPoint(geoPosition);
            return mapViewer.convertPointToGeoPosition(translate(point));
        }

        Point2D translate(Point2D point) {
            point.setLocation(point.getX() + x, point.getY() + y);
            return point;
        }
    }
}
