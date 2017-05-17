package pl.edu.agh.citylight.agents;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.util.Iterator;
import java.util.Vector;

/**
 * Created by Adam on 09.05.2017.
 *
 * LampMasterAgent creates a Vector of lampsToManage on one street / part of the street
 *
 * to see how it works: -gui -agents master1:LampMasterAgent(1000,1,5);car1:CarAgent(50);
 * and:
 * 1. Let LampAgent discover the car and get turned on
 * 2. After the lamps are turned on
 *      (in this case one lamp - limitation for easy watching
 *      - as soon as we add OSM synchornization it will work much better due to diffrences in watched areas)
 *      kill car agent in GUI.
 * 3. Let LampAgent turn off the lamp
 * 4. Add new car Agent from GUI and see how it lights up the lamp.
 *
 * @author Adam Niepokój
 * @version 1.0
 * location - not initialized yet, future works: GeoPoint from OSM or Street name
 * length - controlled length
 * lampsAmount - lamps on controlled section
 * lampsToManage - vector with AIDs of lamps in section
 */
public class LampMasterAgent extends Agent {

    private String location;
    private double length;
    private int lampsAmount;
    private Vector lampsToManage = new Vector();

    /**
     * Required method
     * Setting up the Master agent and his slaves - lamps, passing arguments to them
     *
     * cc - gets access to AgentContainer
     * ac - creates new agents dynamically
     * args - arguments passed by user to create LampManagerAgent
     *  [0] = length
     *  [1] = amount of lamps
     *  [2] = ticker period for movement sensor
     * argss - arguments passed to each LampAgent during creation
     *  [0] = masters AID passed to LampAgents for connection purposes
     */
    protected void setup(){
        ContainerController cc = getContainerController();
        AgentController ac;
        lampsToManage.clear();
        Object[] args = getArguments(); //seconds for car sensor passed
        Object[] argss = {this.getAID(), args[2]};
        lampsAmount = Integer.parseInt(String.valueOf(args[1]));
        length = Double.parseDouble(String.valueOf(args[0]));
        for (int i = 0; i<lampsAmount; i++){
            try {
                ac=cc.createNewAgent("lamp_"+i, "LampAgent2", argss);
                ac.start();
                lampsToManage.add(new AID("lamp_"+i, AID.ISLOCALNAME));
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }
        /**
         *  Creating new behaviour
         */
        addBehaviour(new WaitingForSignal());

    }

    /**
     * Inner class WaitingFoSignal is Cyclic (execution stops when Agent is terminated)
     *
     * AID - lamp which gave the "wake up" call for master, not used for now, added for future works
     * status - if lamps are working or not
     * workTime - how long should the lamps be working (according to cars' speed), send to LampAgent
     * speed - cars' speed, received from LampAgent
     */
    private class WaitingForSignal extends CyclicBehaviour {
        private AID sourceLamp;
        private boolean status = false;
        private double workTime = 10.0;
        private double speed;

        /**
         * Required method
         * Agent receives message from slaves and according to current status turns on/off all lamps on the street
         * mt - Message template for Informing about approaching car
         * msg - ACLMessage object to read from
         * turnOnMSG - message send to LedController Behaviour of LampAgent, params: workTime
         * holdSensor - message send to CarSensor Behav. of LampAgent, it turns on WakerBehaviour in lampagent, params: workTime
         * turnOffMSG - opposite of turnOnMSG
         */
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null){
                if(!status) {
                   if(msg.getConversationId().equals("wake-master")) {
                        sourceLamp = msg.getSender();
                        speed = Double.parseDouble(msg.getContent());
                        workTime = length / speed;
                        System.out.println("Turning on the lamps for " + workTime + " sec...");
                        ACLMessage turnOnMSG = new ACLMessage(ACLMessage.REQUEST);
                        ACLMessage holdSensor = new ACLMessage(ACLMessage.INFORM);
                        for (int i = 0; i < lampsToManage.size(); i++) {
                            turnOnMSG.addReceiver((AID) lampsToManage.get(i));
                            holdSensor.addReceiver((AID) lampsToManage.get(i));
                            turnOnMSG.setContent(String.valueOf(workTime));
                            holdSensor.setContent(String.valueOf(workTime));
                            turnOnMSG.setConversationId("lamps-working");
                            holdSensor.setConversationId("hold-sensor");
                            turnOnMSG.setReplyWith("turnOnMSG" + System.currentTimeMillis());
                            holdSensor.setReplyWith("holdSensor" + System.currentTimeMillis());
                            myAgent.send(turnOnMSG);
                            myAgent.send(holdSensor);
                        }
                        status = true;
                    }
                } else {
                    if(msg.getConversationId().equals("sleep-master")) {
                        System.out.println("Turning off the lamps...");
                        ACLMessage turnOffMSG = new ACLMessage(ACLMessage.REQUEST);
                        for (Iterator i = lampsToManage.iterator(); i.hasNext(); ) {
                            turnOffMSG.addReceiver((AID) i.next());
                        }
                        turnOffMSG.setConversationId("lamps-stop-working");
                        turnOffMSG.setReplyWith("turnOffMSG" + System.currentTimeMillis());
                        myAgent.send(turnOffMSG);
                        status = false;
                    }
                }
            } else {
                block();
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
