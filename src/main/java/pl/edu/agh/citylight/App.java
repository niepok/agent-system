package pl.edu.agh.citylight;

import jade.core.AID;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import pl.edu.agh.citylight.mapping.Car;
import pl.edu.agh.citylight.mapping.Map;
import pl.edu.agh.citylight.mapping.StreetLight;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Vector;

import static java.awt.event.MouseEvent.BUTTON1;
import static java.awt.event.MouseEvent.BUTTON3;

public class App {
    JFrame frame;
    JXMapViewer mapViewer;
    Map map;
    private static Vector lampMasters = new Vector();

    private JXMapViewer mapViewer;
    private JButton button;
    private Map map;
    private Timer timer;
    private Car car;

    public static void main(String[] args) {
        GeoPosition defaultPosition = new GeoPosition(50.0625,19.9388); //Krakow
        Map map = new Map(defaultPosition);
        App window = new App(map);
        window.addListeners();
        window.timer = new Timer(25, actionEvent -> {
            double lat = window.car.getPosition().getLatitude() + 0.000005;
            double lon = window.car.getPosition().getLongitude() + 0.000005;
            window.car.setPosition(new GeoPosition(lat, lon));
            map.repaint();
        });
    }

    App(Map map) {
        this.map = map;
        mapViewer = map.getMapViewer();
        setUpWindow();
    }

    private void setUpWindow() {
        JFrame frame = new JFrame("CityLight");
        frame.setLayout(new BorderLayout());
        button = new JButton("Start");
        frame.add(button, BorderLayout.SOUTH);
        frame.add(mapViewer);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private void addListeners() {
        mapViewer.addMouseListener(new MouseAdapter() {


        Profile p = new ProfileImpl(true);
        ContainerController cc = jade.core.Runtime.instance().createMainContainer(p);
        Object[] argss = {1000,1,5,map};
        AgentController ac;
            try {
                ac = cc.createNewAgent("master1", "agents.LampMasterAgent", argss);
                ac.start();
                lampMasters.add(new AID("master1", AID.ISLOCALNAME));
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }

        window.mapViewer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                int mouseButton = mouseEvent.getButton();
                Point point = mouseEvent.getPoint();
                GeoPosition position = mapViewer.convertPointToGeoPosition(point);
                if (mouseButton == BUTTON1) {
                    map.addStreetLight(position);
                }
                else if (mouseButton == BUTTON3) {
                    Optional<StreetLight> nearestStreetLight = map.getNearestStreetLight(position);
                    nearestStreetLight.ifPresent(map::removeStreetLight);
                }
                map.repaint();
            }
        });

        button.addActionListener(actionEvent -> {
            car = map.addCar(new GeoPosition(50.0625,19.9388));
            timer.start();
        });
    }
}
