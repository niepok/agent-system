package pl.edu.agh.citylight.agents;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

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
public class LampAgent2 extends Agent {
    private String location = "Lamp";
    private AID[] carAgents;
    private AID master;
    private MessageTemplate carSensorMT;
    private SensorStatus sensorStatus;
    private boolean ledStatus = false;
    private long carSensorPeriod;

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
            if(msg!=null){
                workTime = Double.parseDouble(msg.getContent());
                long timeout = (long) workTime*1000 - this.getPeriod();
                System.out.println("Putting sensor to sleep for "+timeout/1000+" sec");

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
                    protected void onWake(){
                        System.out.println("Scanning again...");
                        carAgents=null;
                        sensorStatus = SensorStatus.SCANNING;
                    }
                });
            }
            if (sensorStatus != SensorStatus.HOLDING) {
                /**
                 * Setting the template for incoming car agents
                 * Future works: change service description type to some kind of coordinates or area from OSM
                 */
                System.out.println(getAID().getName() + " Checking if car is approaching");
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("car-approaching");
                template.addServices(sd);
                /**
                 * if there are some cars - change status to detected
                 * if there are not - do nothing, unless the lamp is turned on (not Waiting)
                 * then change status to no_more_cars
                 */
                try {
                    DFAgentDescription[] result = DFService.search(myAgent, template);
                    if (result.length==0){
                        System.out.println("No cars coming");
                        if(sensorStatus != SensorStatus.WAITING) sensorStatus = SensorStatus.NO_MORE_CARS;
                    } else {
                        sensorStatus = SensorStatus.CAR_DETECTED;
                        System.out.println(getAID().getName() + " found the following car agents:");
                        carAgents = new AID[result.length];
                        for (int i = 0; i < result.length; ++i) {
                            carAgents[i] = result[i].getName();
                            System.out.println(carAgents[i].getName());
                        }
                    }
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                } catch (NullPointerException e) {
                    System.out.println("Still scanning...");
                }
                /**
                 * Sending broadcast to all detected cars
                 */
                if(sensorStatus==SensorStatus.CAR_DETECTED) {
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    for (int i = 0; i < carAgents.length; ++i) {
                        cfp.addReceiver(carAgents[i]);
                    }
                    cfp.setConversationId("car-detected");
                    cfp.setReplyWith("cfp" + System.currentTimeMillis()); // Unique value
                    myAgent.send(cfp);

                    // Prepare the template to get proposals
                    carSensorMT = MessageTemplate.and(MessageTemplate.MatchConversationId("car-detected"), MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    System.out.println("Waiting for cars...");
                }
            }
        }
    }

    /**
     * Inner class implementing communication with master
     * It's cyclic - execution stops only with agent termination so it's important to use  clauses
     *
     * step - stage of communication:
     *      1 - receiving responses from cars
     *      2 - received all responses and informing master about situation
     *      3 - informing master about no cars situation - lamps can be turned off
     * closest car - car agent AID which is the closest (future works: delay in turning on according to speed and distance)
     * distance - from lamp to closest car
     * repliesCnt - for checking
     */
    private class ForwardToMaster extends CyclicBehaviour {
        private int step = 1;
        private AID closestCar;
        private double distance;
        private int repliesCnt = 0;

        public void action() {
            switch (step) {
                case 1:
                    if(sensorStatus != SensorStatus.HOLDING) {
                        ACLMessage reply = myAgent.receive(carSensorMT);
                        if (reply != null) {
                            // Reply received
                            if (reply.getPerformative() == ACLMessage.PROPOSE) {
                                // This is an offer
                                System.out.println(getAID().getName()+" calculating the distance...");
                                double speed = Double.parseDouble(reply.getContent());
                                /**
                                 * now: time set according to the slowest car
                                 * future works: change to real distance and suitable time delay according to speed and distance
                                 */
                                if (closestCar == null || speed < distance) {
                                    // This is the best offer at present
                                    distance = speed;
                                    closestCar = reply.getSender();
                                    System.out.println("Closest car is " + closestCar.getName() + ", speed: " + distance);
                                }
                            }
                            /**
                             * if all responses have been checked proceed to lamps launching with proper attributes
                             */
                            repliesCnt++;
                            if (repliesCnt >= carAgents.length) {
                                // We received all replies
                                System.out.println("All "+repliesCnt+" cars checked");
                                if(sensorStatus==SensorStatus.CAR_DETECTED) step = 2;
                            }
                        } else {
                            block();
                            /**
                             * if there are no replies and lamp is still shinin proceed to lamps shutting down
                             */
                            if(sensorStatus == SensorStatus.NO_MORE_CARS) {
                                step = 3;
                            }
                        }
                    }
                    break;
                case 2:
                    /**
                     * waking master - lamps launching
                     */
                    if(!ledStatus) {
                        System.out.println(getAID().getName()+" woke master");
                        ACLMessage wakeMaster = new ACLMessage(ACLMessage.INFORM);
                        wakeMaster.addReceiver(master);
                        wakeMaster.setContent(String.valueOf(distance));
                        wakeMaster.setConversationId("wake-master");
                        wakeMaster.setReplyWith("wakeMaster" + System.currentTimeMillis());
                        myAgent.send(wakeMaster);
                    }
                    /**
                     * return to listening for cars' replies part
                     */
                    step=1;
                    repliesCnt = 0;
                    break;
                case 3:
                    /**
                     * putting master to sleep - lamps shutting down
                     */
                    ACLMessage sleepMaster = new ACLMessage(ACLMessage.INFORM);
                    sleepMaster.addReceiver(master);
                    sleepMaster.setConversationId("sleep-master");
                    sleepMaster.setReplyWith("sleepMaster"+System.currentTimeMillis());
                    myAgent.send(sleepMaster);
                    step=1;
                    repliesCnt = 0;
                    distance=0;
                    closestCar = null;
                    break;
            }
        }
    }

    /**
     * Inner class implementing LedController
     * it waits for proper message from master (matching to mt)
     * activity in action according to ledStatus = if the lamps are on or off
     *
     */
    private class LedController extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                if (!ledStatus && msg.getConversationId().equals("lamps-working")) {
                    System.out.println(getAID().getName() + " turned on for");
                    /**
                     * turning on the lamp (visualization)
                     */
                    ledStatus = true;
                    sensorStatus = SensorStatus.HOLDING;
                    /**
                     * future works: response to master if lamp is working properly
                     */
                } else {
                    System.out.println(getAID().getName() + " turned off");
                    ledStatus = false;
                    sensorStatus = SensorStatus.WAITING;
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
    protected void takeDown(){
        System.out.println("Lamp-agent "+getAID().getName()+" terminating.");
    }
}
