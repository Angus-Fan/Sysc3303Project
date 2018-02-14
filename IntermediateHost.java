//Group 8
import java.net.*;
import java.util.*;
public class ErrorSimulator{
  
  DatagramSocket receiveSocket;//socket for receiving
  DatagramSocket SARSocket;//socket for sending and receiving
  DatagramPacket message;
  
  
  ErrorSimulator(){
  }
  void run(){
    try {
      //repeat forever
      while(true){
        receiveSocket = new DatagramSocket(23);
        SARSocket = new DatagramSocket();
        
        System.out.println("\n====================================================================");
        System.out.println("Intermediate host: initially waiting to receive a request from the client");
        byte[] b1 = new byte[516];
        message = new DatagramPacket(b1,516);
        receiveSocket.receive(message);//wait to receive a message
        System.out.println("message received");
        
        String str = new String(message.getData());
        
        //print(str);
        //create a packet to send containing exactly what you received
        InetAddress address = InetAddress.getByName("127.0.0.1");
        DatagramPacket packet = new DatagramPacket(message.getData(), message.getLength(), address, 69);
        System.out.println("ErrorSimulator: Sending a packet containing exactly what it received to the Server"); 
        Thread.sleep(3000);
        SARSocket.send(packet);//sends to the server
        
        byte [] b2 = new byte[516];
        DatagramPacket packet1 = new DatagramPacket(b2, 516);
        
        System.out.println("ErrorSimulator: Waiting for the server ");
        SARSocket.receive(packet1);
        
        
        System.out.println("ErrorSimulator: printing out information received from the Server");
        str = new String(packet1.getData());
        print(str);
        DatagramPacket packet2 = new DatagramPacket(packet1.getData(),packet1.getLength(),
                                                    message.getAddress(),message.getPort());
        DatagramSocket sendToClient = new DatagramSocket();
        System.out.println("ErrorSimulator: sending packet to Client");
        Thread.sleep(3000);
        sendToClient.send(packet2);
        
        System.out.println("Closing sockets");
        sendToClient.close();
        receiveSocket.close();
        SARSocket.close();
        System.out.println("===================================================================");
      }
    } catch (Exception e){
      System.out.println("Error in run");
      //e.printStackTrace();
      System.exit(1);
    }
    
  }
  void print(String str){
    //print out the information you're about to send
    System.out.print("Message as text: ");
    System.out.println(str);//print as a string
    
    System.out.print("Message as binary: ");
    byte [] b = str.getBytes();
    System.out.println(Arrays.toString(b));//print as a binary
    System.out.println();
  }
  
  public static void main(String [] args){
    ErrorSimulator errorSimulator = new ErrorSimulator();
    errorSimulator.run();
  }
  
}
