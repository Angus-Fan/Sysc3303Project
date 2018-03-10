//Group 8

/*
 * The server class accepts connections on part 69, when it receives a connection
 * it creates a thread to handle that connection. The thread the server creates
 * then sends back a response to the client and then closes.
 * Note: you would have to scroll up to interact with the main server
 * */
import java.net.*;
import java.util.*;
import java.io.*;

public class Server extends Thread{
  DatagramSocket socket = null;
  DatagramPacket packet = null;
  
  String name = null;
  Scanner scanner = null;
  
  //the path to access the file from, fill free to change this
  String path = null;
  
  List<Server> servers;
  
  Server(){
    try{
      socket = new DatagramSocket(69);;
      
      name = "mainServer";
      servers = new ArrayList<Server>();
      scanner = new Scanner(System.in);
      
      path = "C:/Users/Patrick/Documents/Courses/Sysc3303/Project/Iteration3/Code/";
      
    } catch (Exception e){
      System.out.println("Error in server constructor");
    }
  }
  Server(DatagramPacket packet){
    
    name = "notMainServer";
    path = "C:/Users/Patrick/Documents/Courses/Sysc3303/Project/Iteration3/Code/Client/";
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
        while(true){
        System.out.println("Number of server threads created so far: "+servers.size());
        System.out.println("What would you like to do: (shutdown) or (run)");
        String answer = scanner.nextLine();
        //String answer = "run";
        if(answer.toLowerCase().equals("shutdown")){
          System.out.println("Shuting down...");
          shutdown();
          //break;
        } else if(answer.toLowerCase().equals("run")){
          receiveConnections();
        }
        }
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
      System.out.println("an error in one of the thread's handling function, bye");
    }
  }
  boolean ackValid(byte [] response){
    if((response[0]==0) && (response[1]==4)) return true;
    
    return false;
  }
  
  //handle the read request, get the file from the server
  synchronized void handleReadRequest() throws IOException{
    byte blockNum1 =0;
    byte blockNum2 =1;
    path= "C:/Users/Patrick/Documents/Courses/Sysc3303/Project/Iteration3/Code/";
    DatagramSocket socket1 = null;
    InetAddress address = null;
    DatagramPacket packet1 = null;
    
    try{
      socket1 = new DatagramSocket();
      //System.out.println("Port: "+socket1.getPort());
      address = InetAddress.getByName("127.0.0.1");
    } catch (Exception e){
      System.out.println("Error in socket1 init");
    }
    
    
    
    String filename = getFileName(packet.getData());
    
    //System.out.println("Filename: "+filename);
    File file = new File(path+filename);
    //System.out.println("File");
    //
    
    //Trying to read from a file that doesn't exists  --- 
    if(file.exists()==false){
      byte[] responseBytes = new byte[516];
      //return an error packet
      responseBytes = errorPacket(1);
      
      System.out.println("Server: sending an error packet");
      packet1 = new DatagramPacket(responseBytes,516,address,packet.getPort());
      try{
        Thread.sleep(3000);
        socket1.send(packet1);
      } catch (Exception e){
        System.out.println("Error sending packet or sleeping");
      }
      
      socket1.close();
      return;
    }
    
    
    FileInputStream fileInputStream=null;
    try{
      fileInputStream= new FileInputStream(file);
    } catch (Exception e){
      System.out.println("Error in init fileInputStream");
    }
    
    //send the data from the file
    while(true){
      byte[] dataBytes = new byte[516];
      //set the data block, 0 3 for ACK blocks
      dataBytes[0] = 0;
      dataBytes[1] = 3;
      //set the block number
      dataBytes[2] = blockNum1;
      dataBytes[3] = blockNum2;
      
      boolean checkSize1 = true;
      try{
        //read the file
        System.out.println("Server: reading file");
        
        if(fileInputStream.available()<=512) checkSize1=false;
        byte[] fileBytes1 = new byte[512];
        
        fileInputStream.read(fileBytes1);
        for(int i =0;i<512;i++){
          dataBytes[i+4]=fileBytes1[i];
        }
      } catch (Exception e){
        System.out.println("Error reading file");
      }
      
      
      //send the data
      System.out.println("Server: Sending fileBytes");
      //print(dataBytes);
      DatagramPacket packet2= new DatagramPacket(dataBytes,516,address, packet.getPort());//-------
      
      try{
        Thread.sleep(3000);
        socket1.send(packet2);
        
      } catch (Exception e){
        System.out.println("Error in sending packet/sleep");
      }
      
      //checkSize1 = checkSize(dataBytes);
      //System.out.println("CheckSize1: "+checkSize1);
      
      //receive the acknowledgement
      byte[] responseBytes1 = new byte[516];
      DatagramPacket responsePacket = new DatagramPacket(responseBytes1, 516);
      //ITERATION 3 ( if timeout resend packet )
      //WAITING FOR TIMEOUT 10 SECONDS
      socket1.setSoTimeout(10000);
      try{
    	
        System.out.println("Server: waiting to receive acknowledgement packet");
        socket1.receive(responsePacket);
        
        
      } catch (SocketTimeoutException e){
        System.out.println("We timed out while waiting to receive ack packet");
        //We are going to resend the packet 
        System.out.println("Resending Packet to client");
        try{
            Thread.sleep(3000);
            socket1.send(packet2);
            
          } catch (Exception t){
            System.out.println("Error in sending packet/sleep");
          }
        
     
        
      }/*
      This was the previous catch statement changed to timeout for Iteration 3
      catch (Exception e){
          System.out.println("Error in receiving packet");
        }*/
        
      	
      	
      //}
      
      //---------------------------------------------------
      
      System.out.println("Server: Acknowledgement recevied");
      byte[] responseBytes2 = packet.getData();
      //client has sent back an acknowledgement
      if(responseBytes2[0]==0 && responseBytes2[1]==4){
        if(blockNum1==9){
          blockNum2+=1;
          blockNum1=0;
        } else {
          ++blockNum2;
        }
      } 
      //duplicates
      //check if a read request was sent (again), a previous block was sent,
      //or an invalid opcode
      byte a = responseBytes2[1];
      byte a1 = responseBytes2[2];
      
      if(responseBytes2[1]< blockNum1){
        
        System.out.println("Server: Duplicate, sending previous fileBytes");
      } else if(responseBytes2[1]==blockNum1 && responseBytes2[2]<blockNum2){
        System.out.println("Server: Duplicate, sending previous fileBytes");
        
      }
      //lose packet
      if(responseBytes2[1]>blockNum1){
        System.out.println("Server: lose packet, a packet was not received before this one");
      } else if(responseBytes2[1]>blockNum1 && responseBytes2[2]>blockNum2){
        System.out.println("Server: lose packet, a packet was not received before this one");
      }
      
      
      //check if the number of bytes sent was less than 512 bytes
      if(checkSize1==false){
        try{
          System.out.println("Closing sockets, bye");
          fileInputStream.close();
          socket1.close();
        } catch (Exception e){
          System.out.println("Error with closing socket");
        }
        break;
      }
      //System.out.println("CheckSize1");
    }
    
  }
  synchronized boolean checkSize(byte[] data){
    for(int i =4;i<516;i++){
      if(data[i]==0){
        return false;
      }
    }
    return true;
  }
  //Function handles the writeRequst sent by the client
  
  synchronized void handleWriteRequest(){
    byte blockNum1=0;
    byte blockNum2=0;
    path = "C:/Users/Patrick/Documents/Courses/Sysc3303/Project/Iteration3/Code/Server/";
    System.out.println("Write request received");
    InetAddress address = null;
    DatagramSocket socket1 = null;
    FileOutputStream fileOutputStream = null;
    
    try{
      socket1 = new DatagramSocket();
      address = InetAddress.getByName("127.0.0.1");
    } catch (Exception e){
      System.out.println("Error in init address and socket1");
    }
    //send back a an acknowledgement
    System.out.println("Server: Send back an acknowledgement");
    String filename = getFileName(packet.getData());
    byte[] dataBytes = new byte[516];
    
    File file = new File(path+filename);
    
    //if the file already exists return an error
    if(file.exists()){
      //file already exisits
      System.out.println("File already exists");
      dataBytes = errorPacket(6);
      DatagramPacket errorPacket = new DatagramPacket(dataBytes,516,address, packet.getPort());
      
      try{
        socket1.send(errorPacket);
        socket1.close();
      } catch (Exception e){
        System.out.println("Error in sending");
      }
      return;
    }
    
    //if the file doesn't exists create a file
    if(file.exists()==false){
      System.out.println("Creating new file");
      try{
        file.createNewFile();
      } catch(Exception e){
        System.out.println("Error in creating new file");
      }
      
    }
    
    if(file.canWrite()==false){
      //Access violation
      System.out.println("Access violation: can't write to file");
      dataBytes = errorPacket(2);
      DatagramPacket errorPacket = new DatagramPacket(dataBytes,516,address, packet.getPort());
      try{
        socket1.send(errorPacket);
        socket1.close();
      } catch(Exception e){
        System.out.println("Error in sending");
      }
      return;
    } else {
      dataBytes[0]=0;
      dataBytes[1]=4;
      dataBytes[2]=blockNum1;
      dataBytes[3]=blockNum2;
    }
    
    DatagramPacket acknowledgementPacket = new DatagramPacket(dataBytes,516,address, packet.getPort());
    try{
      Thread.sleep(5000);
      socket1.send(acknowledgementPacket);
    } catch (Exception e){
      System.out.println("Error in sending back acknowledgment");
    }
    
    boolean loop = true;
    try{
      fileOutputStream= new FileOutputStream(file);
    } catch (Exception e){
      System.out.println("Error in init fileOutputStream");
    }
    while(loop){
      //if the disk is full
      if(file.getUsableSpace()<512){
        byte[] errorBytes = errorPacket(3);
        DatagramPacket errorPacket = new DatagramPacket(errorBytes,516,address, packet.getPort());
        try{
          
          socket1.send(errorPacket);
          socket1.close();
          return;
        } catch(Exception e){
          System.out.println("Error sending error packet");
        }
      }
        //receive data
        byte[] responseDataBytes = new byte[516];
      DatagramPacket responseDataPacket = new DatagramPacket(responseDataBytes, 516);
      try{
        socket1.receive(responseDataPacket);
        loop = checkSize(responseDataPacket.getData());
        System.out.println("Server: Data received");
      } catch (Exception e){
        System.out.println("Error in receciving");
      }
      try{
        //write the Data to the file
        //print(responseDataPacket.getData());
        System.out.println("Server: writing the bytes to the file");
        byte[] data = responseDataPacket.getData();
        for(int i =4;i<516;i++){
          data[i-4]=data[i];
        }
        int num = checkSizeAndReturn(data);
        if(num!=-1){
          System.out.println("Those are the last bytes");
          loop = false;
        }
        
        fileOutputStream.write(data,0,num);
        
      } catch(Exception e){
        System.out.println("System Error in writing the data to the file");
      }
      //send an acknowledgment
      
      byte[] ackBytes = new byte[516];
      //set the acknowledgement bytes
      ackBytes[0] = 0;
      ackBytes[1] = 4;
      //set the block number
      byte[] blockNum = responseDataPacket.getData();
      ackBytes[2] = blockNum[2];
      ackBytes[3] = blockNum[3];
      
      
      DatagramPacket acknowledgementPacket1 = new DatagramPacket(ackBytes,516,address, responseDataPacket.getPort());
      try{
        System.out.println("Server: Sending acknowledgement");
        Thread.sleep(3000);
        socket1.send(acknowledgementPacket1);
      } catch (Exception e){
        System.out.println("Error sending packet");
      }
      
    }
    
  }
  int checkSizeAndReturn(byte[] data){
    for(int i =0;i<512;i++){
      if(data[i]==0) return i;
    }
    return -1;
  }
  
  //Function gets the name of the file and returns it
  String getFileName(byte [] received){
    
    int index =2;
    while(received[index]!=0){
      index++;
    }
    int size = index-2;
    byte[] filenameBytes = new byte[size];
    int j = 2;
    
    for(int i = 0;i < size; i++){
      filenameBytes[i] = received[j];
      j++;
    }
    String filename = new String(filenameBytes);
    return filename;
  }
  /*
   * This function is only executed by the main server. It is used to recieve
   * messages from the Error simulator and pass it on to a server
   * */
  void receiveConnections(){
    try{
      byte[] b = new byte[516];
      DatagramPacket packet = new DatagramPacket(b, 516);
      
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
    byte[] filenameBytes = new byte[size];
    int j = 2;
    
    for(int ii = 0;ii < size; ii++){
      filenameBytes[ii] = received[j];
      j++;
    }
    String filename = new String(filenameBytes);
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
    if(errNum==4) {
    	 //   Illegal TFTP operation Exists (might want to add the file name to the parameters)
        errorString = "The TFTP operation you attempted does not exist or has not been implemented"; //like put the file name here
        errorMsgBytes = errorString.getBytes();
        errorMsgLength = (errorString.getBytes().length);
        errMsg = new byte[errorMsgLength+5];
        errMsg[0]=(byte)0;
        errMsg[1]=(byte)5;
        errMsg[2]=(byte)0;
        errMsg[3]=(byte)4;
        
        System.arraycopy(errorMsgBytes,0,errMsg,4,errorMsgBytes.length);
        errMsg[errMsg.length-1]=(byte)0;
    }
    if(errNum==5) {
   	 //   Illegal TFTP operation Exists (might want to add the file name to the parameters)
       errorString = "The TFTP operation you attempted does not exist or has not been implemented"; //like put the file name here
       errorMsgBytes = errorString.getBytes();
       errorMsgLength = (errorString.getBytes().length);
       errMsg = new byte[errorMsgLength+5];
       errMsg[0]=(byte)0;
       errMsg[1]=(byte)5;
       errMsg[2]=(byte)0;
       errMsg[3]=(byte)5;
       
       System.arraycopy(errorMsgBytes,0,errMsg,4,errorMsgBytes.length);
       errMsg[errMsg.length-1]=(byte)0;
   }
    if(errNum == 6) {
      //Unknown transfer ID (might want to add the file name to the parameters)
      errorString = "Unknown transfer ID"; //like put the file name here
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
    //for(int i=0;i<errMsg.length;i++) {
    //  System.out.print(errMsg[i]);
    //}
    System.out.println();
    return errMsg;
  }
  //print the bytes as strings
  void print(byte[] data){
    for(int i =0;i<512;i++){
      System.out.print(data[i]+" ");
    }
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
  //Main function
  public static void main(String[] args){
    Server server = new Server();
    server.start();
  }
  
}
