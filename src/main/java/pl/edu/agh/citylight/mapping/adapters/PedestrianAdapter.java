package pl.edu.agh.citylight.mapping.adapters;

import org.jxmapviewer.viewer.GeoPosition;
import pl.edu.agh.citylight.mapping.Map;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;

import static java.awt.event.MouseEvent.BUTTON1;
import static java.awt.event.MouseEvent.BUTTON3;

public class PedestrianAdapter extends MouseAdapter {
    private Map map;
    private java.util.List<GeoPosition> path = new LinkedList<>();
    private boolean creatingPath = false;

    public PedestrianAdapter(Map map) {
        this.map = map;
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        int mouseButton = mouseEvent.getButton();
        Point point = mouseEvent.getPoint();
        GeoPosition position = map.getMapViewer().convertPointToGeoPosition(point);
        if (mouseButton == BUTTON3) {
            if (creatingPath) {
                map.addPedestrian(path);
                path = new LinkedList<>();
                creatingPath = false;
            } else {
                map.getNearestPedestrian(position,50).ifPresent(map::removePedestrian);
            }
        } else if (mouseButton == BUTTON1) {
            creatingPath = true;
            path.add(position);
        }
        map.repaint();
    }
}
