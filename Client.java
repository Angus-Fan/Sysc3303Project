//Group 8
/*
 * This is the client code, the code sends "read" and "write" request to the server.
 * */
import java.net.*;
import java.util.*;
import java.io.*;

public class Client extends Thread{
  private DatagramSocket socket;//both send and receive
  
  FileInputStream fileInputStream = null;
  byte fileIndex =1;
  Scanner scanner = null;
  boolean loop = true;
  
  String path = null;
  
  Client(){
    try{
      socket = new DatagramSocket();
      scanner = new Scanner(System.in);
      path = "C:/Users/Patrick/Documents/Courses/Sysc3303/Project/Iteration2/Code/";
      
    } catch (Exception e){
      System.out.println("Error in constructor");
    }
  }
  /*
   * Function sends a read request to the ErrorSimulator
   * */
  private void read(String filename){
    InetAddress address = null;
    
    System.out.println("\n==================================================");
    System.out.println("Client: Sending a read request");
    byte[] readRequestBytes = new byte[516];
    
    try{
      address = InetAddress.getByName("127.0.0.1");
    } catch(Exception e){
      System.out.println("Error in InetAddress");
    }
    
    //first two strings are 0 and 1
    readRequestBytes[0] = 0;
    readRequestBytes[1] = 1;
    
    //filename gets converted to bytes
    byte [] filenameBytes = filename.getBytes();
    //System.out.println(filename);
    int j = 2;
    for(int i = 0;i<filename.length();i++){
      readRequestBytes[j]=filenameBytes[i];
      j++;
    }
    readRequestBytes[j] =0;
    
    //
    /*
     String mode = "octet";
     byte[] b1 = mode.getBytes();
     //System.out.println(mode);
     //
     ++j;
     for(int i =0;i<5;i++){
     dataBytes[j]=b1[i];
     j++;
     }
     dataBytes[j]=0;
     */
    
    //print information
    
    String str = new String(readRequestBytes);
    System.out.println(str);
    // print(str);
    
    
    //Create the readRequest datagramPacket
    DatagramPacket readRequestPacket = new DatagramPacket(readRequestBytes,20,address,23);
    try{
      Thread.sleep(2000);
      //send read request
      socket.send(readRequestPacket);
    } catch (Exception e){
      System.out.println("Error in sending Read request");
    }
    boolean loop = true;
    
    //keep accepting stuffs from the server until the size of the message is not up to 512
    while(loop){
      byte[] dataBytes = new byte[516];
      DatagramPacket dataPacket = new DatagramPacket(dataBytes, 516);
      
      //data received from Server
      try{
        System.out.println("Client: waiting for Data packet");
        socket.receive(dataPacket);
        System.out.println("Client: Data packet received");
      } catch(Exception e){
        System.out.println("Error in receiving response");
      }
      //print(dataPacket.getData());
      
      //byte[] dataBytes2 = dataPacket.getData();
      
      if(checkSize(dataPacket.getData()) == false){
        //send the last acknowledgement
        loop = false;
        
        System.out.println("That is all");
        try{
        fileInputStream.close();
        } catch (Exception e){
          System.out.println("Error closing file input stream");
        }
      }
      byte[] dataBytes2 = dataPacket.getData();
      
      //send acknowledgement
      byte[] ackBytes = new byte[516];
      
      ackBytes[0]=0;
      ackBytes[1]=4;
      ackBytes[2]= dataBytes2[2];
      ackBytes[3]= dataBytes2[3];
      
      System.out.println("Client: Sending acknowledgement");
      DatagramPacket ackPacket = new DatagramPacket(ackBytes, 516,address,23);
      try{
        Thread.sleep(2000);
        socket.send(ackPacket);
      } catch(Exception e){
        System.out.println("Error in sending acknowledgement");
      }
      
      
    }
    
  }
  //Function creates a packet to be sent
  private void createPacket(byte[] writeRequest, String filename){
    //first two strings are 0 and 2
    writeRequest[0] = 0;
    writeRequest[1] = 2;
    
    //get the filename
    byte[] fileBytes = filename.getBytes();;
    int j = 2;
    
    for(int i = 0;i<filename.length();i++){
      writeRequest[j]=fileBytes[i];
      j++;
    }
    writeRequest[j] =0;
    /*
     //Set the mode
     String mode = "octet";
     byte[] modeBytes = mode.getBytes();
     //System.out.println(mode);
     //
     ++j;
     //copy the modeBytes into the writeRequest (bytes)
     for(int i =0;i<5;i++){
     writeRequest[j]=modeBytes[i];
     j++;
     }
     writeRequest[j]=0;
     */
  }
  /*
   * Function sends a write request to the ErrorSiumulator
   * */
  private void write(String filename){
    byte[] writeRequest = new byte[516];
    InetAddress address = null;
    DatagramPacket writeRequestPacket = null;
    String string = null;
    
    try{
      address = InetAddress.getByName("127.0.0.1");
    } catch (Exception e){
      System.out.println("Error in InetAddress");
    }
    
    System.out.println("\n==================================================");
    System.out.println("Client: Sending a write request");
    
    
    createPacket(writeRequest, filename);
    
    //print out the message
    string = new String(writeRequest);
    print(string);
    
    try{
      writeRequestPacket = new DatagramPacket(writeRequest,516,address,23);
      
      Thread.sleep(2000);
      socket.send(writeRequestPacket);
    } catch(Exception e){
      System.out.println("Error in sending writeRequest/sleep");
    }
    //get the response from the server
    try{
      fileInputStream = new FileInputStream(path+filename);
    } catch (Exception e){
      System.out.println("Error in init inputStream");
    }
    
    while(loop){
      byte[] ackBytes = new byte[516];
      DatagramPacket ackPacket = new DatagramPacket(ackBytes, 516);
      
      System.out.println("Client: waiting for an acknowledgement ");
      
      try{
        socket.receive(ackPacket);
      } catch (Exception e){
        System.out.println("Error in receiving acknowledgment");
      }
      
      System.out.println("Client: acknowledgment received");
      //print the message received
      System.out.println("Client: printing packet information");
      //print(responsePacket.getData());
      
      byte[] answer = handleACK(ackPacket.getData());
      
      if(answer[0]==-1){
        System.out.println("System didn't acknowledge, an error occured");
        return;
      } else {
        try{
          //create a file stream for the file
          
        } catch(Exception e){
          System.out.println("Error in init fileInputStream");
        }
        //Server has sent back an acknowledgement, send the rest of the file
        
        //byte[] fileBytes = new byte[512];
        byte[] dataBytes = new byte[516];
        
        dataBytes[0]=0;
        dataBytes[1]=3;
        //
        if(answer[1]==9){
          answer[1]=0;
          answer[0]+=1;
        } else{
          answer[1]+=1;
        }
        dataBytes[2]=answer[0];
        dataBytes[3]=answer[1];
        
        try{
          //check how much bytes are left
          if(fileInputStream.available()<=512){
            System.out.println("Last set of bytes");
            loop= false;
            
          }
          
          //read more of the file and send it--------------------------
          System.out.println("Client: reading from file");
          fileInputStream.read(dataBytes, 4, 512);
        } catch(Exception e){
          System.out.println("Error reading file");
        }
        
        DatagramPacket p =  new DatagramPacket(dataBytes,516,address,23);
        //send the file to the server
        try{
          Thread.sleep(2000);
          System.out.println("Client: Sending file bytes");
          socket.send(p);
        } catch (Exception e){
          System.out.println("Error sending the fileBytes ");
        }
        if(loop==false){
          byte[] ackBytes2 = new byte[516];
          DatagramPacket ackPacket2 = new DatagramPacket(ackBytes2, 516);
          
          System.out.println("Client: waiting for an acknowledgement ");
          
          try{
            socket.receive(ackPacket2);
          } catch (Exception e){
            System.out.println("Error in receiving acknowledgment");
          }
          
          System.out.println("Client: acknowledgment received");
          //print the message received
          System.out.println("Client: printing packet information");
          //print(responsePacket.getData());
          
        }
        
      }
    }
  }
//Check the size of the file
  private boolean checkSize(byte[] received){
    for(int i =4;i<516;i++){
      if(received[i]==0) return false;
    }
    return true;
  }
//verify the response received from the user and handle it acorrdingly
  byte[] handleACK(byte[] response){
    if(response[0]==0 && response[1]==5){//This is an error
      System.out.println("The server encountered an error");
      if(response[2]==0 && response[3]==1){//File not Found
      } else if(response[2]==0 && response[3]==2){//Access violation
        System.out.println("Error, Access violation");
      } else if(response[2]==0 && response[3]==3){//disk full or allocation exceeded
        System.out.println("Error, the disk is full");
      } else if(response[2]==0 && response[3]==6){//file already exists
        System.out.println("Error, The file already exists");
      }
      byte[] error = new byte[1];
      error[0]=-1;
      return error;
    }
    byte blockNum [] = new byte[2];
    blockNum[0]=response[2];
    blockNum[1]=response[3];
    return blockNum;
  }
//get the file 
  /*
   * The function prints out the bytes of array "b" both as a string and bytes
   * */
  void print(String string){
    //print out the information you're about to send
    System.out.print("Message as text: ");
    System.out.println(string);//print as a string
    
    System.out.print("Message as binary: ");
    byte [] b = string.getBytes();
    System.out.println(Arrays.toString(b));//print as a binary
    
  }
  /*
   * The function prints out the bytes of array "b" both as a string and bytes
   * */
  void print(byte[] received){
    //print out the information you're about to send
    String string = new String(received);
    System.out.print("Message as text: ");
    System.out.println(string);//print as a string
    
    System.out.print("Message as binary: ");
    System.out.println(Arrays.toString(received));//print as a binary
    
  }
  
  public void run(){
    //while(true){
    System.out.println("Hi, what type of request would you like to send: (read), (write) or (shutdown)");
    String answer = scanner.nextLine().toLowerCase();

    if(answer.equals("read")){
      System.out.println("What is the name of the file");
      //answer = "test.txt";
      answer = scanner.nextLine().toLowerCase();
      
      read(answer);
    } else if(answer.equals("write")){
      System.out.println("What is the name of the file");
      answer = scanner.nextLine().toLowerCase();
      
      write(answer);
      
    } else if (answer.equals("shutdown")){
      System.out.println("Shuting down...");
      //break;
    } else {
      System.out.println("Invalid request");
    }
    //}
    try{
      System.out.println("Sockets closing, good bye");
      socket.close();
      
    } catch (Exception e){
      System.out.println("Error closing sockets/streams");
    }
  }
  
  public static void main(String[] args){
    Client c = new Client();
    c.start();
  }
}
