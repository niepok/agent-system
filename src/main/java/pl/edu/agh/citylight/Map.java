package pl.edu.agh.citylight;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
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

class Map {
    private JXMapViewer mapViewer = new JXMapViewer();
    private Set<StreetLight> streetLights = new HashSet<>();
    private WaypointPainter<StreetLight> streetLightPainter = new WaypointPainter<>();

    JXMapViewer getMapViewer() {
        return mapViewer;
    }

	Map(GeoPosition centerPosition) {
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

    StreetLight addStreetLight(GeoPosition position) {
        StreetLight streetLight = new StreetLight(position);
        streetLights.add(streetLight);
        return streetLight;
    }

	void removeStreetLight(StreetLight waypoint) {
	    streetLights.remove(waypoint);
    }

    Optional<StreetLight> getNearestWaypoint(GeoPosition position) {
        return streetLights.stream().
                min(Comparator.comparing(i -> i.distance(position, mapViewer)));
    }

    void repaint() {
        streetLightPainter.setWaypoints(streetLights);
        mapViewer.setOverlayPainter(streetLightPainter);
        Toolkit.getDefaultToolkit().sync();
    }

}
