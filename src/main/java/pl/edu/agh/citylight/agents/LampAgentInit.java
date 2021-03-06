package pl.edu.agh.citylight.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import org.jxmapviewer.viewer.GeoPosition;
import pl.edu.agh.citylight.mapping.Map;

import java.io.*;
import java.util.ArrayList;
import java.util.Vector;

public class LampAgentInit extends LampAgent2 {
    private BufferedReader br = null;
    private static final String FILENAME = "/agents.txt";
    private ArrayList<String> cords = new ArrayList<>();
    private Vector<AID> agents = new Vector<>();
    private int _counter=0;
    private Map map;

    public void setup(){
        Object[] args = getArguments();
        setCords();
        ContainerController cc = (ContainerController) args[1];
        map = (Map) args[2];
        AgentController ac;
        for(String c : cords) {
            try {
                String[] _cords = c.split(";");
                Object[] argss = {"2",map,map.addStreetLight(new GeoPosition(Double.parseDouble(_cords[0]), Double.parseDouble(_cords[1])), new AID("lamp"+_counter, AID.ISLOCALNAME))};
                ac = cc.createNewAgent("lamp"+_counter, "pl.edu.agh.citylight.agents.LampAgent2", argss);
                ac.start();
                agents.add(new AID("lamp"+_counter, AID.ISLOCALNAME));
                _counter++;
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }
        map.repaint();
        addBehaviour(new SetNeighbours());
    }

    private void setCords() {
        try {
            br = new BufferedReader(new InputStreamReader(LampAgentInit.class.getResourceAsStream(FILENAME)));
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                cords.add(sCurrentLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            try {
                if (br != null)
                    br.close();
            } catch (IOException ex) {

                ex.printStackTrace();

            }

        }
    }

    private class SetNeighbours extends OneShotBehaviour {
        @Override
        public void action() {
            //System.out.println("Calling to set neighbours");
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.setConversationId("neighbours");
            for(AID a : agents){
                msg.addReceiver(a);
            }
            myAgent.send(msg);
            //System.out.println("Called to set neighbours");
        }
    }

    protected void takeDown() {
        System.out.println("Lampinit-agent " + getAID().getName() + " terminating.");
    }
}
