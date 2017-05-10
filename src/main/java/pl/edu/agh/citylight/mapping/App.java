package pl.edu.agh.citylight.mapping;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Optional;

import static java.awt.event.MouseEvent.BUTTON1;
import static java.awt.event.MouseEvent.BUTTON3;

public class App {
    JFrame frame;
    JXMapViewer mapViewer;
    Map map;
    
    public static void main(String[] args) {
        GeoPosition krakow = new GeoPosition(50.0625,19.9388);
        Map map = new Map(krakow);
        App window = new App(map);
        
        window.mapViewer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                int mouseButton = mouseEvent.getButton();
                Point point = mouseEvent.getPoint();
                GeoPosition position = window.mapViewer.convertPointToGeoPosition(point);
                if (mouseButton == BUTTON1) {
                    map.addStreetLight(position);
                }
                else if (mouseButton == BUTTON3) {
                    Optional<StreetLight> nearestStreetLight = map.getNearestWaypoint(position);
                    nearestStreetLight.ifPresent(map::removeStreetLight);
                }
                map.repaint();
            }
        });
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
