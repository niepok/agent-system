package pl.edu.agh.citylight.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import org.jxmapviewer.viewer.GeoPosition;
import pl.edu.agh.citylight.mapping.Map;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by Adam on 23.05.2017.
 */
public class LampAgentInit extends Agent {
    private BufferedReader br = null;
    private static final String FILENAME = "C:\\Users\\Adam\\Desktop\\Studia\\6Semestr\\studio projektowe\\agent-system\\src\\main\\java\\pl\\edu\\agh\\citylight\\agents\\agents.txt";
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
                map.addStreetLight(new GeoPosition(Double.parseDouble(_cords[0]), Double.parseDouble(_cords[1])), new AID("lamp"+_counter, AID.ISLOCALNAME));
                Object[] argss = {"5",map,map.getLampList().get(_counter)};
                ac = cc.createNewAgent("lamp"+_counter, "pl.edu.agh.citylight.agents.LampAgent2", argss);
                ac.start();
                agents.add(new AID("lamp"+_counter, AID.ISLOCALNAME));
                _counter++;
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }
        addBehaviour(new SetNeighbours());
    }

    private void setCords() {
        try {
            br = new BufferedReader(new FileReader(FILENAME));
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                System.out.println(sCurrentLine);
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
