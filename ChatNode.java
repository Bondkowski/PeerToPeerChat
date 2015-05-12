package Chat2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class ChatNode {
    private static String nodeID;
    private static boolean work = true;
    private static String username;
    private static Receiver receiver;
    private static Sender sender;
    private static HashMap<String, InetAddress> addressList;
    private static BufferedReader inFromUser;
    private static InetAddress localIp;
    private static InetAddress initialIp;


    public ChatNode(){
        addressList = new HashMap<>();
    }
    public String getUsername(){
        return username;
    }
    public String getNodeID(){
        return nodeID;
    }
    public InetAddress getIp(){
        return localIp;
    }
    public InetAddress getInitialIp(){
        return initialIp;
    }
    public Sender getSender(){
        return sender;
    }
    public HashMap<String,InetAddress> getAddressList(){
        return addressList;
    }
    public void addAddressToList(String id, String ip){
        try {
                addressList.put(id, InetAddress.getByName(ip));

            } catch (java.net.UnknownHostException uhe) {
                System.out.println("Incorrect address sent by: " + id + ip.toString().substring(1));
            }
        }
    public void removeAddressFromList(String id){
        addressList.remove(id);
    }
    public static void defineIp(){
        //this method shows to user all IP's of his computer in comfortable way and ask to choose one of them,
        // then assignees chosen one to the field
        try {
            InetAddress list[] = InetAddress.getAllByName(InetAddress.getLocalHost().getHostName());
            System.out.println("Which address you want to use? Enter the number...");
            for (int i = 0; i<list.length; i++)
                System.out.println(i + ":  " + list[i].toString().substring(InetAddress.getLocalHost().getHostName().length()).substring(1));
            int i = Integer.parseInt(inFromUser.readLine());
            localIp = InetAddress.getByName(list[i].toString().substring(InetAddress.getLocalHost().getHostName().length()).substring(1));
            System.out.println("You chose: " + localIp.toString().substring(1));
        }catch (java.net.UnknownHostException uhe){
            System.out.println("Something is wrong with IP address: " + uhe.getMessage());
        }catch (IOException ioe){
            System.out.println("Enter the number only! "+ ioe.getMessage());
        }
    }
    public static void defineInitialIp(){
        try {
            System.out.println("Enter initial IP address");
            initialIp = InetAddress.getByName(inFromUser.readLine());
            System.out.println("Trying to connect to: " + initialIp.toString().substring(1));
        }catch (IOException ioe){
            System.out.println("Enter the initial IP");
        }
    }
    public static void printAddressList(){
        System.out.println("Next users are currently online:");
        for(Map.Entry<String, InetAddress> e : addressList.entrySet()){
            System.out.println("ID: " + e.getKey() +"   IP address: "+ e.getValue().toString().substring(1));
        }
    }
    public static void stop() throws Exception{
        //we stop receiver and wait for it to stop
        receiver.finish();
        receiver.join();
        //send leaving message to make everyone to delete this node from their addressLists
        //I am using boolean to make this method wait until remote method finish to send messages
        if(sender.leaveMessage())
        work = false;
        System.out.println("Bye!");
    }

    public static void main(String args[]) throws Exception{
        //create a chatNode instance to be able to reference it from Receiver and Sender
        ChatNode chatNode = new ChatNode();
        //Need to check out is 1024 is enough and what is biggest possible to use
        inFromUser = new BufferedReader(new InputStreamReader(System.in));

        //initializing Username
        username = "";
        while(username.equals("")){
            System.out.println("Enter username: ");
            username = inFromUser.readLine();
        }

        //creating nodeID
        Random r = new Random();
        nodeID = Integer.toString(r.nextInt(10000000));
        System.out.println("Your node ID is: " + nodeID);

        //creating and starting Receiver and Sender
        System.out.println("Starting Receiver");
        receiver = new Receiver(chatNode);
        receiver.start();
        System.out.println("Starting Sender");
        sender = new Sender(chatNode);

        //define IP of the node by let user to pick one
        defineIp();

        //take an initial address and send message to it
        defineInitialIp();

        //add node to addressList
        addressList.put(nodeID, localIp);

        //send joining message
        sender.joinMessage();

        while (work) {

            if (inFromUser.ready()) {
                String message = inFromUser.readLine();
                if (message.equals("---exit"))
                    stop();
                else if (message.startsWith("---all")){
                    sender.chatBroadcast(message);
                    message = message.substring(6);
                    //System.out.println("Me: " + message);
                }
                else if(message.equals("---list")){
                    printAddressList();
                }
                else if (!message.isEmpty()) {
                    sender.chatMessage(message);
                    //System.out.println("Me: " + message);
                }
            }
        }

    }
}
