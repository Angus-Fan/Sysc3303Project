//Patrick Aduwo
//100962777

//import java.net.*;
//import java.net.InetAddress;
import java.util.*;
import java.lang.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Client{
  DatagramSocket socket;//both send and receive
  int s = 1;
  
  Client(){
    try{
      socket = new DatagramSocket();
    } catch (Exception e){
      System.out.println("Error in constructor");
      e.printStackTrace();
      System.exit(1);
    }
  }
  
  void read(){
    try{
      System.out.println("\n==================================================");
      System.out.println("Client: Sending a read request");
      byte[] foo = new byte[20];
      
      //first two strings are 0 and 1
      foo[0] = 0;
      foo[1] = 1;
      
      //filename gets converted to bytes
      String filename = "read.txt";
      byte [] b = filename.getBytes();
      int j = 2;
      
      for(int i = 0;i<filename.length();i++){
        foo[j]=b[i];
        j++;
      }
      foo[j] =0;
      j++;//next element
      
      String mode = "neTAscii";//or "octet"
      byte [] b1 = mode.getBytes();
      for(int i =0; i<mode.length();i++){
        foo[j] = b1[i];
        j++;
      }
      foo[j] =0;
      
      //print information
      String str = new String(foo);
      print(str);
      
      InetAddress address = InetAddress.getByName("127.0.0.1");
      DatagramPacket readRequest = new DatagramPacket(foo,20,address,23);
      
      socket.send(readRequest);
      //wait to receive
      
      byte[] b2 = new byte[20];
      DatagramPacket r = new DatagramPacket(b2, 20);
      
      System.out.println("Client: waiting... ");
      socket.receive(r);
      System.out.println("Client: packet received");
      print(r.getData());
      
    } catch (Exception e){
      System.out.println("Error in read");
      System.out.println(e);
    }
  }
  void write(){
    try{
      System.out.println("\n======================================");
      System.out.println("Client: Sending a write request");
      byte[] foo = new byte[20];
      
      //first two strings are 0 and 2
      foo[0] = 0;
      foo[1] = 2;
      
      //filename gets converted to bytes
      String filename = "file.txt";
      byte [] b = filename.getBytes();
      int j = 2;
      
      for(int i = 0;i<filename.length();i++){
        foo[j]=b[i];
        j++;
      }
      foo[j] =0;
      j++;
      
      String mode = "neTAscii";//other mode is octet
      byte [] b1 = mode.getBytes();
      for(int i =0; i<mode.length();i++){
        foo[j] = b1[i];
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
      socket.send(write);
      
      //wait to receive
      byte[] b2 = new byte[20];
      DatagramPacket r = new DatagramPacket(b2, 20);
      
      System.out.println("Client: waiting for a response from the Intermediate host...");
      socket.receive(r);
      print(r.getData());
      
      
    } catch (Exception e){
      System.out.println("Error in write");
      System.out.println(e);
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
      j++;
      
      String mode = "neTAscii";//other mode is octet
      byte [] b1 = mode.getBytes();
      for(int i =0; i<mode.length();i++){
        foo[j] = b1[i];
        j++;
      }
      foo[j] =0;
      
      //print information
      String str = new String(foo);
      print(str);
      InetAddress address;
      address = InetAddress.getByName("127.0.0.1");
      DatagramPacket write = new DatagramPacket(foo,20,address,23);
      
      //send the request
      System.out.println("Client: sending an invalid request");
      socket.send(write);
      
      System.out.println("Client: waiting for a response from the Intermediate host...");
      //wait to receive
      byte[] b2 = new byte[20];
      DatagramPacket response = new DatagramPacket(b2, 20);
      
      socket.setSoTimeout(3000);
      socket.receive(response);
      print(response.getData());
      
    } catch (Exception e){
      System.out.println("Error in invalidRequest ");
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
  
  void run(){
    
    try{
      //read();
      for(int i =0;i<11;i++){
        //read request
        System.out.println("i; "+i);
        if(i==10){
          //invalid request
          invalidRequest();
        } else {
          if(s == 1){
            //Alternates between a read request and a write request
            read();
            s =2;
            
          } else if (s ==2) {
            write();
            s=1;
          }
        }
      }
      System.out.println("Sockets closing, good bye");
      socket.close();
      
    } catch (Exception e){
      System.out.println("Error in run");
      e.printStackTrace();
      System.exit(1);
    }
  }
  
  public static void main(String[] args){
    Client client = new Client();
    client.run();
  }
}