package pl.edu.agh.citylight.mapping;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.WaypointRenderer;
import pl.edu.agh.citylight.mapping.waypoints.Waypoint2D;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

public class ImageRenderer implements WaypointRenderer<Waypoint2D>{
    private BufferedImage image = null;

    ImageRenderer(String imageName) {
        try {
            this.image = ImageIO.read(getClass().getResource(imageName));
        } catch (Exception e) {
            System.err.println("Resource " + imageName + " not found");
        }

    }
    @Override
    public void paintWaypoint(Graphics2D graphics2D, JXMapViewer mapViewer, Waypoint2D waypoint) {
        if(this.image != null) {
            Point2D point = mapViewer.getTileFactory().geoToPixel(waypoint.getPosition(), mapViewer.getZoom());
            int x = (int)point.getX() - this.image.getWidth() / 2;
            int y = (int)point.getY() - this.image.getHeight() / 2;
            graphics2D.drawImage(this.image, x, y, null);
        }
    }
}
