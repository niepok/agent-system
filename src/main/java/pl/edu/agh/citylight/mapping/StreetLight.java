package pl.edu.agh.citylight.mapping;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;

public class StreetLight extends Waypoint2D {
    StreetLight(GeoPosition geoPosition, JXMapViewer mapViewer){
        super(geoPosition, mapViewer);
    }
}