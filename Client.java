//Group 8
/*
 * This is the client code, the code sends "read" and "write" request to the server.
 * */
import java.net.*;
import java.util.*;
import java.io.*;

public class Client extends Thread{
  DatagramSocket socket;//both send and receive
  
  FileInputStream fileInputStream = null;
  byte fileIndex =1;
  Scanner scanner = null;
  boolean loop = true;
  
  String path = "C:/Users/Patrick/Documents/Courses/Sysc3303/Project/iteration2/Code/";
  
  Client(){
    try{
      socket = new DatagramSocket();
      scanner = new Scanner(System.in);
      
    } catch (Exception e){
      System.out.println("Error in constructor");
    }
  }
  /*
   * Function sends a read request to the ErrorSimulator
   * */
  void read(String filename){
    InetAddress address = null;
    
    System.out.println("\n==================================================");
    System.out.println("Client: Sending a read request");
    byte[] foo = new byte[516];
    
    //first two strings are 0 and 1
    foo[0] = 0;
    foo[1] = 1;
    
    //filename gets converted to bytes
    byte [] b = filename.getBytes();
    //System.out.println(filename);
    int j = 2;
    for(int i = 0;i<filename.length();i++){
      foo[j]=b[i];
      j++;
    }
    foo[j] =0;
    
    //
    String mode = "octet";
    byte[] b1 = mode.getBytes();
    //System.out.println(mode);
    //
    ++j;
    for(int i =0;i<5;i++){
      foo[j]=b1[i];
      j++;
    }
    foo[j]=0;
    
    //print information
    
    String str = new String(foo);
    System.out.println(str);
   // print(str);
    
    try{
      address = InetAddress.getByName("127.0.0.1");
    } catch(Exception e){
      System.out.println("Error in InetAddress");
    }
    //Create the readRequest datagramPacket
    DatagramPacket readRequest = new DatagramPacket(foo,20,address,23);
    try{
      Thread.sleep(2000);
      //send read request
      socket.send(readRequest);
    } catch (Exception e){
      System.out.println("Error in sending Read request");
    }
    
    //keep accepting stuffs from the server until the size of the message is not up to 512
    while(true){
      byte[] b2 = new byte[516];
      DatagramPacket response = new DatagramPacket(b2, 516);
      
      //data received from Server
      try{
        System.out.println("Client: waiting for Data packet");
        socket.receive(response);
        System.out.println("Client: Data packet received");
      } catch(Exception e){
        System.out.println("Error in receiving response");
      }
      print(response.getData());
      
      byte[] b3 = response.getData();
      
      if(checkSize(response.getData()) == false){
        System.out.println("That is all");
        break;
        
      } else {
        byte[] responseByte = response.getData();
        
        //send acknowledgement
        byte[] foo2 = new byte[516];
        foo2[0]=0;
        foo2[1]=4;
        foo2[2]= b3[2];
        foo2[3]= b3[3];
        
        System.out.println("Client: Sending acknowledgement");
        DatagramPacket ack = new DatagramPacket(foo2, 516,address,23);
        try{
          Thread.sleep(2000);
          socket.send(ack);
        } catch(Exception e){
          System.out.println("Error in sending acknowledgement");
        }
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
  }
  /*
   * Function sends a write request to the ErrorSiumulator
   * */
  void write(String filename){
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
      
    } catch (Exception e){
      System.out.println("error with thread sleeping, address or writeRequest");
    }
    try{
      //send the request to the errorSimulator
      socket.send(writeRequestPacket);
    } catch(Exception e){
      System.out.println("Error in sending writeRequest");
    }
    //get the response from the server
    
    while(loop){
      byte[] responseBytes = new byte[516];
      DatagramPacket responsePacket = new DatagramPacket(responseBytes, 516);
      
      System.out.println("Client: waiting for a response ");
      
      try{
        socket.receive(responsePacket);
      } catch (Exception e){
        System.out.println("Error in waiting for a response");
      }
      System.out.println("Client: response received");
      //print the message received
      System.out.println("Client: printing packet information");
      print(responsePacket.getData());
      
      byte[] answer = handleACK(responsePacket.getData());
      if(answer[0]==-1){
        System.out.println("System didn't acknowledge, an error occured");
        return;
      } else {
        try{
          //create a file stream for the file
          fileInputStream = new FileInputStream(path+filename);
        } catch(Exception e){
          System.out.println("Error in init fileInputStream");
        }
        //Server has sent back an acknowledgement, send the rest of the file
        
        //byte[] fileBytes = new byte[512];
        byte[] dataBytes = new byte[516];
        
        dataBytes[0]=0;
        dataBytes[1]=3;
        if(answer[3]==9){
          answer[3]=0;
          answer[2]+=1;
        } else{
          answer[3]+=1;
        }
        dataBytes[2]=answer[0];
        dataBytes[3]=answer[1];
        
        try{
          //check how much bytes are left
          if(fileInputStream.available()<=512){
            loop= false;
          }
          
          //read more of the file and send it--------------------------
          fileInputStream.read(dataBytes, 4, 512);
        } catch(Exception e){
          System.out.println("Error reading file");
        }

        DatagramPacket p =  new DatagramPacket(dataBytes,516,address,23);
        //send the file to the server
        try{
          Thread.sleep(2000);
          socket.send(p);
        } catch (Exception e){
          System.out.println("Error sending the fileBytes ");
        }
      }
    }
  }
//Check the size of the file
  boolean checkSize(byte[] b){
    for(int i =0;i<512;i++){
      if(b[i]==0) return false;
    }
    return true;
  }
//verify the response received from the user and handle it acorrdingly
  byte[] handleACK(byte[] r){
    if(r[0]==0 && r[1]==5){//This is an error
      System.out.println("The server encountered an error");
      if(r[2]==0 && r[3]==1){//File not Found
      } else if(r[2]==0 && r[3]==2){//Access violation
        System.out.println("Error, Access violation");
      } else if(r[2]==0 && r[3]==3){//disk full or allocation exceeded
        System.out.println("Error, the disk is full");
      } else if(r[2]==0 && r[3]==6){//file already exists
        System.out.println("Error, The file already exists");
      }
      byte[] a = new byte[1];
      a[0]=-1;
      return a;
    }
    byte arr [] = new byte[2];
    arr[0]=r[2];
    arr[1]=r[3];
    return arr;
  }
//get the file 
  /*
   * The function prints out the bytes of array "b" both as a string and bytes
   * */
  void print(String str){
    //print out the information you're about to send
    System.out.print("Message as text: ");
    System.out.println(str);//print as a string
    
    System.out.print("Message as binary: ");
    byte [] b = str.getBytes();
    System.out.println(Arrays.toString(b));//print as a binary
    
  }
  /*
   * The function prints out the bytes of array "b" both as a string and bytes
   * */
  void print(byte[] b){
    //print out the information you're about to send
    String str = new String(b);
    System.out.print("Message as text: ");
    System.out.println(str);//print as a string
    
    System.out.print("Message as binary: ");
    System.out.println(Arrays.toString(b));//print as a binary
    
  }
  
  public void run(){
    //while(true){
    System.out.println("Hi, what type of request would you like to send: (read), (write) or (shutdown)");
    //String answer = scanner.nextLine().toLowerCase();
    //String answer = "read";
    String answer = "write";
    if(answer.equals("read")){
      System.out.println("What is the name of the file");
      answer = "test.txt";
      //answer = scanner.nextLine().toLowerCase();
      
      read(answer);
    } else if(answer.equals("write")){
      System.out.println("What is the name of the file");
      answer = "test.txt";
      //answer = scanner.nextLine().toLowerCase();
      
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
      fileInputStream.close();
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
