package pl.edu.agh.citylight;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import jade.core.AID;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import pl.edu.agh.citylight.agents.LampAgentInit;
import pl.edu.agh.citylight.mapping.Car;
import pl.edu.agh.citylight.mapping.Map;
import pl.edu.agh.citylight.mapping.Pedestrian;
import pl.edu.agh.citylight.mapping.adapters.*;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalTime;
import java.util.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static java.awt.event.MouseEvent.BUTTON1;
import static java.awt.event.MouseEvent.BUTTON3;

public class App {
    private JXMapViewer mapViewer;
    private JButton startButton;
    private JButton saveButton;
    private JButton loadButton;
    private JComboBox<String> comboBox;
    private Map map;
    private final JFileChooser fc = new JFileChooser();

    //intersection simulation parameters
    static GeoPosition defaultPosition = new GeoPosition(50.03458,20.01169);

    //listeners for adding and removing objects
    private static StreetLightAdapter streetLightAdapter;
    private static CarAdapter carAdapter;
    private static PedestrianAdapter pedestrianAdapter;
    private static MouseAdapter currentAdapter;


    public static final double LAMPRANGE = 42.5;

    //speed parameters
    public static double framesPerSecond = 60;


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

    @SuppressWarnings("unchecked")
    private void setUpWindow() {
        JFrame frame = new JFrame("CityLight");
        frame.setLayout(new BorderLayout());
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());

        String options[] = {"Lampy", "Samochody", "Piesi"};
        startButton = new JButton("Start");
        saveButton = new JButton("Zapisz");
        loadButton = new JButton("Wczytaj");
        comboBox = new JComboBox(options);
        comboBox.setSelectedIndex(0);
        controlPanel.add(startButton);
        controlPanel.add(saveButton);
        controlPanel.add(loadButton);
        controlPanel.add(comboBox);
        frame.add(mapViewer);
        frame.add(controlPanel, BorderLayout.SOUTH);
        frame.setSize(1280, 720);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private void addListeners() {
        streetLightAdapter = new StreetLightAdapter(map);
        carAdapter = new CarAdapter(map);
        pedestrianAdapter = new PedestrianAdapter(map);
        //mapViewer.addMouseListener(new EchoAdapter(map));
        mapViewer.addMouseListener(streetLightAdapter);
        currentAdapter = streetLightAdapter;
        startButton.addActionListener(actionEvent -> startSimulation());
        saveButton.addActionListener(this::save);
        loadButton.addActionListener(this::load);
        comboBox.addActionListener(this::switchAdapters);
    }

    private void startSimulation() {
        int timerPeriod = (int) (framesPerSecond / 1000);
        Timer timer = new Timer(timerPeriod, actionEvent -> {
            map.moveCars();
            map.movePedestrians();
            map.repaint();
        });
        timer.start();
    }

    private void switchAdapters(ActionEvent e) {
        JComboBox comboBox = (JComboBox) e.getSource();
        mapViewer.removeMouseListener(currentAdapter);
        switch ((String)comboBox.getSelectedItem()) {
            case "Lampy":
                currentAdapter = streetLightAdapter;
                break;
            case "Samochody":
                currentAdapter = carAdapter;
                break;
            case "Piesi":
                currentAdapter = pedestrianAdapter;
                break;
        }
        mapViewer.addMouseListener(currentAdapter);
    }

    private void save(ActionEvent e) {
        int returnVal = fc.showSaveDialog((Component) e.getSource());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            try (CSVWriter writer = new CSVWriter(new FileWriter(file), '\t')){
                List<String[]> entries = carsToEntryList(map.getCars());
                writer.writeAll(entries);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private List<String[]> carsToEntryList (Set<Car> cars) {
        return cars.stream().
                map(this::carToEntry).
                collect(Collectors.toList());
    }

    private String[] carToEntry (Car car) {
        return car.getPath().stream().
                map(GeoPosition::toString).
                toArray(String[]::new);
    }

    private void load(ActionEvent e) {
        int returnVal = fc.showOpenDialog((Component) e.getSource());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            try (CSVReader reader = new CSVReader(new FileReader(file), '\t')){
                List<String[]> entries = reader.readAll();
                entries.stream().
                        map(this::pathFromEntry).
                        forEach(map::addCar);
                map.repaint();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private List<GeoPosition> pathFromEntry(String[] entry) {
        return Arrays.stream(entry).
                map(this::positionFromString).
                collect(Collectors.toList());
    }

    private GeoPosition positionFromString(String coords) {
        return new GeoPosition(Arrays.stream(coords.split(",")).
                map(s -> s.replaceAll("[^\\d.]", "")).
                mapToDouble(Double::parseDouble).
                toArray());
    }


}
