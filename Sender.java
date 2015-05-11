package Chat2;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.LinkedHashMap;
import java.util.Map;


public class Sender {

    private InetAddress ip;
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
    }

    public void chatMessage(String s){
        if (!s.isEmpty()) {
            try {
                Map<String, String> map = new LinkedHashMap<>();
                map.put("type", "CHAT");
                map.put("sender_id", nodeID);
                map.put("username", username);
                map.put("text", s);
                String jsonString = JSONValue.toJSONString(map);
                //System.out.println("Outgoing message: " + jsonString);
                sendMessage(jsonString);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
    public boolean joinMessage(String id,InetAddress ip){
        if (!id.isEmpty()) {
            nodeID = id;
            try {
                Map<String, String> map = new LinkedHashMap<>();
                map.put("type", "JOINING_NETWORK");
                map.put("sender_id", nodeID);
                map.put("ip_address", ip.toString().substring(1));
                String jsonString = JSONValue.toJSONString(map);
                System.out.println(jsonString);
                senderSocket = new DatagramSocket(0, chatNode.getIp());
                sendData = jsonString.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length, ip, portNumber);
                senderSocket.send(sendPacket);
                senderSocket.close();
                return true;
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        return false;
    }
    public void sendLeaveMessage(){

    }
    public void sendMessage(String s){
        try{
        sendData = s.getBytes();
        senderSocket = new DatagramSocket(0, chatNode.getIp());
        for(InetAddress ipDest: chatNode.getAddressList().values()) {
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipDest, portNumber);
            senderSocket.send(sendPacket);
        }
        senderSocket.close();
        }catch (IOException ioe){
            System.out.println("Something was wrong during the sending process: " + ioe.getMessage());
        }
    }
}
