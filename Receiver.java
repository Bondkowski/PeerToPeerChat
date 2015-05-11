package Chat2;

import com.sun.jmx.snmp.internal.SnmpSubSystem;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;


public class Receiver extends Thread{
    private static boolean work = true;
    private ChatNode chatNode;

    public Receiver(ChatNode c){
        chatNode = c;
    }
    public void handleMessage(JSONObject j){
        String type = (String) j.get("type");
        switch (type){
            case "CHAT":
                if(!j.get("username").equals(chatNode.getUsername()))
                    System.out.println(j.get("username") + ": " + j.get("text"));
                break;
            case "JOINING_NETWORK": incomingJoinRequest(j);
                break;
            case "LEAVING_NETWORK": incomingLeaveRequest(j);
                break;
            case "ROUTING_INFO": setNewRoutingInfo(j);
                break;
        }

    }
    public void incomingJoinRequest(JSONObject j){

    }
    public void incomingLeaveRequest(JSONObject j){

    }
    public void setNewRoutingInfo(JSONObject j){

    }
    public void finish(){
        System.exit(1);
    }

    public void run() {
        starting();
    }

    public void starting(){
        byte[] receiveData = new byte[2048];
        try {
            DatagramSocket serverSocket = new DatagramSocket(9999);

            while (work) {
                //System.out.println("Receiver is running");
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                String message = new String(receivePacket.getData());

                try {
                    if (!message.isEmpty()) {
                        message = message.substring(0, (message.indexOf("}")+1));
                        JSONObject jsonObject = (JSONObject) new JSONParser().parse(message);
                        handleMessage(jsonObject);
                    }
                }catch (org.json.simple.parser.ParseException pe){
                    System.out.println("Something is wrong with JSON parsing: " + pe);
                }
            }
        } catch (SocketException se) {
            System.out.println("Cannot start node: " + se.getMessage());
        } catch (IOException ie) {
            System.out.println("Something is wrong with the socket: " + ie.getMessage());
        }
    }
}
