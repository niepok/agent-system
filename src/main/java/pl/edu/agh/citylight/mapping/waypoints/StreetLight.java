package pl.edu.agh.citylight.mapping.waypoints;

import jade.core.AID;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import pl.edu.agh.citylight.mapping.Intensity;

public class StreetLight extends Waypoint2D {
    private AID agent;
    private Intensity lightIntensity = Intensity.OFF;

    public AID getAgent() {
        return agent;
    }

    public void setAgent(AID agent) {
        this.agent = agent;
    }

    public Intensity getLightIntensity() {
        return lightIntensity;
    }

    public void setLightIntensity(Intensity lightIntensity) {
        this.lightIntensity = lightIntensity;
    }

    public StreetLight(GeoPosition geoPosition, JXMapViewer mapViewer, AID agent){
        super(geoPosition, mapViewer);
        this.agent=agent;
    }
}