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
 */
public class LampAgent extends Agent {
    private String location = "Lamp";
    private AID[] carAgents={
            new AID("car1", AID.ISLOCALNAME),
            new AID("car2", AID.ISLOCALNAME)
    };
    private AID master;

    protected void setup(){
        System.out.println("Hallo! Lamp-agent "+getAID().getName()+" is ready.");

        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            master = (AID) args[0];
            System.out.println("This lamp's master is "+master.getName());
        }


        addBehaviour(new TickerBehaviour(this, 50000) {
            @Override
            protected void onTick() {
                System.out.println(getAID().getName()+" Checking if car is approaching");
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("car-approaching");
                template.addServices(sd);

                try {
                    DFAgentDescription[] result = DFService.search(myAgent, template);
                    System.out.println(getAID().getName()+" found the following car agents:");
                    carAgents = new AID[result.length];
                    for (int i = 0; i < result.length; ++i) {
                        carAgents[i] = result[i].getName();
                        System.out.println(carAgents[i].getName());
                    }
                    //tutaj ewentualne usypianie sprawdzania i przeniesienie RequestPerformera
                }
                catch (FIPAException fe) {
                    fe.printStackTrace();
                }
                
                myAgent.addBehaviour(new CarSensor());
                myAgent.addBehaviour(new LedController());
            }
        });

        //myAgent.addBehaviour(prześlij do sterownika));
    }

    protected void takeDown(){
        System.out.println("Lamp-agent "+getAID().getName()+" terminating.");
    }
    
    private class CarSensor extends CyclicBehaviour {
        private int step = 0;
        private MessageTemplate mt; // The template to receive replies
        private AID closestCar; // The agent who provides the best offer
        private double distance;  // The best offered price
        private int repliesCnt = 0; // The counter of replies from seller agents

        public void action() {
            switch (step) {
                case 0:
                    // Send the cfp to all cars
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    for (int i = 0; i < carAgents.length; ++i) {
                        cfp.addReceiver(carAgents[i]);
                    }
                    cfp.setConversationId("car-detected");
                    cfp.setReplyWith("cfp" + System.currentTimeMillis()); // Unique value
                    myAgent.send(cfp);

                    // Prepare the template to get proposals
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("car-detected"),
                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    step = 1;
                    System.out.println("Waiting for cars...");
                    break;
                case 1:
                    ACLMessage reply = myAgent.receive(mt);
                    if (reply != null) {

                        //waking master
                        ACLMessage wakeMaster = new ACLMessage(ACLMessage.INFORM);
                        wakeMaster.addReceiver(master);
                        wakeMaster.setContent(reply.getContent());
                        wakeMaster.setConversationId("wake-master");
                        wakeMaster.setReplyWith("wakeMaster"+System.currentTimeMillis());
                        myAgent.send(wakeMaster);
                        //mtMaster=MessageTemplate.and(MessageTemplate.MatchConversationId("wake-master"),
                         //       MessageTemplate.MatchInReplyTo(wakeMaster.getReplyWith()));

                        // Reply received
                        if (reply.getPerformative() == ACLMessage.PROPOSE) {
                            // This is an offer
                            System.out.println("Calculating the distance...");
                            double speed = Double.parseDouble(reply.getContent());
                            if (closestCar == null || speed > distance) {
                                // This is the best offer at present
                                distance = speed;
                                closestCar = reply.getSender();
                                System.out.println("Closest car is "+closestCar.getName()+", speed: "+distance);
                            }
                        }
                        repliesCnt++;
                        if (repliesCnt >= carAgents.length) {
                            // We received all replies
                            step = 2;
                            System.out.println("No more cars coming");
                            //step=0;
                        }
                    } else {
                        block();
                        // wyłącz latarnie
                    }
                    break;
                case 2:
                    mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                    ACLMessage msg = myAgent.receive(mt);
                    if(msg!=null){
                        double workTime = Double.parseDouble(msg.getContent());
                        addBehaviour(new WakerBehaviour(LampAgent.this, (long) workTime) {
                            protected void onWake(){
                                System.out.println("Scanning again...");
                            }
                        });
                    }
            }
        }
    }

    private class LedController extends CyclicBehaviour {
        private boolean status = false;
        private double workTime = 10.0;
        private String operation ="";

        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage msg = myAgent.receive(mt);
            if (!status) {
                if (msg != null){// && operation.equals("lamps-working")) {
                    //workTime = Double.parseDouble(msg.getContent());
                    workTime = Double.parseDouble(msg.getContent());
                    if (workTime != 0.0){
                        System.out.println(getAID().getName()+" turned on for "+workTime+" sec");
                        status = true;
                        //odpowiedź o sukcesie (diagonstyka)
                    }
                }

            } else {
                //operation=msg.getConversationId();
                if (msg != null){// && operation.equals("lamps-stop-working")) {
                    System.out.println(getAID().getName()+" turned off");
                    status = false;
                    workTime = 0.0;
                    //odpowiedź o sukcesie (diagonstyka)
                }

            }
        }
    }
}
