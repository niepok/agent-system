package pl.edu.agh.citylight.mapping;

import jade.core.AID;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.VirtualEarthTileFactoryInfo;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.viewer.*;
import pl.edu.agh.citylight.mapping.waypoints.Car;
import pl.edu.agh.citylight.mapping.waypoints.Pedestrian;
import pl.edu.agh.citylight.mapping.waypoints.StreetLight;
import pl.edu.agh.citylight.mapping.waypoints.Waypoint2D;

import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Map {
    private JXMapViewer mapViewer = new JXMapViewer();
    private Set<StreetLight> streetLights = new HashSet<>();
    private Set<Car> cars = new HashSet<>();
    private Set<Pedestrian> pedestrians = new HashSet<>();
    private WaypointPainter<StreetLight> streetLightPainter = new WaypointPainter<>();
    private WaypointPainter<Waypoint2D> carPainter = new WaypointPainter<>();
    private WaypointPainter<Waypoint2D> pedestrianPainter = new WaypointPainter<>();
    @SuppressWarnings("unchecked")
    private CompoundPainter<JXMapViewer> painters = new CompoundPainter<>(streetLightPainter, carPainter, pedestrianPainter);

    public JXMapViewer getMapViewer() {
        return mapViewer;
    }

    public Set<Car> getCars() {
        return cars;
    }

    public void setCars(Set<Car> cars) {
        this.cars = cars;
    }

    public Map(GeoPosition centerPosition, int zoom) {
	    mapViewer.setTileFactory(createTileFactory());
	    mapViewer.setCenterPosition(centerPosition);
        mapViewer.setZoom(zoom);
        addListeners();
        carPainter.setRenderer(new ImageRenderer("/car.png"));
        pedestrianPainter.setRenderer(new ImageRenderer("/pedestrian.png"));
        streetLightPainter.setRenderer(new StreetLightRenderer());
	}

    private TileFactory createTileFactory() {
        TileFactoryInfo info = new VirtualEarthTileFactoryInfo(VirtualEarthTileFactoryInfo.MAP);
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        tileFactory.setThreadPoolSize(8);
        return tileFactory;
    }

    private void addListeners(){
        MouseInputListener mia = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(mia);
        mapViewer.addMouseMotionListener(mia);
        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));
        mapViewer.addKeyListener(new PanKeyListener(mapViewer));
    }

    public StreetLight addStreetLight(GeoPosition position, AID agent) {
        StreetLight streetLight = new StreetLight(position, mapViewer, agent);
        streetLights.add(streetLight);
        return streetLight;
    }
    public void removeStreetLight(StreetLight streetLight) {
        streetLights.remove(streetLight);
    }
    public Optional<StreetLight> getNearestStreetLight(GeoPosition position) {
        return streetLights.stream().
                min(Comparator.comparing(i -> i.distance(position)));
    }

    public Set<StreetLight> getStreetLights(GeoPosition position, double radius) {
        return streetLights.stream().
                filter(i -> (i.distance(position) <= radius)).
                collect(Collectors.toSet());
    }

    public Car addCar(List<GeoPosition> path) {
        Car car = new Car(path, mapViewer);
        cars.add(car);
        return car;
    }
    public void removeCar(Car car) {
        cars.remove(car);
    }
    public void moveCars() {
        cars.forEach(Car::move);
    }
    public Optional<Car> getNearestCar(GeoPosition position, double maxDistance) {
        Optional<Car> car = getNearestCar(position);
        if(car.isPresent() && car.get().distance(position) > maxDistance) {
            return Optional.empty();
        }
        return car;
    }
    public Set<Car> getNearestCars(GeoPosition position, double radius) {
        return cars.stream().
                filter(i -> (i.distance(position) <= radius)).
                collect(Collectors.toSet());
    }

    public Pedestrian addPedestrian(List<GeoPosition> path) {
        Pedestrian pedestrian = new Pedestrian(path, mapViewer);
        pedestrians.add(pedestrian);
        return pedestrian;
    }
    public void removePedestrian(Pedestrian pedestrian) {
        pedestrians.remove(pedestrian);
    }
    public void movePedestrians() {
        pedestrians.forEach(Pedestrian::move);
    }
    public Optional<Pedestrian> getNearestPedestrian(GeoPosition position, double maxDistance) {
        Optional<Pedestrian> pedestrian = getNearestPedestrian(position);
        if(pedestrian.isPresent() && pedestrian.get().distance(position) > maxDistance) {
            return Optional.empty();
        }
        return pedestrian;
    }
    public Set<Pedestrian> getNearestPedestrians(GeoPosition position, double radius) {
        return pedestrians.stream().
                filter(i -> (i.distance(position) <= radius)).
                collect(Collectors.toSet());
    }

    public void repaint() {
        streetLightPainter.setWaypoints(streetLights);
        carPainter.setWaypoints(cars);
        pedestrianPainter.setWaypoints(pedestrians);
        mapViewer.setOverlayPainter(painters);
        Toolkit.getDefaultToolkit().sync();
    }

    private Optional<Car> getNearestCar(GeoPosition position) {
        return cars.stream().
                min(Comparator.comparing(i -> i.distance(position)));
    }

    private Optional<Pedestrian> getNearestPedestrian(GeoPosition position) {
        return pedestrians.stream().
                min(Comparator.comparing(i -> i.distance(position)));
    }



}
