//Patrick Aduwo
//100962777
import java.net.*;
import java.util.*;

public class Server extends Thread{
  DatagramSocket socket;
  DatagramPacket packet;
  String name=null;
  
  Scanner scanner;
  
  List<Server> servers;
  
  Server(){
    try{
      //socket = new DatagramSocket(69);
      socket = new DatagramSocket(69);
      
      name = "mainServer";
      servers = new ArrayList<Server>();
      scanner = new Scanner(System.in);
    } catch (Exception e){
      System.out.println("Error in server constructor");
    }
  }
  Server(DatagramPacket packet){
    
    name = "notMainServer";
    try{
      //socket = new DatagramSocket(69);
      this.packet = packet;
    } catch(Exception e){
      System.out.println("Error in constructor 2");
    }
  }
  public void run(){
    try{
      if(name.equals("mainServer")){
        while(true){
          System.out.println("Number of server threads running: "+servers.size());
          System.out.println("What would you like to do: (shutdown) or (continue)");
          String answer = scanner.nextLine();
          //String answer = "continue";
          if(answer.toLowerCase().equals("shutdown")){
            System.out.println("Shuting down...");
            shutdown();
            break;
          } else if(answer.toLowerCase().equals("continue")){
          //} else if(answer.toLowerCase().equals("1")){
            receiveConnections();
          }
        }
      } else {
        handleConnections();
      }
    } catch(Exception e) {
      System.out.println("Error in run");
      if(e.getMessage().equals("Invalid request")){
        System.out.println("An invalid request was received, Closing sockets, bye");
        socket.close();
      } else {
        System.out.println("Error wasn't because of an invalid request");
        e.printStackTrace();
        System.exit(1);
      }
      //e.printStackTrace();
      //System.exit(1);
    }
    
  }
  void handleConnections(){
    try{
      System.out.println("Handling connections....");
      if(packet==null) return;
      
      boolean valid = check(packet.getData());
      byte[] b1 = packet.getData();
      
      byte[] replyBytes = new byte[2];
      DatagramPacket reply ;
      try{
        //1 0 for RRQ
        //0 0 for WRQ
        if(valid){
          if((b1[0] == 0) && (b1[1]==1)){
            //send 1 0
            replyBytes[0] = 1;
            replyBytes[1] = 0;
          } else if((b1[0] == 0) && (b1[1] ==2)){
            //send 0 4 0 0
            replyBytes[0] = 0;
            replyBytes[1] = 0;
          }
        } else {
          Exception e = new Exception("Invalid Request");
          throw e;
        }
      } catch (Exception e){
        
      }
      
      System.out.println("Server: Sending the response to the ErrorSimulator");
      reply = new DatagramPacket(replyBytes, 2, packet.getAddress(), packet.getPort());
      //print(reply);//prints the response packet informatione
      DatagramSocket responseSocket = new DatagramSocket();
      
      Thread.sleep(3000);
      
      responseSocket.send(reply);
      System.out.println("Closing sockets, bye");
      responseSocket.close();
    } catch (Exception e){
      
    }
  }
  void receiveConnections(){
    try{
      byte[] b = new byte[20];
      packet = new DatagramPacket(b, 20);
      
      //The server waits to receive a packet from the intermediate host
      System.out.println("\n=========================================================");
      System.out.println("Server: Waiting for Packet from the ErrorSimulator");
      socket.receive(packet);
      //System.out.println(packet.getPort());
      System.out.println("Packet received");
      
      //create a new server and pass it the packet
      Server s = new Server(packet);
      servers.add(s);
      s.start();
      
    } catch (Exception e){
      System.out.println("Error in receiveConnections");
    }
  }
  //Functions checks if the message if valid, if it's valid returns true, otherwise returns false
  boolean check(byte[] received){
    if(received[0]!=0){
      System.out.println("Invalid request");
      return false;
    } else if((received[1]!=1) && (received[1]!=2)){
      System.out.println("Invalid request");
      
      return false;
    }
    //get the file name
    int i =2;
    while(received[i]!=0){
      i++;
    }
    
    int size = i-2;
    //System.out.println(size);
    byte[] b1 = new byte[size];
    int j = 2;
    
    for(int ii = 0;ii < size; ii++){
      b1[ii] = received[j];
      j++;
    }
    if(received[i]!=0) return false;
    i++;
    j=i;
    size=0;
    //get the mode
    while(received[i]!=0){
      i++;
      size++;
    }
    byte[] b2 = new byte[size];
    for(int ii =0;ii<size;ii++){
      b2[ii]=received[j];
      j++;
    }
    //String str1 = new String(b2);
    //System.out.println("Mode: "+str1);
    if(received[i]!=0) return false;
    return true;
    
  }
  
  void shutdown(){
    try{
      for(Server s: servers){
        s.join();
      }
    } catch(Exception e){
      System.out.println("Error in shtudown function");
    }
  }
  public static void main(String[] args){
    Server server = new Server();
    server.start();
  }
  
}
