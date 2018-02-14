//Group 8

/*
 * The server class accepts connections on part 69, when it receives a connection
 * it creates a thread to handle that connection. The thread the server creates
 * then sends back a response to the client and then closes.
 * 
 * */
import java.net.*;
import java.util.*;
import java.io.*;

public class Server extends Thread{
  DatagramSocket socket;
  
  DatagramPacket packet;
  String name = null;
  Scanner scanner;
  
  byte index1 = 0;
  byte index2 = 0;
  
  //the path to access the file from, fill free to change this
  String path = "C:/Users/Patrick/Documents/Courses/Sysc3303/Project/iteration2/Code/";
  
  List<Server> servers;
  
  Server(){
    try{
      socket = new DatagramSocket(69);;
      
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
      this.packet = packet;
    } catch(Exception e){
      System.out.println("Error in constructor 2");
    }
  }
  /*
   * This function is executed by both the main server and the threads created by the main server
   * "client connection threads"
   * */
  public void run(){
    try{
      if(name.equals("mainServer")){
        //while(true){
          System.out.println("Number of server threads created so far: "+servers.size());
          System.out.println("What would you like to do: (shutdown) or (run)");
          //String answer = scanner.nextLine();
          String answer = "run";
          if(answer.toLowerCase().equals("shutdown")){
            System.out.println("Shuting down...");
            shutdown();
            //break;
          } else if(answer.toLowerCase().equals("run")){
            receiveConnections();
          }
        //}
      } else {
        handleConnections();
      }
    } catch(Exception e) {
      System.out.println("Error in run");
      //e.printStackTrace();
      //System.exit(1);
    }
    
  }
  /*
   * This is the function used by the server threads to handle requests. It sends back a response to
   * the server
   * 
   * */
  synchronized void handleConnections(){
    try{
      //System.out.println("Handling connections....");
      if(packet==null) return;
      
      byte[] b1 = packet.getData();//type of request
      
      
      //read request
      if((b1[0] == 0) && (b1[1]==1)){
        //send 0 3 0 1
        handleReadRequest();
        
        //write request
      } else if((b1[0] == 0) && (b1[1] ==2)){
        //send 0 4 0 1
        handleWriteRequest();//--
      } else {
        System.out.println("Invalid format"); 
      }
      
    } catch (Exception e){
      System.out.println("Error in one of the thread's handling function");
    }
  }
  boolean ackValid(byte [] response){
    if((response[0]==0) && (response[1]==4)) return true;
    
    return false;
  }
  //handle the read request, get the file from the server
  synchronized void handleReadRequest(){
    try{
      DatagramSocket socket1 = new DatagramSocket();
      byte[] b1 = new byte[512];
      
      String filename = getFileName(packet.getData());
      
      //System.out.println("Filename: "+filename);
      File file = new File(path+filename);
      //System.out.println("File");
      //
      
      //Trying to read from a file that doesn't exists
      if(file.exists()==false){
        //return an error packet
        b1[0]=0;
        b1[1]=5;
        b1[2]=0;
        b1[3]=1;
        System.out.println("Server: sending an error packet");
        DatagramPacket packet1 = new DatagramPacket(b1,512,packet.getAddress(),packet.getPort());
        Thread.sleep(3000);
        socket1.send(packet1);
        
        socket1.close();
        return;
      }
      
      FileInputStream fi = new FileInputStream(file);
      //send the data
      while(true){
        b1 = new byte[516];
        //set the data block
        b1[0] = 0;
        b1[1] = 3;
        //set the block number
        b1[2] = index1;
        b1[3] = index2;
        
        byte[] b2 = new byte[512];
        //read the file
        fi.read(b2);
        for(int i =0;i<512;i++){
          b1[i+4]=b2[i];
        }
        //send the data
        System.out.println("Server: Sending data packet");
        InetAddress address = InetAddress.getByName("127.0.0.1");
        DatagramPacket packet1= new DatagramPacket(b1,516,address,69);
        Thread.sleep(4000);
        //print(b1);
        socket1.send(packet1);
        
        boolean checkSize1 = checkSize(b1);
        //System.out.println("CheckSize1: "+checkSize1);
        
        //receive the acknowledgement
        byte[] b3 = new byte[516];
        DatagramPacket packet2 = new DatagramPacket(b3, 516);
        System.out.println("Server: Receving acknowledgement packet");
        //socket1.receive(packet2);
        
        socket1.receive(packet2);
        
        System.out.println("Server: Acknowledgement recevied");
        byte[] b4 = packet.getData();
        //client has sent back an acknowledgement
        if(b4[0]==0 && b4[1]==4){
          if(index1==9){
            index2+=1;
            index1=0;
          } else {
            ++index2;
          }
        }
        //check if the number of bytes sent was less than 512 bytes
        if(checkSize1==false){
          fi.close();
          socket1.close();
          break;
        }
        System.out.println("CheckSize1");
      }
      
      
    } catch (Exception e){
      System.out.println("Error in handle readRequest");
    }
    
  }
  synchronized boolean checkSize(byte[] b){
    for(int i =4;i<516;i++){
      if(b[i]==0){
        System.out.println("b[i]: "+b[i]);
        return false;
      }
    }
    return true;
  }
  //handle the write request
  synchronized void handleWriteRequest(){
    try{
      DatagramSocket socket1 = new DatagramSocket();
      //send back a an acknowledgement
      String filename = getFileName(packet.getData());
      byte[] b1 = new byte[516];
      
      
      File file = new File(path+filename);
      if(!file.exists()){
        file.createNewFile();
      }
      if(file.canWrite()==false){
        //Access violation
        b1[0]=0;
        b1[0]=5;
        b1[0]=0;
        b1[0]=2;
        
      } else {
        b1[0]=0;
        b1[1]=4;
        b1[2]=index1;
        b1[3]=index2;
      }
      
      DatagramPacket packet1 = new DatagramPacket(b1,516,packet.getAddress(),packet.getPort());
      Thread.sleep(3000);
      socket1.send(packet1);
      
      //Trying to write to a read only file
      if(file.canWrite()==false){
        return;
      }
      while(true){
        //wait for data
        break;
      }
      
    } catch(Exception e){
      
    }
  }
  
  //gets the name of the file
  String getFileName(byte [] received){
    //byte[] received = packet.getData();
    int i =2;
    while(received[i]!=0){
      i++;
    }
    int size = i-2;
    byte[] b1 = new byte[size];
    int j = 2;
    
    for(int ii = 0;ii < size; ii++){
      b1[ii] = received[j];
      j++;
    }
    String n = new String(b1);
    return n;
  }
  /*
   * This function is only executed by the main server. It is used to recieve
   * messages from the Error simulator
   * */
  void receiveConnections(){
    try{
      byte[] b = new byte[516];
      packet = new DatagramPacket(b, 516);
      
      socket.receive(packet);
      
      //create a new server and pass it the packet
      Server s = new Server(packet);
      servers.add(s);
      s.start();
      
    } catch (Exception e){
      System.out.println("Error in receiveConnections");
    }
  }
  /*
   * This function checks if the message received is valid
   * */
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
    String n = new String(b1);
    //System.out.println("Filename: "+n);
    if(received[i]!=0) return false;
    return true;
  }
  /*
   * This function waits for all the threads created by the server
   * to finish before it shutsdown
   * */
  void shutdown(){
    try{
      for(Server s: servers){
        s.join();
      }
      socket.close();
    } catch(Exception e){
      System.out.println("Error in shtudown function");
    }
  }
  public static void main(String[] args){
    Server server = new Server();
    server.start();
  }
  
}
