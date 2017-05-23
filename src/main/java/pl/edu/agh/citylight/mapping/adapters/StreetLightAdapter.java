package pl.edu.agh.citylight.mapping.adapters;

import org.jxmapviewer.viewer.GeoPosition;
import pl.edu.agh.citylight.App;
import pl.edu.agh.citylight.mapping.Map;
import pl.edu.agh.citylight.mapping.StreetLight;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Optional;

import static java.awt.event.MouseEvent.BUTTON1;
import static java.awt.event.MouseEvent.BUTTON3;

public class StreetLightAdapter extends MouseAdapter {
    private Map map;

    public StreetLightAdapter(Map map) {
        this.map = map;
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        int mouseButton = mouseEvent.getButton();
        Point point = mouseEvent.getPoint();
        GeoPosition position = map.getMapViewer().convertPointToGeoPosition(point);
        if (mouseButton == BUTTON1) {
            map.addStreetLight(position,null);
        }
        else if (mouseButton == BUTTON3) {
            Optional<StreetLight> nearestStreetLight = map.getNearestStreetLight(position);
            nearestStreetLight.ifPresent(map::removeStreetLight);
        }
        map.repaint();
    }
}
