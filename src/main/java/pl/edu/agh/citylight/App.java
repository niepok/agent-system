package pl.edu.agh.citylight;

import jade.core.AID;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import pl.edu.agh.citylight.agents.LampAgentInit;
import pl.edu.agh.citylight.mapping.Map;
import pl.edu.agh.citylight.mapping.adapters.CarAdapter;
import pl.edu.agh.citylight.mapping.adapters.EchoAdapter;
import pl.edu.agh.citylight.mapping.adapters.StreetLightAdapter;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

import static java.awt.event.MouseEvent.BUTTON1;
import static java.awt.event.MouseEvent.BUTTON3;

public class App {
    private JXMapViewer mapViewer;
    private JButton startButton;
    private JButton switchButton;
    private Map map;



    //intersection simulation parameters
    private static GeoPosition defaultPosition = new GeoPosition(50.03458,20.01169);

    //listeners for adding and removing objects
    private static StreetLightAdapter streetLightAdapter;
    private static CarAdapter carAdapter;

    public static final double LAMPRANGE = 55.5;

    //speed parameters
    private static double timerPeriod = 25.0;
    private static double latInc = 0.00001;
    private static double lonInc = 0.00001;


    public static void main(String[] args) {
        Map map = new Map(defaultPosition, 3);
        App window = new App(map);
        Profile p = new ProfileImpl(true);
        ContainerController cc = jade.core.Runtime.instance().createMainContainer(p);
        AgentController ac;
        Object[] argss = {p,cc,map};
        try{
            ac = cc.createNewAgent("lampinit", "pl.edu.agh.citylight.agents.LampAgentInit", argss);
            ac.start();
        }catch (StaleProxyException e) {
            e.printStackTrace();
        }

        window.addListeners();
    }

    App(Map map) {
        this.map = map;
        mapViewer = map.getMapViewer();
        setUpWindow();
    }

    private void setUpWindow() {
        JFrame frame = new JFrame("CityLight");
        frame.setLayout(new BorderLayout());
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());
        startButton = new JButton("Start");
        switchButton = new JButton("Samochody");
        controlPanel.add(startButton);
        controlPanel.add(switchButton);
        frame.add(mapViewer);
        frame.add(controlPanel, BorderLayout.SOUTH);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private void addListeners() {
        streetLightAdapter = new StreetLightAdapter(map);
        carAdapter = new CarAdapter(map);
        mapViewer.addMouseListener(new EchoAdapter(map));
        mapViewer.addMouseListener(streetLightAdapter);
        startButton.addActionListener(actionEvent -> startSimulation());
        switchButton.setActionCommand("cars");
        switchButton.addActionListener(this::switchAdapters);
    }

    private void startSimulation() {
        Timer timer = new Timer((int) timerPeriod, actionEvent -> {
            map.moveCars();
            map.repaint();
        });
        timer.start();
    }

    private void switchAdapters(ActionEvent e) {
        JButton sourceButton = (JButton) e.getSource();
        if (e.getActionCommand().equals("cars")) {
            mapViewer.removeMouseListener(streetLightAdapter);
            mapViewer.addMouseListener(carAdapter);
            sourceButton.setText("Latarnie");
            sourceButton.setActionCommand("streetLights");
        } else {
            mapViewer.removeMouseListener(carAdapter);
            mapViewer.addMouseListener(streetLightAdapter);
            sourceButton.setText("Samochody");
            sourceButton.setActionCommand("cars");
        }
    }
}
