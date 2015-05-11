package Chat2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.HashMap;
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
    public HashMap<String,InetAddress> getAddressList(){
        return addressList;
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

    public static void stop(){
        receiver.finish();
        sender.sendLeaveMessage();
        System.out.println("Bye!");
        System.exit(1);
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
        //creating and starting Receiver and Sender
        System.out.println("Starting Receiver");
        receiver = new Receiver(chatNode);
        receiver.start();
        System.out.println("Starting Sender");
        sender = new Sender(chatNode);
        //define IP of the node by let user to pick one
        defineIp();
        //take an initial address and send message to it
        System.out.println("Enter initial IP address");
        InetAddress initialIP = InetAddress.getByName(inFromUser.readLine());
        System.out.println("Trying to connect to: " + initialIP.toString().substring(1));
        //creating nodeID
        Random r = new Random();
        nodeID = Integer.toString(r.nextInt(10000000));
        System.out.println("Your node ID is: " + nodeID);

        //temporary!!!

        addressList.put("111111", InetAddress.getByName("ec2-52-24-242-40.us-west-2.compute.amazonaws.com"));
        addressList.put("222222", InetAddress.getByName("ec2-52-24-243-189.us-west-2.compute.amazonaws.com"));
        addressList.put(nodeID, localIp);


        //notify service if joining fails
        if(!sender.joinMessage(nodeID, initialIP)) {
            System.out.println("You are alone in the network(this world?)... Wait for somebody to connect to you.");
        }

        while (work) {

            if (inFromUser.ready()) {
                String message = inFromUser.readLine();
                if(message.equals("---exit"))
                    stop();
                else if (!message.isEmpty()) {
                    sender.chatMessage(message);
                    System.out.println((char) 27 + "[31mMe: " + (char) 27 + "[0m" + message);
                }
            }
        }

    }
}
