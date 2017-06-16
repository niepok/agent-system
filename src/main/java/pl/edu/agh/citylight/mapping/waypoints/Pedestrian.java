package pl.edu.agh.citylight.mapping.waypoints;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;

import java.util.List;

public class Pedestrian extends MobileWaypoint {
    public Pedestrian(List<GeoPosition> path, JXMapViewer mapViewer) {
        super(path, mapViewer, 2.5);
    }
}
