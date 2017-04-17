package pl.edu.agh.citylight;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.jxmapviewer.viewer.Waypoint;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.util.HashSet;

public class App {
    JFrame frame;
    JXMapViewer mapViewer;
    Map map;
    public static void main(String[] args) {
        GeoPosition krakow = new GeoPosition(50.0625,19.9388);
        Map map = new Map(krakow);
        App window = new App(map);
    }

    App(Map map) {
        this.map = map;
        mapViewer = map.getMapViewer();
        setUpWindow();
    }

    private void setUpWindow(){
        frame = new JFrame("CityLight");
        frame.getContentPane().add(mapViewer);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

}
