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

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
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
    private JButton button;
    private Map map;



    //intersection simulation parameters
    private static GeoPosition car1StartPos = new GeoPosition(50.032651998280635, 20.011188983917236);
    private static GeoPosition car1EndPos = new GeoPosition(50.036194190130736, 20.011789798736572);
    private static GeoPosition car2StartPos = new GeoPosition(50.036021910580374, 20.007508993148804);
    private static GeoPosition car2EndPos = new GeoPosition(50.03358925734411, 20.01459002494812);
    private static GeoPosition defaultPosition = new GeoPosition(50.03458,20.01169);


    public static final double LAMPRANGE = 60.0;

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
        button = new JButton("Start");
        frame.add(button, BorderLayout.SOUTH);
        frame.add(mapViewer);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private void addListeners() {
        mapViewer.addMouseListener(new EchoAdapter(map));
        mapViewer.addMouseListener(new CarAdapter(map, defaultPosition));
        button.addActionListener(actionEvent -> startSimulation());
    }

    private void startSimulation() {
        Timer timer = new Timer((int) timerPeriod, actionEvent -> {
            map.moveCars();
            map.repaint();
        });
        timer.start();
    }
}
