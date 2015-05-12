package Chat2;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;

import java.util.Map;


public class Sender {

    private int portNumber;
    private String username;
    private String nodeID;
    private ChatNode chatNode;
    private DatagramSocket senderSocket;
    private byte[] sendData;

    public Sender(ChatNode c) throws Exception{
        chatNode = c;
        sendData = new byte[2048];
        portNumber = 9999;
        username = c.getUsername();
        nodeID = chatNode.getNodeID();
    }

    public void chatBroadcast(String s){
        if (!s.isEmpty()) {
            try {
                Map<String, String> map = new HashMap<>();
                map.put("type", "CHAT");
                map.put("sender_id", nodeID);
                map.put("username", username);
                map.put("text", "Broadcast: " + s.substring(7));
                String jsonString = JSONValue.toJSONString(map);
                //System.out.println("Outgoing message: " + jsonString);
                sendMessage(jsonString, "broadcast");
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
    public void chatMessage(String s){
        if (!s.isEmpty()) {
            //take a target_id from chat message
            String targetID;
            try {
                targetID = s.substring(0,s.indexOf(" "));
                s = s.substring(s.indexOf(" "));
                //check if target_id is a number
                Integer.parseInt(targetID);
            } catch (java.lang.NumberFormatException|java.lang.StringIndexOutOfBoundsException nfe){
                System.out.println("Incorrect format of message: put the target node id first and message after the space: '0123456 Hello'.");
                return;
            }
            try{
                Map<String, String> map = new HashMap<>();
                map.put("type", "CHAT");
                map.put("target_id", targetID);
                map.put("sender_id", nodeID);
                map.put("username", username);
                map.put("text", s);
                String jsonString = JSONValue.toJSONString(map);
                //System.out.println("Outgoing message: " + jsonString);
                sendMessage(jsonString, targetID);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
    public boolean joinMessage(){
        try {
             Map<String, String> map = new HashMap<>();
             map.put("type", "JOINING_NETWORK");
             map.put("sender_id", nodeID);
             map.put("ip_address", chatNode.getIp().toString().substring(1));
             String jsonString = JSONValue.toJSONString(map);
             //System.out.println("Join message sent: " + jsonString);
             sendMessage(jsonString, "join");

            } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return true;
    }
    public boolean leaveMessage(){
        try {
            Map<String, String> map = new HashMap<>();
            map.put("type", "LEAVING_NETWORK");
            map.put("sender_id", nodeID);
            map.put("ip_address", chatNode.getIp().toString().substring(1));
            String jsonString = JSONValue.toJSONString(map);
            sendMessage(jsonString, "broadcast");
            System.out.println("Sending leaving messages");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return true;
    }
    public void sendAddressList(String target_id){
        Map<String, Serializable> addressList = new HashMap<>();
        addressList.put("type", "ROUTING_INFO");
        addressList.put("gateway_id", nodeID);
        addressList.put("node_id", target_id);
        addressList.put("ip_address", chatNode.getIp().toString().substring(1));
        JSONArray idToIp = new JSONArray();
        for(Map.Entry<String, InetAddress> e : chatNode.getAddressList().entrySet()) {
            Map<String, String> line = new HashMap<>();
            line.put("node_id", e.getKey());
            line.put("ip_address", e.getValue().toString().substring(1));
            idToIp.add(line);
        }
        addressList.put("route_table", idToIp);
        sendMessage(JSONValue.toJSONString(addressList), target_id);
        //System.out.println("Address list " + idToIp.toJSONString() + "has sent");
    }
    public void broadcastAddress(String target_id, String target_ip){
        Map<String, Serializable> addressList = new HashMap<>();
        addressList.put("type", "ROUTING_INFO");
        addressList.put("gateway_id", nodeID);
        addressList.put("node_id", target_id);
        addressList.put("ip_address", chatNode.getIp().toString().substring(1));
        JSONArray idToIp = new JSONArray();
        Map<String, String> line = new HashMap<>();
        line.put("node_id", target_id);
        line.put("ip_address", target_ip);
        idToIp.add(line);
        addressList.put("route_table", idToIp);
        sendMessage(JSONValue.toJSONString(addressList), "broadcast");
    }
    public void sendMessage(String s, String targetId){
        try{
            sendData = s.getBytes();
            senderSocket = new DatagramSocket(0, chatNode.getIp());
            DatagramPacket sendPacket;
            switch (targetId) {
                case "broadcast":
                    for (InetAddress ipDest : chatNode.getAddressList().values()) {
                        sendPacket = new DatagramPacket(sendData, sendData.length, ipDest, portNumber);
                        senderSocket.send(sendPacket);
                    }
                    break;
                case "join":
                    sendPacket = new DatagramPacket(sendData,sendData.length, chatNode.getInitialIp(), portNumber);
                    senderSocket.send(sendPacket);
                    break;
                default:
                    InetAddress ipDest = chatNode.getAddressList().get(targetId);
                    sendPacket = new DatagramPacket(sendData, sendData.length, ipDest, portNumber);
                    senderSocket.send(sendPacket);
                    break;
            }
            senderSocket.close();
        }catch (IOException ioe){
            System.out.println("Something was wrong during the sending process: " + ioe.getMessage());
        }catch (NullPointerException npe){
            System.out.println("The target_id is not in the network or incorrect target_id");
        }
    }
}
