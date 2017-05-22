package pl.edu.agh.citylight.mapping.adapters;

import org.jxmapviewer.viewer.GeoPosition;
import pl.edu.agh.citylight.App;
import pl.edu.agh.citylight.mapping.Map;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

public class EchoAdapter extends MouseAdapter {
    private Map map;

    public EchoAdapter(Map map) {
        this.map = map;
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        GeoPosition position = map.getMapViewer().convertPointToGeoPosition(mouseEvent.getPoint());
        Point2D point = map.getMapViewer().convertGeoPositionToPoint(position);
        System.out.println("GeoPos: " + position.getLatitude() + " " + position.getLongitude());
        System.out.println("Point: " + point.getX() + " " + point.getY());
    }
}
