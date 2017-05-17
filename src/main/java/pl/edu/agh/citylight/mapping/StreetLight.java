package pl.edu.agh.citylight.mapping;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import pl.edu.agh.citylight.agents.LampAgent2;

public class StreetLight extends Waypoint2D {
    private LampAgent2 agent;

    public LampAgent2 getAgent() {
        return agent;
    }

    public void setAgent(LampAgent2 agent) {
        this.agent = agent;
    }

    StreetLight(GeoPosition geoPosition, JXMapViewer mapViewer){
        super(geoPosition, mapViewer);
    }
}