package pl.edu.agh.citylight.agents;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import pl.edu.agh.citylight.mapping.Car;
import pl.edu.agh.citylight.mapping.Map;
import pl.edu.agh.citylight.mapping.StreetLight;

import java.util.NoSuchElementException;

import static pl.edu.agh.citylight.App.LAMPRANGE;

/**
 * Created by Adam on 09.05.2017.
 *
 * LampAgent is responsible for:
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
    private Map map;

    private AID master;
    private SensorStatus sensorStatus;
    private boolean ledStatus = false;
    private long carSensorPeriod;
    private boolean communicationStatus = false;

    /**
     * Required method
     * it sets up the lampagent with period for movemnt sensor and master's AID
     */
    protected void setup(){
        //lamp init
        System.out.println("Hallo! Lamp-agent "+getAID().getName()+" is ready.");
        sensorStatus = SensorStatus.WAITING;
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            master = (AID) args[0];
            carSensorPeriod = Long.parseLong((String) args[1]);
            System.out.println("This lamp's master is "+master.getName()+" sensor period: "+carSensorPeriod+" sec");
            map = (Map) args[2];
            position = (StreetLight) args[3];
        }

        //behaviour init
        addBehaviour(new CarSensor(this, carSensorPeriod*1000));
        addBehaviour(new ForwardToMaster());
        addBehaviour(new LedController());
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
        private double workTime;

        public CarSensor(Agent a, long period) {
            super(a, period);
        }

        protected void onTick() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage msg = myAgent.receive(mt);
            if(sensorStatus!=SensorStatus.HOLDING) {
                if (msg != null && msg.getConversationId().equals("hold-sensor")) {
                    workTime = Double.parseDouble(msg.getContent());
                    long timeout = (long) workTime * 1000 - this.getPeriod();
                    sensorStatus = SensorStatus.HOLDING;
                    System.out.println(getAID().getName() + " " + sensorStatus + " Putting sensor to sleep for " + timeout / 1000 + " sec");

                    /**
                     * Anonymus class WakerBehaviour
                     * Holds looking for new cars (and all other processes in fact) after turning on the lamps
                     * Launched after receiving proper message from Master
                     *
                     * timeout - period during which lamp is asleep (only shinin bright like a diamond)
                     * after timeout changing status to SCANNING - diffrent than waiting for new cars - due to light status
                     * and its opportunity to be turned off
                     */
                    addBehaviour(new WakerBehaviour(LampAgent2.this, timeout) {
                        protected void onWake() {
                            sensorStatus = SensorStatus.SCANNING;
                            communicationStatus = false;
                            System.out.println(getAID().getName() + " " + sensorStatus + " Scanning again...");
                        }
                    });
                }
                if(sensorStatus==SensorStatus.SCANNING || sensorStatus==SensorStatus.WAITING){
                    System.out.println(getAID().getName() +" "+sensorStatus+ " Checking if car is approaching");
                    try {
                        nearestCar = map.getNearestCar(position.getPosition(), LAMPRANGE).get();
                        sensorStatus=SensorStatus.CAR_DETECTED;
                        System.out.println(getAID().getName() +" "+sensorStatus+ " found car nearby");
                    } catch (NoSuchElementException e) {
                        if(sensorStatus==SensorStatus.SCANNING) sensorStatus=SensorStatus.NO_MORE_CARS;
                    }
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
    private class ForwardToMaster extends CyclicBehaviour {
        private double speed;

        public void action() {
            switch (sensorStatus) {
                case CAR_DETECTED:
                    speed = nearestCar.getSpeed();
                    if (!ledStatus && !communicationStatus) {
                        System.out.println(getAID().getName() +" "+sensorStatus+ " woke master");
                        ACLMessage wakeMaster = new ACLMessage(ACLMessage.INFORM);
                        wakeMaster.addReceiver(master);
                        wakeMaster.setContent(String.valueOf(speed));
                        wakeMaster.setConversationId("wake-master");
                        myAgent.send(wakeMaster);
                        communicationStatus = true;
                    }
                    else if (ledStatus && !communicationStatus){
                        System.out.println(getAID().getName() +" "+sensorStatus+ " still having cars");
                        ACLMessage stillCars = new ACLMessage(ACLMessage.INFORM);
                        stillCars.addReceiver(master);
                        stillCars.setContent(String.valueOf(speed));
                        stillCars.setConversationId("still-cars");
                        myAgent.send(stillCars);
                        communicationStatus = true;
                    }
                    break;
                case NO_MORE_CARS:
                    if(ledStatus) {
                        ACLMessage sleepMaster = new ACLMessage(ACLMessage.INFORM);
                        sleepMaster.addReceiver(master);
                        sleepMaster.setConversationId("sleep-master");
                        sleepMaster.setSender(this.getAgent().getAID());
                        myAgent.send(sleepMaster);
                        speed = 0.0;
                        communicationStatus = true;
                    }
                    sensorStatus=SensorStatus.WAITING;
                    break;
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
            if(sensorStatus!=SensorStatus.HOLDING)
                if (msg != null) {
                    if (!ledStatus && msg.getConversationId().equals("lamps-working")) {
                        System.out.println(getAID().getName() +" "+sensorStatus+ " turned on");
                        /**
                         * turning on the lamp (visualization)
                         */
                        ledStatus = true;
                        communicationStatus = false;
                        /**
                         * future works: response to master if lamp is working properly
                         */
                    } else if(ledStatus && msg.getConversationId().equals("lamps-stop-working")) {
                        System.out.println(getAID().getName() +" "+sensorStatus+ " turned off");
                        ledStatus = false;
                        sensorStatus = SensorStatus.WAITING;
                        communicationStatus = false;
                        /**
                         * future works: response to master if lamp is working properly
                         */
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
}
