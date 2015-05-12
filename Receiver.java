package Chat2;

import org.json.simple.JSONArray;
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
                    System.out.println(j.get("sender_id") +"   "+ j.get("username") + ": " + j.get("text"));
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
        //System.out.println("Node " + j.get("sender_id") +": "+ j.get("ip_address") + " is joining the network");
        chatNode.addAddressToList((String) j.get("sender_id"), (String) j.get("ip_address"));
        chatNode.getSender().sendAddressList((String) j.get("sender_id"));
        chatNode.getSender().broadcastAddress((String) j.get("sender_id"), (String) j.get("ip_address"));
    }
    public void incomingLeaveRequest(JSONObject j){
        chatNode.removeAddressFromList((String) j.get("sender_id"));
        System.out.println("Node " + j.get("sender_id") + " has left the network.");
    }
    public void setNewRoutingInfo(JSONObject j){
        try {
            String id = null;
            String ip = null;
            JSONArray addressList = (JSONArray) j.get("route_table");
            for(int i = 0; i <addressList.size(); i++){
                JSONObject line = (JSONObject) new JSONParser().parse(addressList.get(i).toString());
                id = (String) line.get("node_id");
                ip = (String) line.get("ip_address");
                chatNode.addAddressToList(id, ip);
            }
            //Advertising arriving of new node if it is not this node
            if(addressList.size()==1)
                if(!chatNode.getNodeID().equals(id))
                    System.out.println("Host " + id + ": " + ip + " has joined the network.");
        }catch (org.json.simple.parser.ParseException jp){
            System.out.println("Something is wrong with parsing Routing information from: " + j.get("sender_id"));
        }
    }
    public void finish(){
        work = false;
        //to be able to stop this process I need to send something to it, otherwise it will wait for it
        chatNode.getSender().sendMessage("", chatNode.getNodeID());
    }

    public void run() {
        starting();
    }

    public void starting(){
        byte[] receiveData;
        try {
            DatagramSocket serverSocket = new DatagramSocket(9999);

            while (work) {
                receiveData = new byte[2048];
                //System.out.println("Receiver is running");
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                String message = new String(receivePacket.getData());

                try {
                    if (!message.isEmpty()) {
                        message = message.substring(0, (message.lastIndexOf("}")+1));
                        //System.out.println(message);
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
