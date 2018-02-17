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
  DatagramSocket socket = null;
  
  DatagramPacket packet = null;
  String name = null;
  Scanner scanner = null;
  boolean keepLooping = true;
  
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
    DatagramSocket socket1 = null;
    InetAddress address = null;
    
    try{
      socket1 = new DatagramSocket();
    } catch (Exception e){
      System.out.println("Error in socket1 init");
    }
    byte[] b1 = new byte[512];
    
    String filename = getFileName(packet.getData());
    
    //System.out.println("Filename: "+filename);
    File file = new File(path+filename);
    //System.out.println("File");
    //
    
    //Trying to read from a file that doesn't exists  --- 
    if(file.exists()==false){
      //return an error packet
      b1 = errorPacket(1);
      
      System.out.println("Server: sending an error packet");
      
      try{
        
        address = InetAddress.getByName("127.0.0.1");
      } catch (Exception e){
        System.out.println("Init address");
      }
      
      DatagramPacket packet1 = new DatagramPacket(b1,512,address,23);
      try{
        Thread.sleep(3000);
      } catch (Exception e){
        
      }
      try{
        socket1.send(packet1);
      } catch (Exception e){
        
      }
      
      socket1.close();
      return;
    }
    FileInputStream fi=null;
    try{
      fi= new FileInputStream(file);
    } catch (Exception e){
      
    }
    
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
      try{
        //read the file
        fi.read(b2);
      } catch (Exception e){
        
      }
      for(int i =0;i<512;i++){
        b1[i+4]=b2[i];
      }
      //send the data
      System.out.println("Server: Sending data packet");
      try{
        address = InetAddress.getByName("127.0.0.1");
      } catch (Exception e){
        System.out.println("");
      }
      DatagramPacket packet1= new DatagramPacket(b1,516,address,23);
      try{
        Thread.sleep(8000);
      } catch (Exception e){
        
      }
      //print(b1);
      //------------------------------------------------------
      //variable used to check the size of the file
      boolean checkSize1 = true;
      //while(keepLooping){
      try{
        System.out.println("Sending packet");
        socket1.send(packet1);
      } catch (IOException ee){
        System.out.println("");
      }
      
      checkSize1 = checkSize(b1);
      //System.out.println("CheckSize1: "+checkSize1);
      
      //receive the acknowledgement
      byte[] b3 = new byte[516];
      DatagramPacket packet2 = new DatagramPacket(b3, 516);
      System.out.println("Server: Receving acknowledgement packet");
      //socket1.receive(packet2);
      //socket.setSoTimeout(3000);
      try{
        socket1.setSoTimeout(4000);
        socket1.receive(packet2);
      } catch (SocketException e){
        System.out.println("Timeout reached");
        keepLooping=false;
      } catch (Exception e){
        System.out.println("receving packet");
      }
      //}
      
      //---------------------------------------------------
      
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
        try{
          fi.close();
          socket1.close();
        } catch (Exception e){
          System.out.println("Closing socket");
        }
        break;
      }
      System.out.println("CheckSize1");
    }
    
    
    //} catch (Exception e){
    //  System.out.println("Error in handle readRequest");
    //}
    
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
    DatagramSocket socket1 = null;
    try{
      socket1 = new DatagramSocket();
    } catch (Exception e){
      
    }
    //send back a an acknowledgement
    String filename = getFileName(packet.getData());
    byte[] b1 = new byte[516];
    
    File file = new File(path+filename);
    if(!file.exists()){
      try{
        file.createNewFile();
      } catch (Exception e){
        
      }
    }
    if(file.canWrite()==false){
      //Access violation
      b1 = errorPacket(2);
      
    } else {
      b1[0]=0;
      b1[1]=4;
      b1[2]=index1;
      b1[3]=index2;
    }
    
    DatagramPacket packet1 = new DatagramPacket(b1,516,packet.getAddress(),23);
    try{
      Thread.sleep(5000);
      socket1.send(packet1);
    } catch (Exception e){
      
    }
    
    //Trying to write to a read only file
    if(file.canWrite()==false){
      return;
    }
    while(true){
      //wait for data
      break;
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
  //function: errorPacket
  //in: error number
  //out: byte[] to send back
  //desc: takes input for the number and creates a proper errorMsg bytes
  //ERROR PACKET FOLLOWS THIS (OP CODE (2bytes), ERR CODE (2bytes), ERR MSG (string),0 byte)
  public byte[] errorPacket(int errNum){
    int errorMsgLength; 
    byte[] errorMsgBytes;
    byte[] errMsg = new byte[0];
    String errorString;
    
    if(errNum == 1) {
      //filenotfound (might want to add the file name to the parameters)
      errorString = "The file you requested was not found"; //like put the file name here
      errorMsgBytes = errorString.getBytes();
      errorMsgLength = (errorString.getBytes().length);
      errMsg = new byte[errorMsgLength+5];
      errMsg[0]=(byte)0;
      errMsg[1]=(byte)5;
      errMsg[2]=(byte)0;
      errMsg[3]=(byte)1;
      
      System.arraycopy(errorMsgBytes,0,errMsg,4,errorMsgBytes.length);
      errMsg[errMsg.length-1]=(byte)0;
      
    }
    if(errNum == 2) {
      //AccessViolation (might want to add the file name to the parameters)
      errorString = "You do not have the permission to access this file"; //like put the file name here
      errorMsgBytes = errorString.getBytes();
      errorMsgLength = (errorString.getBytes().length);
      errMsg = new byte[errorMsgLength+5];
      errMsg[0]=(byte)0;
      errMsg[1]=(byte)5;
      errMsg[2]=(byte)0;
      errMsg[3]=(byte)2;
      
      System.arraycopy(errorMsgBytes,0,errMsg,4,errorMsgBytes.length);
      errMsg[errMsg.length-1]=(byte)0;
      
    }
    if(errNum == 3) {
      //DISKFULL (might want to add the file name to the parameters)
      errorString = "The disk is full"; //like put the file name here
      errorMsgBytes = errorString.getBytes();
      errorMsgLength = (errorString.getBytes().length);
      errMsg = new byte[errorMsgLength+5];
      errMsg[0]=(byte)0;
      errMsg[1]=(byte)5;
      errMsg[2]=(byte)0;
      errMsg[3]=(byte)3;
      
      System.arraycopy(errorMsgBytes,0,errMsg,4,errorMsgBytes.length);
      errMsg[errMsg.length-1]=(byte)0;
      
    }
    if(errNum == 6) {
      //FileAlready Exists (might want to add the file name to the parameters)
      errorString = "This file already exists"; //like put the file name here
      errorMsgBytes = errorString.getBytes();
      errorMsgLength = (errorString.getBytes().length);
      errMsg = new byte[errorMsgLength+5];
      errMsg[0]=(byte)0;
      errMsg[1]=(byte)5;
      errMsg[2]=(byte)0;
      errMsg[3]=(byte)6;
      
      System.arraycopy(errorMsgBytes,0,errMsg,4,errorMsgBytes.length);
      errMsg[errMsg.length-1]=(byte)0;
      
    }
    else {
      System.out.println("This err number you entered is not an errorCode: "+ errNum);
    }
    System.out.println("RETURNING THIS BYTE ARRAY");
    for(int i=0;i<errMsg.length;i++) {
      System.out.print(errMsg[i]);
    }
    System.out.println();
    return errMsg;
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
