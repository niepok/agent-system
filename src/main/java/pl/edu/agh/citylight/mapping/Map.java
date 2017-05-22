package pl.edu.agh.citylight.mapping;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.VirtualEarthTileFactoryInfo;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.viewer.*;

import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.util.*;

public class Map {
    private JXMapViewer mapViewer = new JXMapViewer();
    private ArrayList<StreetLight> lampList = new ArrayList<>();
    private Set<StreetLight> streetLights = new HashSet<>();
    private Set<Car> cars = new HashSet<>();
    private WaypointPainter<Waypoint2D> streetLightPainter = new WaypointPainter<>();
    private WaypointPainter<Waypoint2D> carPainter = new WaypointPainter<>();
    @SuppressWarnings("unchecked")
    private CompoundPainter<JXMapViewer> painters = new CompoundPainter<>(streetLightPainter, carPainter);

    public JXMapViewer getMapViewer() {
        return mapViewer;
    }

	public Map(GeoPosition centerPosition, int zoom) {
	    mapViewer.setTileFactory(createTileFactory());
	    mapViewer.setCenterPosition(centerPosition);
        mapViewer.setZoom(zoom);
        addListeners();
	}

    public StreetLight addStreetLight(GeoPosition position) {
        StreetLight streetLight = new StreetLight(position, mapViewer);
        lampList.add(streetLight);
        streetLights.add(streetLight);
        return streetLight;
    }
    public void removeStreetLight(StreetLight waypoint) {
        streetLights.remove(waypoint);
    }
    public Optional<StreetLight> getNearestStreetLight(GeoPosition position) {
        return streetLights.stream().
                min(Comparator.comparing(i -> i.distance(position)));
    }

    public Car addCar(GeoPosition startPosition, GeoPosition targetPosition) {
        Car car = new Car(startPosition, targetPosition, mapViewer);
        cars.add(car);
        return car;
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

    public void repaint() {
        streetLightPainter.setWaypoints(streetLights);
        carPainter.setWaypoints(cars);
        mapViewer.setOverlayPainter(painters);
        Toolkit.getDefaultToolkit().sync();
    }

    public ArrayList<StreetLight> getLampList() {
        return lampList;
    }

    private Optional<Car> getNearestCar(GeoPosition position) {
        return cars.stream().
                min(Comparator.comparing(i -> i.distance(position)));
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

}
