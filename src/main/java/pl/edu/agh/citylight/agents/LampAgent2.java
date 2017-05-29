package pl.edu.agh.citylight.agents;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import pl.edu.agh.citylight.mapping.Car;
import pl.edu.agh.citylight.mapping.Map;
import pl.edu.agh.citylight.mapping.StreetLight;

import java.util.*;

import static pl.edu.agh.citylight.App.LAMPRANGE;

/**
 * Created by Adam on 09.05.2017.
 *
 * LampAgent2 is responsible for:
 *  CarSensor which check if theere is a car approaching (currently only presence in system)
 *  ForwardToMaster - behaviour for communication with master according to car movement
 *  LedController - turning on/off lamp according to messages from Master
 *
 * @auhor Adam Niepokój
 * @version 2.0 - separated behaviours, cleaner code
 *
 * location - not initialized yet - for future works GeoPoint from OSM
 * carAgents - car agents present in system - future works: present in observed area <- OSM synchronization
 * master - master's AID for communication
 * carSensorMT - message template for communication with approaching car
 * sensorStatus - car sensor status, VERY IMPORTANT
 * @see SensorStatus
 * ledStatus - if the lamp's shinin'
 * carSensorPeriod - passed from Master, time in milliseconds, how often do the sensor check for car presence
 *
 */
@SuppressWarnings("Duplicates")
public class LampAgent2 extends Agent {

    private StreetLight position;
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
        //lamp init
        //System.out.println("Hallo! Lamp-agent "+getAID().getName()+" is ready.");
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            carSensorPeriod = Long.parseLong((String) args[0]);
            //System.out.println(getAID().getName()+" This lamp's period: "+carSensorPeriod+" sec");
            map = (Map) args[1];
            position = (StreetLight) args[2];
        }


        sensorStatus = SensorStatus.WAITING;
        //behaviour init
        //addBehaviour(new PedestrianSensor(this, 2000));
        addBehaviour(new CarSensor(this, carSensorPeriod*1000));
        addBehaviour(new Receiver());
        addBehaviour(new Sender());
        addBehaviour(new LedController());
    }

    public void setNeighbourLamps(){
        neighbourLamps = map.getStreetLights(position.getPosition(), 2*LAMPRANGE);
        System.out.println(getAID().getName()+" this lamp's neighbours: "+neighbourLamps.size());
    }
    /**
     * Inner class
     * Impelemnts movement sensor for lampagent.
     *
     * This version: looks up all the car agents in yellow pages (all in system)
     * Future works: looks up car agents only in specific area (probably radius around GeoPoint from OSM)
     *               case when car stopped in area (maybe special message type?)
     * After finding cars it sends broadcast message to all of them due to receive their position (currently speed - only numeric version)
     *
     * Agent a - parent agent
     * long period - sensor frequency
     */
    private class CarSensor extends TickerBehaviour {
        CarSensor(Agent a, long period) {
            super(a, period);
        }

        protected void onTick() {
            if(sensorStatus==SensorStatus.SCANNING || sensorStatus==SensorStatus.WAITING){
                //System.out.println(getAID().getName() +" "+sensorStatus+ " Checking if car is approaching");
                if(neighbourLamps.size()==2) nearestCars = map.getNearestCars(position.getPosition(), 1.5*LAMPRANGE);
                else nearestCars = map.getNearestCars(position.getPosition(), LAMPRANGE);
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

    /**
     * Inner class implementing communication with master
     * It's cyclic - execution stops only with agent termination so it's important to use  clauses
     * <p>
     * step - stage of communication:
     * 1 - receiving responses from cars
     * 2 - received all responses and informing master about situation
     * 3 - informing master about no cars situation - lamps can be turned off
     * closest car - car agent AID which is the closest (future works: delay in turning on according to speed and distance)
     * distance - from lamp to closest car
     * repliesCnt - for checking
     */
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
                    //communicationStatus = true;
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

    /**
     * Inner class implementing LedController
     * it waits for proper message from master (matching to mt)
     * activity in action according to ledStatus = if the lamps are on or off
     */
    private class LedController extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                if (!ledStatus && msg.getConversationId().equals("turn-on")) {
                    System.out.println(getAID().getName() +" "+sensorStatus+ " turned on for "+ Collections.max(receivedSignals.values()) + " by "+msg.getSender().getName());
                    ledStatus = true;
                } else if(msg.getConversationId().equals("adjust") && !pedestrian) {
                    System.out.println(getAID().getName() + " shinning for " + Collections.max(receivedSignals.values()));
                } else if(msg.getConversationId().equals("pedestrian-detected")) {
                    System.out.println(getAID().getName() + " detected a pedestrian, shining maximum power");
                    if (!ledStatus) ledStatus = true;
                } else if(msg.getConversationId().equals("no-more-pedestrians")){
                    if(!receivedSignals.isEmpty()) System.out.println(getAID().getName()+" no more pedestrians, backing lamp power to "+Collections.max(receivedSignals.values()));
                    else {
                        System.out.println(getAID().getName() +" "+sensorStatus+ " turned off by "+msg.getSender().getName());
                        ledStatus = false;
                        sensorStatus = SensorStatus.WAITING;
                    }
                } else if(msg.getConversationId().equals("turn-off")){
                    System.out.println(getAID().getName() +" "+sensorStatus+ " turned off by "+msg.getSender().getName());
                    ledStatus = false;
                    sensorStatus = SensorStatus.WAITING;
                }
            }
        }
    }

    /**
     * proper termination requirement
     */
    protected void takeDown() {
        System.out.println("Lamp-agent " + getAID().getName() + " terminating.");
    }
    /*
    private class PedestrianSensor extends TickerBehaviour {
        PedestrianSensor(Agent a, long period) {
            super(a, period);
        }

        @Override
        protected void onTick() {
            ACLMessage pedestrianDetected = new ACLMessage(ACLMessage.REQUEST);
            pedestrianDetected.addReceiver(this.getAgent().getAID());
            if(map.checkForPedestrians(position.getPosition(), 0.5*LAMPRANGE) && !pedestrian) {
                pedestrianDetected.setConversationId("pedestrian-detected");
                myAgent.send(pedestrianDetected);
                pedestrian=true;
            } else if (!map.checkForPedestrian(position.getPosition(), 0.5*LAMPRANGE) && pedestrian){
                pedestrianDetected.setConversationId("no-more-pedestrians");
                myAgent.send(pedestrianDetected);
                pedestrian=false;
            }
        }
    }*/
}
