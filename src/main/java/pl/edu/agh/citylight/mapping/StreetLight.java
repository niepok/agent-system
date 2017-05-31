package pl.edu.agh.citylight.mapping;

import jade.core.AID;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import pl.edu.agh.citylight.agents.LampAgent2;

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

    StreetLight(GeoPosition geoPosition, JXMapViewer mapViewer, AID agent){
        super(geoPosition, mapViewer);
        this.agent=agent;
    }
}