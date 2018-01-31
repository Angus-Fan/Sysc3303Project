//Patrick Aduwo
//100962777

import java.net.*;
import java.util.*;

public class Client extends Thread{
  DatagramSocket socket;//both send and receive
  int s = 1;
  Scanner scanner;
  
  Client(){
    try{
      socket = new DatagramSocket();
      scanner = new Scanner(System.in);
    } catch (Exception e){
      System.out.println("Error in constructor");
      e.printStackTrace();
      //System.exit(1);
    }
  }
  
  void read(String filename){
    try{
      System.out.println("\n==================================================");
      System.out.println("Client: Sending a read request");
      byte[] foo = new byte[20];
      
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
      
      //print information
      String str = new String(foo);
      print(str);
      
      InetAddress address = InetAddress.getByName("127.0.0.1");
      DatagramPacket readRequest = new DatagramPacket(foo,20,address,23);
      
      Thread.sleep(3000);
      
      socket.send(readRequest);
      //wait to receive
      
      byte[] b2 = new byte[20];
      DatagramPacket response = new DatagramPacket(b2, 20);
      
      System.out.println("Client: waiting... ");
      socket.receive(response);
      System.out.println("Client: packet received");
      print(response.getData());
      
    } catch (Exception e){
      System.out.println("Error in read");
      //System.out.println(e);
      //System.exit(1);
    }
  }
  void write(String filename){
    try{
      System.out.println("\n======================================");
      System.out.println("Client: Sending a write request");
      byte[] foo = new byte[20];
      
      //first two strings are 0 and 2
      foo[0] = 0;
      foo[1] = 2;
      
      //filename gets converted to bytes
      //String filename = "file.txt";
      byte [] b = filename.getBytes();
      int j = 2;
      
      for(int i = 0;i<filename.length();i++){
        foo[j]=b[i];
        j++;
      }
      foo[j] =0;
      
      //print information
      String str = new String(foo);
      print(str);
      
      InetAddress address = InetAddress.getByName("127.0.0.1");
      DatagramPacket write = new DatagramPacket(foo,20,address,23);
      
      //send the request
      System.out.println("Client: sending a write request");
      Thread.sleep(3000);
      socket.send(write);
      
      //wait to receive
      byte[] b2 = new byte[20];
      DatagramPacket writeResponse = new DatagramPacket(b2, 20);
      
      System.out.println("Client: waiting for a response from the Intermediate host...");
      socket.receive(writeResponse);
      print(writeResponse.getData());
      
      
    } catch (Exception e){
      System.out.println("Error in write");
      //System.out.println(e);
    }
  }
  void invalidRequest(){
    try{
      System.out.println("\n=================================================");
      System.out.println("Client: Sending an invalid request");
      byte[] foo = new byte[20];
      foo[0]=44;
      foo[1]=90;
      
      //filename gets converted to bytes
      String filename = "file.txt";
      byte [] b = filename.getBytes();
      int j = 2;
      
      for(int i = 0;i<filename.length();i++){
        foo[j]=b[i];
        j++;
      }
      foo[j] =0;
      
      //print information
      String str = new String(foo);
      print(str);
      InetAddress address;
      address = InetAddress.getByName("127.0.0.1");
      DatagramPacket invalid = new DatagramPacket(foo,20,address,23);
      
      //send the request
      System.out.println("Client: sending an invalid request");
      Thread.sleep(3000);
      socket.send(invalid);
      
      System.out.println("Client: waiting for a response from the Intermediate host...");
      //wait to receive
      byte[] b2 = new byte[11];
      DatagramPacket invalidResponse = new DatagramPacket(b2, 20);
      
      socket.setSoTimeout(3000);
      socket.receive(invalidResponse);
      print(invalidResponse.getData());
      
    } catch (Exception e){
      
      System.out.println("Error in (InvalidRequest)");
      
    }
    
  }
  
  void print(String str){
    //print out the information you're about to send
    System.out.print("Message as text: ");
    System.out.println(str);//print as a string
    
    System.out.print("Message as binary: ");
    byte [] b = str.getBytes();
    System.out.println(Arrays.toString(b));//print as a binary
    
  }
  void print(byte[] b){
    //print out the information you're about to send
    String str = new String(b);
    System.out.print("Message as text: ");
    System.out.println(str);//print as a string
    
    System.out.print("Message as binary: ");
    System.out.println(Arrays.toString(b));//print as a binary
    
  }
  
  public void run(){
    try{
      while(true){
        System.out.println("Hi, what type of request would you like to send: (read), (write) or (shutdown)");
        //String answer = scanner.nextLine().toLowerCase();
        String answer = "read";
        if(answer.equals("read")){
          System.out.println("What is the name of the file");
          //answer = scanner.nextLine().toLowerCase();
          answer = "file.txt";
          read(answer);
        } else if(answer.equals("write")){
          System.out.println("What is the name of the file");
          answer = scanner.nextLine().toLowerCase();
          write(answer);
          
        } else if (answer.equals("shutdown")){
          System.out.println("Shuting down...");
          break;
          
        } else {
          System.out.println("Invalid request");
        }
      }
      
      System.out.println("Sockets closing, good bye");
      socket.close();
      
    } catch (Exception e){
      System.out.println("Error in run");
      e.printStackTrace();
      //System.exit(1);
    }
  }
  public static void main(String[] args){
    Client c = new Client();
    c.run();
  }
}
