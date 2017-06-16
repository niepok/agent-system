package pl.edu.agh.citylight.mapping;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.WaypointRenderer;
import pl.edu.agh.citylight.mapping.waypoints.StreetLight;

import java.awt.*;
import java.awt.geom.Point2D;

public class StreetLightRenderer implements WaypointRenderer<StreetLight>{
    private static final int LAMP_WIDTH = 15;
    private static final int LAMP_HEIGHT = 15;
    @Override
    public void paintWaypoint(Graphics2D graphics2D, JXMapViewer mapViewer, StreetLight streetLight) {
        Point2D point = mapViewer.getTileFactory().geoToPixel(streetLight.getPosition(), mapViewer.getZoom());
        int x = (int)point.getX() - LAMP_WIDTH / 2;
        int y = (int)point.getY() - LAMP_HEIGHT / 2;
        Color color;
        switch (streetLight.getLightIntensity()) {
            default:
            case OFF:
                color = Color.WHITE;
                break;
            case LOW:
                color = Color.GREEN;
                break;
            case MEDIUM:
                color = Color.YELLOW;
                break;
            case HIGH:
                color = Color.RED;
                break;
        }
        graphics2D.setColor(color);
        graphics2D.fillOval(x, y, LAMP_WIDTH, LAMP_HEIGHT);
        graphics2D.setColor(Color.BLACK);
        graphics2D.drawOval(x, y, LAMP_WIDTH, LAMP_HEIGHT);
    }
}
