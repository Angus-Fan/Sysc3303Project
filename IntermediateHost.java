//Group 8
import java.net.*;
import java.util.*;
public class ErrorSimulator extends Thread{
  
  DatagramSocket receiveSocket;//socket for receiving
  DatagramSocket SARSocket = null;//socket for sending and receiving
  
  DatagramPacket message = null;
  List<ErrorSimulator> errorSimulators;
  String name = "";
  
  DatagramSocket sendToClient = null;
  
  int clientPort = -2;
  int serverPort = -2;
  boolean loop = true;
  
  //constructor for the main error Simulator
  ErrorSimulator(){
    try{
      name = "main errorSimulator";
      errorSimulators = new ArrayList<ErrorSimulator>();
      receiveSocket = new DatagramSocket(23);
    } catch (Exception e){
      System.out.println("Error in constructor");
    }
  }
  //constructor for the client handling error simulator
  ErrorSimulator(DatagramPacket message1){
    this.message = message1;
    name = "not main errorSimulator";
    //initialize the appropriate sockets
    try{
      sendToClient = new DatagramSocket();
      SARSocket = new DatagramSocket();
      
    } catch(Exception e){
      System.out.println("Error in creating sendToClient socket");
    }
  }
  
  //run function for the error simulator
  public void run(){
    //repeat forever
    if(name.equals("main errorSimulator")){
      while(loop){
        
        System.out.println("\n====================================================================");
        System.out.println("main ErrorSimulator: initially waiting to receive a request from the client");
        byte[] b1 = new byte[516];
        DatagramPacket message1 = new DatagramPacket(b1,516);
        try{
          receiveSocket.receive(message1);//wait to receive a message
        } catch(Exception e){
          System.out.println("Error in receiving a datagramPacket");
        }
        System.out.println("read request received");
        
        handleConnections(message1);
        
      }
      //Client handling threads
    } else if (name.equals("not main errorSimulator")) {
      System.out.println("not main errorSimulator: init");
      //message: request doesn't change
      
      String str = new String(message.getData());
      System.out.println("not main errorSimulator: printing packet");
      print(str);
      clientPort = message.getPort();//this is the client's port
      InetAddress address = null;
      
      try{
        //create a packet to send containing exactly what you received
        address = InetAddress.getByName("127.0.0.1");
      } catch(Exception e){
        System.out.println("error in init address");
      }
      //send to part 69 (server)
      boolean check = checkRequest(message.getData());
      
      DatagramPacket packet = null;
      //send to the server
      if(check==true){
        System.out.println("not main errorSimulator: initially sending request to server");
        //it's a request, send to port 69 (main server)
        packet = new DatagramPacket(message.getData(), message.getLength(), address, 69);
      } else {
        System.out.println("not main errorSimulator: sending packet (Data/ack) to server");
        //send to the server thread handling this request
        packet = new DatagramPacket(message.getData(), message.getLength(), address, serverPort);
      }
      
      try{
        Thread.sleep(1000);
        //initialy sends to the main server, then later sends to the client handling thread
        SARSocket.send(packet);
      } catch(Exception e){
        System.out.println("Error in Thread.sleep/sending a packet");
      }
      
      //-----interaction----while loop --------------------------------------------------
      while(true){
        //receive from server
        
        byte [] b2 = new byte[516];
        DatagramPacket receivedFromServer = new DatagramPacket(b2, 516);
        
        System.out.println("ErrorSimulator: Waiting for the server ");
        try{
          SARSocket.receive(receivedFromServer);
          System.out.println("non main errorSimulator: printing...");

        } catch(Exception e){
          System.out.println("Error in receiving a packet");
        }
        //get server port
        serverPort = receivedFromServer.getPort();
        //The server thread that replied
        //System.out.println("ServerPort "+ serverPort);
        
        
        System.out.println("ErrorSimulator: printing out information received from the Server");
        str = new String(receivedFromServer.getData());
        print(str);
        DatagramPacket sendPacketToClient = new DatagramPacket(receivedFromServer.getData(),receivedFromServer.getLength(),
                                                               message.getAddress(),message.getPort());
        loop = checkSize(message.getData());
        
        //socket to send back to the client
        //System.out.println("message port: "+message.getPort());
        
        try{
          System.out.println("ErrorSimulator: sending packet to Client");
          Thread.sleep(1000);
          System.out.println("ErrorSimulator: Printing...");
          str = new String (receivedFromServer.getData());
          print(str);
          
          sendToClient.send(sendPacketToClient);
          
        } catch(Exception e){
          System.out.println("Error in Thread.sleep/sending packet");
        }
        //receive from client
        System.out.println("errorSimulator: waiting for packet from client");
        
        byte[] b3 = new byte[516];
        DatagramPacket packetFromClient = new DatagramPacket(b3,516);
        try{
          sendToClient.receive(packetFromClient);
        } catch(Exception e){
          System.out.println("Error receiving to client");
        }
        
        str = new String(packetFromClient.getData());
        print(str);
        
        System.out.println("errorSimulator: received packet from client");
        System.out.println("errorSimulator: sending packet to server");
        DatagramPacket sendPacketToServer = new DatagramPacket(packetFromClient.getData(), packetFromClient.getLength(),
                                                               receivedFromServer.getAddress(), receivedFromServer.getPort());
        
        
        str = new String (packetFromClient.getData());
        print(str);
        try{
          
          
          SARSocket.send(sendPacketToServer);
        } catch(Exception e){
          System.out.println("Error sending to server");
        }
        //if(loop == false) break;
        
        
        System.out.println("===================================================================");
      }
    }
    shutdown();
    
  }
  
  boolean checkRequest(byte[] data){
    if((data[0]==0 && data[1]==1) || (data[0]==0 && data[1]==2)) return true;
    
    return false;
  }
  //function handles client connections
  void handleConnections(DatagramPacket message){
    ErrorSimulator e = new ErrorSimulator(message);
    errorSimulators.add(e);
    e.start();
    
    
  }
  
  //shutdown the sockets
  private void shutdown(){
    System.out.println("Closing socket");
    SARSocket.close();
    sendToClient.close();
    
    //wait for the other errorSimulators to finish
    for(ErrorSimulator e: errorSimulators){
      try{
        e.join();
      } catch(Exception ee){
        System.out.println("Error in shuting down");
      }
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
  //check the size of the data being sent
  boolean checkSize(byte[] data){
    if(data[0]==0 && data[1]==4) return true;//ACK message
    if(data[0]==0 && data[1]==5) return true;//error message
    
    for(int i =4;i<512;i++){
      if(data[i]==0) return false;
    }
    return true;
  }
  
  public static void main(String [] args){
    ErrorSimulator errorSimulator = new ErrorSimulator();
    errorSimulator.start();
  }
  
}
