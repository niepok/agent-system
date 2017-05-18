package pl.edu.agh.citylight.mapping;

import org.jxmapviewer.viewer.GeoPosition;

/**
 * Created by Adam on 18.05.2017.
 */
public class CarSpeed {

    private double carSpeed;

    CarSpeed(double _carSpeed){
        carSpeed=_carSpeed;
    }

    public double getDistanceInKm(double startLatitude, double startLongitude, double endLatitude, double endLongitude){
        double R = 6371.0;
        double dLat = deg2rad(endLatitude-startLatitude);
        double dLon = deg2rad(endLongitude-startLongitude);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2)+ Math.cos(deg2rad(startLatitude))*Math.cos(deg2rad(endLatitude))*Math.sin(dLon/2)*Math.sin(dLon/2);
        double c = 2*Math.atan2(Math.sqrt(a),Math.sqrt(1-a));
        double d = R*c; //distance in km
        return d;
    }

    double deg2rad(double deg){
        return deg*(Math.PI/180);
    }

    //timePeriod - miliseconds
    public void setCarSpeed(GeoPosition start, GeoPosition end, double timePeriod){
        this.carSpeed = (getDistanceInKm(start.getLatitude(),start.getLongitude(),end.getLatitude(),end.getLongitude())/1000)/(timePeriod*1000);
    }

    public double getCarSpeed() {
        return carSpeed;
    }
}
