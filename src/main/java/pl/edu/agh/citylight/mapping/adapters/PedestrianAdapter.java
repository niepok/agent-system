package pl.edu.agh.citylight.mapping.adapters;

import org.jxmapviewer.viewer.GeoPosition;
import pl.edu.agh.citylight.mapping.Map;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static java.awt.event.MouseEvent.BUTTON1;
import static java.awt.event.MouseEvent.BUTTON3;

public class PedestrianAdapter extends MouseAdapter {
    private Map map;
    private GeoPosition startPosition;
    private boolean isClickFirst = true;

    public PedestrianAdapter(Map map) {
        this.map = map;
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        int mouseButton = mouseEvent.getButton();
        Point point = mouseEvent.getPoint();
        GeoPosition position = map.getMapViewer().convertPointToGeoPosition(point);
        if (mouseButton == BUTTON3) {
            map.getNearestPedestrian(position,50).ifPresent(map::removePedestrian);
        } else if (mouseButton == BUTTON1) {
            placePedestrian(position);
        }
        map.repaint();
    }

    private void placePedestrian(GeoPosition position) {
        if (isClickFirst) {
            startPosition = position;
            isClickFirst = false;
        } else {
            map.addPedestrian(startPosition, position);
            isClickFirst = true;
        }
    }
}