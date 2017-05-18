package pl.edu.agh.citylight.agents;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import pl.edu.agh.citylight.mapping.Car;


/**
 * Created by Adam on 09.05.2017.
 *
 * CarAgent simply moves forward and answers for LampAgent broadcast with it's speed (current version)
 *
 * @author Adam Niepokój
 * @version 1.0
 *
 * speed - current speed (constant movement)
 *
 * future works:
 *      acceleration
 *      stop in city area
 *      synchronization with OSM
 */
public class CarAgent extends Agent{

    private Car car;

    private double speed;

    /**
     * Required method
     * Sets up the car agent with given speed (args)
     * and registers it in Yellow Pages
     */
    protected void setup() {
        System.out.println("Hallo! Car-agent "+getAID().getName()+" is ready.");

        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            speed = Double.parseDouble((String) args[0]);
            System.out.println("Car speed is "+speed+" m/s");
        }
        else speed=14;

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("car-approaching");
        sd.setName(car.getStringPosition());
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

        addBehaviour(new Respond());
    }

    /**
     * proper termination requirement
     */
    protected void takeDown() {
        // Deregister from the yellow pages
        try {
            DFService.deregister(this);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
        // Close the GUI
        //myGui.dispose();
        // Printout a dismissal message
        System.out.println("Car-agent "+getAID().getName()+" terminating.");
    }

    /**
     * Inner class implementing responding to lamp's car sensor broadcasting
     * if speed is no 0 then car replies with it's speed
     */
    private class Respond extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                // CFP Message received. Process it
                //String location = msg.getContent();
                ACLMessage reply = msg.createReply();
                //Integer price = (Integer) catalogue.get(title);
                if (speed != 0) {
                    // The requested book is available for sale. Reply with the price
                    reply.setPerformative(ACLMessage.PROPOSE);
                    reply.setContent(String.valueOf(speed));
                    System.out.println("Car received signal, sending reply");

                } else {
                    /**
                     * Basis for car stoping - to be continued...
                     */
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("car-stopped");
                }
                myAgent.send(reply);
            } else {
                block();
            }
        }
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car position) {
        this.car = position;
    }
}
