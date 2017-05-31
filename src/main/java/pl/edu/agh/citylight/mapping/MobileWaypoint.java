package pl.edu.agh.citylight.mapping;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import pl.edu.agh.citylight.App;

import java.awt.geom.Point2D;

public class MobileWaypoint extends Waypoint2D {
    private double speed;
    private GeoPosition targetPosition;
    private JXMapViewer mapViewer;
    private Vector2D deltaPosition;

    public double getSpeed() {
        return speed;
    }

    GeoPosition move() {
        setPosition(deltaPosition.translateGeoPosition(getPosition()));
        return getPosition();
    }

    MobileWaypoint(GeoPosition startPosition, GeoPosition targetPosition, JXMapViewer mapViewer, double speed) {
        super(startPosition, mapViewer);
        this.mapViewer = mapViewer;
        this.speed = speed;
        this.targetPosition = targetPosition;
        this.deltaPosition = calculateDelta(startPosition, targetPosition);
    }

    private Vector2D calculateDelta(GeoPosition startPosition, GeoPosition targetPosition) {
        Point2D start = mapViewer.convertGeoPositionToPoint(startPosition);
        Point2D end = mapViewer.convertGeoPositionToPoint(targetPosition);
        double s = start.distance(end);
        double x = end.getX() - start.getX();
        double y = end.getY() - start.getY();
        double dx = (speed * x) / (s * App.framesPerSecond);
        double dy = (speed * y) / (s * App.framesPerSecond);
        return new Vector2D(dx, dy);
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
