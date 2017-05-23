package pl.edu.agh.citylight.mapping.adapters;

import org.jxmapviewer.viewer.GeoPosition;
import pl.edu.agh.citylight.mapping.Car;
import pl.edu.agh.citylight.mapping.Map;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Optional;

import static java.awt.event.MouseEvent.BUTTON1;
import static java.awt.event.MouseEvent.BUTTON3;

public class CarAdapter extends MouseAdapter {
    private Map map;
    private GeoPosition startPosition;
    private boolean isClickFirst = true;

    public CarAdapter(Map map) {
        this.map = map;
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        int mouseButton = mouseEvent.getButton();
        Point point = mouseEvent.getPoint();
        GeoPosition position = map.getMapViewer().convertPointToGeoPosition(point);
        if (mouseButton == BUTTON3) {
            map.getNearestCar(position,50).ifPresent(map::removeCar);
        } else if (mouseButton == BUTTON1) {
            placeCar(position);
        }
        map.repaint();
    }

    private void placeCar(GeoPosition position) {
        if (isClickFirst) {
            startPosition = position;
            isClickFirst = false;
        } else {
            map.addCar(startPosition, position);
            isClickFirst = true;
        }
    }

}
