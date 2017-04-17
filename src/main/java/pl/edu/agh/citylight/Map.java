package pl.edu.agh.citylight;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.viewer.*;

import javax.swing.event.MouseInputListener;
import java.util.HashSet;
import java.util.Set;

class Map {
    private JXMapViewer mapViewer = new JXMapViewer();
    private Set<Waypoint> waypoints = new HashSet<>();
    private WaypointPainter<Waypoint> waypointPainter = new WaypointPainter<>();

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
        TileFactoryInfo info = new OSMTileFactoryInfo();
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

    Waypoint addWaypoint(GeoPosition position) {
        Waypoint waypoint = new DefaultWaypoint(position);
        waypoints.add(waypoint);
        waypointPainter.setWaypoints(waypoints);
        mapViewer.setOverlayPainter(waypointPainter);
        return waypoint;
    }

	void removeWaypoint(Waypoint waypoint) {
	    waypoints.remove(waypoint);
    }

}
