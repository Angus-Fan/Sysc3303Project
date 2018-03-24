//Group 8
import java.net.*;
import java.util.*;
public class ErrorSimulator extends Thread{
  
  DatagramSocket receiveSocket;//socket for receiving
  DatagramSocket SARSocket = null;//socket for sending and receiving
  
  DatagramPacket message = null;
  List<ErrorSimulator> errorSimulators;
  String name = "";
  String numAnswer = "";
  
  DatagramSocket sendToClient = null;
  
  int clientPort = 0;
  int serverPort = 69;
  boolean loop = true;
  //
  Scanner scanner = null;
  
  //constructor for the main error Simulator
  ErrorSimulator(){
    try{
      name = "main errorSimulator";
      errorSimulators = new ArrayList<ErrorSimulator>();
      receiveSocket = new DatagramSocket(23);
      scanner = new Scanner(System.in);
    } catch (Exception e){
      System.out.println("Error in constructor");
    }
  }
  //constructor for the client handling error simulator
  ErrorSimulator(DatagramPacket message1){
    this.message = message1;
    name = "not main errorSimulator";
    scanner = new Scanner(System.in);
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
      System.out.println("What would you like to do:");
      System.out.println("0 - normal operation, 1 - Invalid TFTP opcode on WRQ or RRQ, 2 - Invalid mode");
      //numAnswer = scanner.nextLine().toLowerCase();
      numAnswer = "2";
      System.out.println(numAnswer);
      
      if(numAnswer.equals("0")){
        System.out.println("Normal operation selected");
      } else if(numAnswer.equals("1")){
        System.out.println("ErrorSimulator: Invalid TFTP opcode on RRQ or WRQ");
      } else if(numAnswer.equals("2")){
        System.out.println("ErrorSimulator: Invalid mode selected");
        
      } else {
        System.out.println("Please try again");
      }
      
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
      // boolean check = checkRequest(message.getData());
      
      DatagramPacket packet = null;
      //send to the server
      
      System.out.println("not main errorSimulator: sending packet to server");
      //send to the server thread handling this request
      packet = new DatagramPacket(message.getData(), message.getLength(), address, serverPort);
      
      
      try{
        Thread.sleep(1000);
        //initialy sends to the main server, then later sends to the client handling thread
        SARSocket.send(packet);
      } catch(Exception e){
        System.out.println("Error in Thread.sleep/sending a packet");
      }
      
      //-----interaction----while loop --------------------------------------------------
      boolean eLoop = true;
      
      while(eLoop){
        //receive from server
        
        byte [] b2 = new byte[516];
        DatagramPacket receivedFromServer = new DatagramPacket(b2, 516);
        
        System.out.println("not main ErrorSimulator: Waiting for the server ");
        try{
          SARSocket.receive(receivedFromServer);
          System.out.println("not main errorSimulator: printing...");
          
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
        
        System.out.println(eLoop);
        eLoop = checkSize(message.getData());
        if(eLoop==false){
          System.out.println("Size");
          System.out.println("eLoop: "+eLoop);
          break;
        }
        eLoop = checkOpCode(sendPacketToClient.getData());
        if(eLoop==false){
          System.out.println("not main ErrorSimulator: an errorPacket was sent");
          break;
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
        //print(str);
        //---------------------

        //-----------------------------
        
        System.out.println("errorSimulator: received packet from client");
        eLoop = checkOpCode(packetFromClient.getData());
        System.out.println("errorSimulator: sending packet to server");
        DatagramPacket sendPacketToServer = new DatagramPacket(packetFromClient.getData(), packetFromClient.getLength(),
                                                               receivedFromServer.getAddress(), receivedFromServer.getPort());
        
        
        str = new String (packetFromClient.getData());
        //print(str);
        try{
          SARSocket.send(sendPacketToServer);
        } catch(Exception e){
          System.out.println("Error sending to server");
        }
        //---------------------
        eLoop = checkSize(message.getData());
        if(eLoop == false){
          System.out.println("eLoop: "+eLoop);
          break;
        }
        eLoop = checkOpCode(sendPacketToClient.getData());
        if(eLoop == false){
          System.out.println("not main ErrorSimulator: an errorPacket was sent");
          break;
        }
        //-----------------------------
        
        
        System.out.println("===================================================================");
      }
    }
    shutdown();
    
  }
  
  boolean checkOpCode(byte[] data){
    if(data[0]==0 && data[1]==5) return false;
    return true;
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
    if(name.equals("not main ErrorSimulator")){
      SARSocket.close();
      sendToClient.close();
    }
    
    if(name.equals("main errorSimulator")){
      receiveSocket.close();
      //wait for the other errorSimulators to finish
      for(ErrorSimulator e: errorSimulators){
        try{
          e.join();
        } catch(Exception ee){
          System.out.println("Error in shuting down");
        }
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
  private boolean checkSize(byte[] data){
    if(data[0]==0 && data[1]==4) return true;//ACK message
    if(data[0]==0 && data[1]==5) return true;//error message
    
    for(int i =4;i<512;i++){
      
      if(data[i]==0){
        System.out.println(i+" "+data[i]);
        return false;
        
      }
    }
    return true;
  }
  
  public static void main(String [] args){
    ErrorSimulator errorSimulator = new ErrorSimulator();
    errorSimulator.start();
  }
  
}
