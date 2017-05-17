package pl.edu.agh.citylight.mapping;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.VirtualEarthTileFactoryInfo;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.viewer.*;

import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class Map {
    private JXMapViewer mapViewer = new JXMapViewer();
    private Set<StreetLight> streetLights = new HashSet<>();
    private Set<Car> cars = new HashSet<>();
    private WaypointPainter<StreetLight> streetLightPainter = new WaypointPainter<>();

    public JXMapViewer getMapViewer() {
        return mapViewer;
    }

	public Map(GeoPosition centerPosition) {
	    mapViewer.setTileFactory(createTileFactory());
	    mapViewer.setCenterPosition(centerPosition);
        mapViewer.setZoom(7);
        addListeners();
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

    public StreetLight addStreetLight(GeoPosition position) {
        StreetLight streetLight = new StreetLight(position, mapViewer);
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

    public Optional<Car> getNearestCar(GeoPosition position) {
        return cars.stream().
                min(Comparator.comparing(i -> i.distance(position)));
    }

    public void repaint() {
        streetLightPainter.setWaypoints(streetLights);
        mapViewer.setOverlayPainter(streetLightPainter);
        Toolkit.getDefaultToolkit().sync();
    }

}
