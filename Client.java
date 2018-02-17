//Group 8
/*
 * This is the client code, the code sends "read" and "write" request to the server.
 * */
import java.net.*;
import java.util.*;
import java.io.*;

public class Client extends Thread{
  DatagramSocket socket;//both send and receive
  
  FileInputStream fii = null;
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
    try{
      System.out.println("\n==================================================");
      System.out.println("Client: Sending a read request");
      byte[] foo = new byte[516];
      
      //first two strings are 0 and 1
      foo[0] = 0;
      foo[1] = 1;
      
      //filename gets converted to bytes
      byte [] b = filename.getBytes();
      int j = 2;
      
      for(int i = 0;i<filename.length();i++){
        foo[j]=b[i];
        j++;
      }
      foo[j] =0;
      //
      String mode = "octet";
      
      
      //print information
      String str = new String(foo);
      print(str);
      
      InetAddress address = InetAddress.getByName("127.0.0.1");
      DatagramPacket readRequest = new DatagramPacket(foo,20,address,23);
      Thread.sleep(2000);
      
      
      //send read request
      socket.send(readRequest);
      
      //keep accepting stuffs from the server until the size of the message is not up to 512
      while(true){
        byte[] b2 = new byte[516];
        DatagramPacket response = new DatagramPacket(b2, 516);
        
        //data received from Server
        System.out.println("Client: waiting for Data packet");
        socket.receive(response);
        System.out.println("Client: Data packet received");
        print(response.getData());
        
        byte[] b3 = response.getData();
        
        if(verifySize(response.getData()) == false){
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
          Thread.sleep(2000);
          socket.send(ack);
        }
        
      }
    } catch (Exception e){
      System.out.println("Error in read");
    }
  }
  //verify if the file sent is less than 512 bytes
  boolean verifySize(byte[] received){
    int i=4;
    while(i<516){
      if(received[i]==0) return false;
      i++;
    }
    return true;
  }
  /*
   * Function sends a write request to the ErrorSiumulator
   * */
  void write(String filename){
    try{
      byte[] foo = new byte[516];
      InetAddress address;
      DatagramPacket writeRequest;
      System.out.println("\n==================================================");
      System.out.println("Client: Sending a write request");
      
      //first two strings are 0 and 2
      foo[0] = 0;
      foo[1] = 2;
      
      //get the filename
      byte[] b = filename.getBytes();;
      int j = 2;
      
      for(int i = 0;i<filename.length();i++){
        foo[j]=b[i];
        j++;
      }
      foo[j] =0;
      //print out the message
      String str = new String(foo);
      print(str);
      
      address = InetAddress.getByName("127.0.0.1");
      writeRequest = new DatagramPacket(foo,516,address,23);
      
      Thread.sleep(2000);
      //send the request to the errorSimulator
      socket.send(writeRequest);
      //get the response from the server
      
      while(loop){
        byte[] b1 = new byte[516];
        DatagramPacket response = new DatagramPacket(b1, 516);
        
        System.out.println("Client: waiting... ");
        socket.receive(response);
        System.out.println("Client: response received");
        //print the message received
        print(response.getData());
        
        byte[] answer = checkACKandGetBlock(response.getData());
        if(answer[0]==-1){
          System.out.println("System didn't acknowledge");
          return;
        } else {
          fii = new FileInputStream(path+filename);
          //Server has sent back an acknowledgement, send the rest of the file
          
          byte[] b2 = new byte[512];
          byte[] b3 = new byte[516];
          b3[0]=0;
          b3[1]=3;
          b3[2]=answer[0];
          b3[3]=answer[1];
          
          fii.read(b2);//read more of the file and send it
          //check the size
          if(checkSize(b2)==false){
            loop=false;
          }
          for(int i =0;i<512;i++){
            b3[i+4]=b2[i];
          }
          
          address = InetAddress.getByName("127.0.0.1");
          DatagramPacket p =  new DatagramPacket(b2,516,address,23);
          //send the file to the server
          Thread.sleep(2000);
          socket.send(p);
        }
      }
    } catch (Exception e){
      System.out.println("Error in read function");
    }
    
  }
  boolean checkSize(byte[] b){
    for(int i =0;i<512;i++){
      if(b[i]==0) return false;
    }
    return true;
  }
  byte[] checkACKandGetBlock(byte[] r){
    if((r[0]!=0) || (r[1]!=4)){
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
      fii.close();
      socket.close();
      
    } catch (Exception e){
      System.out.println("Error in run");
      //e.printStackTrace();
      //System.exit(1);
    }
  }
  public static void main(String[] args){
    Client c = new Client();
    c.start();
  }
}
