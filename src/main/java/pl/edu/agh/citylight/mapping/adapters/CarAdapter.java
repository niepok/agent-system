package pl.edu.agh.citylight.mapping.adapters;

import org.jxmapviewer.viewer.GeoPosition;
import pl.edu.agh.citylight.mapping.Map;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static java.awt.event.MouseEvent.BUTTON1;

public class CarAdapter extends MouseAdapter {
    private Map map;
    private GeoPosition destination;

    public CarAdapter(Map map, GeoPosition destination) {
        this.map = map;
        this.destination = destination;
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        int mouseButton = mouseEvent.getButton();
        Point point = mouseEvent.getPoint();
        GeoPosition position = map.getMapViewer().convertPointToGeoPosition(point);
        if (mouseButton == BUTTON1) {
            map.addCar(position, destination);
        }
        map.repaint();
    }
}
