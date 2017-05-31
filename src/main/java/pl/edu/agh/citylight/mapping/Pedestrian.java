package pl.edu.agh.citylight.mapping;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;

import java.util.List;

public class Pedestrian extends MobileWaypoint {
    Pedestrian(List<GeoPosition> path, JXMapViewer mapViewer) {
        super(path, mapViewer, 25);
    }
}
