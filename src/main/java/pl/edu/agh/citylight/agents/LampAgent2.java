package pl.edu.agh.citylight.agents;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import pl.edu.agh.citylight.mapping.Car;
import pl.edu.agh.citylight.mapping.Intensity;
import pl.edu.agh.citylight.mapping.Map;
import pl.edu.agh.citylight.mapping.StreetLight;


import java.util.*;

import static pl.edu.agh.citylight.App.LAMPRANGE;

/**
 * Created by Adam on 09.05.2017.
 */
@SuppressWarnings("Duplicates")
public class LampAgent2 extends Agent {

    private StreetLight lampObject;
    private Car nearestCar;
    private Set<Car> nearestCars;
    private Map map;
    private Set<StreetLight> neighbourLamps;

    private SensorStatus sensorStatus;
    private boolean ledStatus = false;
    private boolean pedestrian = false;
    private long carSensorPeriod;
    private boolean communicationStatus = false;
    private int carsDetected;
    private java.util.Map<AID, Integer> receivedSignals = new HashMap<>();

    /**
     * Required method
     * it sets up the lampagent with period for movemnt sensor and master's AID
     */
    protected void setup(){
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            carSensorPeriod = Long.parseLong((String) args[0]);
            map = (Map) args[1];
            lampObject = (StreetLight) args[2];
        }

        sensorStatus = SensorStatus.WAITING;
        addBehaviour(new PedestrianSensor(this, 2000));
        addBehaviour(new CarSensor(this, carSensorPeriod*1000));
        addBehaviour(new Receiver());
        addBehaviour(new Sender());
        addBehaviour(new LedController());
    }

    public void setNeighbourLamps(){
        neighbourLamps = map.getStreetLights(lampObject.getPosition(), 2*LAMPRANGE);
        System.out.println(getAID().getName()+" this lamp's neighbours: "+neighbourLamps.size());
    }

    private class CarSensor extends TickerBehaviour {
        CarSensor(Agent a, long period) {
            super(a, period);
        }

        protected void onTick() {
            if(sensorStatus==SensorStatus.SCANNING || sensorStatus==SensorStatus.WAITING){
                if(neighbourLamps.size()==2) nearestCars = map.getNearestCars(lampObject.getPosition(), 1.5*LAMPRANGE);
                else nearestCars = map.getNearestCars(lampObject.getPosition(), LAMPRANGE);
                if(nearestCars.size()>0) {
                    System.out.println(getAID().getName() + " " + sensorStatus + " found car nearby: " + nearestCars.size());
                    for (Car car : nearestCars) {
                        if(nearestCar==null) nearestCar=car;
                        else if (car.getSpeed() > nearestCar.getSpeed()) nearestCar = car;
                    }
                    carsDetected = nearestCars.size();
                    receivedSignals.put(this.getAgent().getAID(), carsDetected);
                    sensorStatus = SensorStatus.CARS_DETECTED;
                } else if(sensorStatus==SensorStatus.SCANNING){
                    receivedSignals.remove(this.getAgent().getAID());
                    sensorStatus=SensorStatus.NO_MORE_CARS;
                }
            }

        }
    }

    private class Sender extends CyclicBehaviour {
        public void action() {
            switch (sensorStatus) {
                case CARS_DETECTED:
                    if (!communicationStatus) {
                        ACLMessage wakingNeighbours = new ACLMessage(ACLMessage.INFORM);
                        for(StreetLight lamp : neighbourLamps){
                            wakingNeighbours.addReceiver(lamp.getAgent());
                        }
                        wakingNeighbours.setContent(String.valueOf(nearestCar.getSpeed())+";"+String.valueOf(carsDetected));
                        wakingNeighbours.setConversationId("cars-detected");
                        wakingNeighbours.setSender(this.getAgent().getAID());
                        myAgent.send(wakingNeighbours);
                        System.out.println(getAID().getName() +" "+sensorStatus+ " waking neighbours");
                        communicationStatus = true;
                        if (ledStatus) sensorStatus=SensorStatus.SCANNING;
                    }
                    break;
                case NO_MORE_CARS:
                    ACLMessage noMoreCars = new ACLMessage(ACLMessage.INFORM);
                    for(StreetLight lamp : neighbourLamps){
                        noMoreCars.addReceiver(lamp.getAgent());
                    }
                    noMoreCars.setConversationId("no-more-cars");
                    noMoreCars.setSender(this.getAgent().getAID());
                    myAgent.send(noMoreCars);
                    sensorStatus=SensorStatus.WAITING;

                    break;
            }
        }
    }

    private class Receiver extends CyclicBehaviour{
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage msg = myAgent.receive(mt);
            if(msg!=null){
                if(neighbourLamps==null && msg.getConversationId().equals("neighbours")){
                    setNeighbourLamps();
                } else if(msg.getConversationId().equals("cars-detected")){
                    String[] split = msg.getContent().split(";");
                    receivedSignals.put(msg.getSender(), Integer.parseInt(split[1]));
                    ACLMessage turnOn = new ACLMessage(ACLMessage.REQUEST);
                    turnOn.addReceiver(this.getAgent().getAID());
                    if (!ledStatus) turnOn.setConversationId("turn-on");
                    else turnOn.setConversationId("adjust");
                    turnOn.setContent(String.valueOf(LAMPRANGE/Double.parseDouble(split[0])));
                    myAgent.send(turnOn);
                    communicationStatus = false;
                } else if(msg.getConversationId().equals("no-more-cars")){
                    receivedSignals.remove(msg.getSender());
                    if(receivedSignals.isEmpty()) {
                        ACLMessage turnOff = new ACLMessage(ACLMessage.REQUEST);
                        turnOff.setConversationId("turn-off");
                        turnOff.addReceiver(this.getAgent().getAID());
                        myAgent.send(turnOff);
                    }
                }
            }
        }
    }

    private class LedController extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                if (!ledStatus && msg.getConversationId().equals("turn-on")) {
                    System.out.println(getAID().getName() +" "+sensorStatus+ " turned on for "+ Collections.max(receivedSignals.values()));
                    lampObject.setLightIntensity(ledIntensity(Collections.max(receivedSignals.values())));
                    ledStatus = true;
                } else if(msg.getConversationId().equals("adjust") && !pedestrian) {
                    lampObject.setLightIntensity(ledIntensity(Collections.max(receivedSignals.values())));
                } else if(msg.getConversationId().equals("pedestrian-detected")) {
                    System.out.println(getAID().getName() + " detected a pedestrian, shining maximum power");
                    lampObject.setLightIntensity(Intensity.HIGH);
                    if (!ledStatus) ledStatus = true;
                } else if(msg.getConversationId().equals("no-more-pedestrians")){
                    if(!receivedSignals.isEmpty()){
                        System.out.println(getAID().getName()+" no more pedestrians, backing lamp power to "+Collections.max(receivedSignals.values()));
                        lampObject.setLightIntensity(ledIntensity(Collections.max(receivedSignals.values())));
                    }
                    else {
                        System.out.println(getAID().getName() +" "+sensorStatus+ " turned off");
                        lampObject.setLightIntensity(Intensity.OFF);
                        ledStatus = false;
                        sensorStatus = SensorStatus.WAITING;
                    }
                } else if(msg.getConversationId().equals("turn-off") &&!pedestrian){
                    System.out.println(getAID().getName() +" "+sensorStatus+ " turned off");
                    lampObject.setLightIntensity(Intensity.OFF);
                    ledStatus = false;
                    sensorStatus = SensorStatus.WAITING;
                }
            }
        }
    }

    private Intensity ledIntensity(int amount){
        if(amount>5) return Intensity.HIGH;
        else if (amount>0 && amount<=2) return Intensity.LOW;
        else if (amount>2 && amount<=5) return Intensity.MEDIUM;
        else return Intensity.OFF;
    }

    /**
     * proper termination requirement
     */
    protected void takeDown() {
        System.out.println("Lamp-agent " + getAID().getName() + " terminating.");
    }

    private class PedestrianSensor extends TickerBehaviour {
        PedestrianSensor(Agent a, long period) {
            super(a, period);
        }

        @Override
        protected void onTick() {
            ACLMessage pedestrianDetected = new ACLMessage(ACLMessage.REQUEST);
            pedestrianDetected.addReceiver(this.getAgent().getAID());
            if(map.getNearestPedestrian(lampObject.getPosition(), 0.75*LAMPRANGE).isPresent() && !pedestrian) {
                pedestrianDetected.setConversationId("pedestrian-detected");
                myAgent.send(pedestrianDetected);
                pedestrian=true;
            } else if (!map.getNearestPedestrian(lampObject.getPosition(), 0.75*LAMPRANGE).isPresent() && pedestrian){
                pedestrianDetected.setConversationId("no-more-pedestrians");
                myAgent.send(pedestrianDetected);
                pedestrian=false;
            }
        }
    }
}