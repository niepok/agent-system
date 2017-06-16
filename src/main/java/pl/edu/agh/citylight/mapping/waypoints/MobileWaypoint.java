package pl.edu.agh.citylight.mapping.waypoints;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import pl.edu.agh.citylight.App;

import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.List;

abstract class MobileWaypoint extends Waypoint2D {
    private double speed;
    private List<GeoPosition> path;
    private Iterator<GeoPosition> positionIterator;
    private GeoPosition segmentEnd;

    private Vector2D deltaPosition;

    private static final double THRESHOLD = 0.00005;

    public double getSpeed() {
        return speed;
    }

    public List<GeoPosition> getPath() {
        return path;
    }

    public void move() {
        GeoPosition currentPosition = getPosition();
        setPosition(deltaPosition.translateGeoPosition(currentPosition));
        if (equals(currentPosition, segmentEnd)) {
            if (positionIterator.hasNext()) {
                segmentEnd = positionIterator.next();
                deltaPosition = calculateDelta(currentPosition, segmentEnd);
            } else {
                //TODO
            }
        }
    }

    MobileWaypoint(List<GeoPosition> path, JXMapViewer mapViewer, double speed) {
        super(path.get(0), mapViewer);
        this.speed = speed;
        this.path = path;
        positionIterator = path.iterator();
        GeoPosition pathStart = positionIterator.next();
        segmentEnd = positionIterator.next();
        this.deltaPosition = calculateDelta(pathStart, segmentEnd);
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

    private boolean equals(GeoPosition first, GeoPosition second) {
        return equals(first.getLatitude(), second.getLatitude()) &&
                equals(first.getLongitude(), second.getLongitude());
    }

    private boolean equals(double first, double second) {
        return Math.abs(first - second) < THRESHOLD;
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

        private Point2D translate(Point2D point) {
            point.setLocation(point.getX() + x, point.getY() + y);
            return point;
        }
    }
}
