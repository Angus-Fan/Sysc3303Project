//Patrick Aduwo
//100962777
import java.net.*;
import java.util.*;
//import java.lang.Object;


public class Server {
  DatagramSocket socket;
  
  Server(){
	  Block ackBlock = new Block();
	  //respond with ACK block 0
	  ackBlock.setBlockNoPart(0);
	 
	  //respond with DATA block 1 and 0 bytes of data
	  Block dataBlock = new Block();
	  dataBlock.setBlockNoPart(1);
	  
  }
  void run(){
    try{
      //Loop runs forever
      while(true){
        socket = new DatagramSocket(69);
        byte[] b = new byte[20];
        DatagramPacket packet = new DatagramPacket(b, 20);
        
        //The server waits to receive a packet from the intermediate host
        System.out.println("\n=========================================================");
        System.out.println("Server: Waiting for Packet from the intermediate host");
        socket.receive(packet);
        
        String info = new String(packet.getData());
        boolean valid = check(packet.getData());//parse the data to see if it's valid
        print(info);//prints information it has received
        
        byte[] replyBytes = new byte[4];
        DatagramPacket reply ;
        
        if(valid){
          byte[] b1 = packet.getData();
          if(b1[0] == 0 && b1[1]==1){
        	//Valid Read Request
            //send 0 3 0 1
            replyBytes[0] = 0;
            replyBytes[1] = 3;
            replyBytes[2] = 0;
            replyBytes[3] = 1;
            //Send Data Block Here
          } else if(b1[0] ==0 && b1[1]==2){
        	//Valid Write Request
            //send 0 4 0 0
        	  
            replyBytes[0] = 0;
            replyBytes[1] = 4;
            replyBytes[2] = 0;
            replyBytes[3] = 0;
            //Send ACK Block Here
          }
        } else {
          Exception e = new Exception("Invalid request");
          throw e;
        }
        System.out.println("Server: Sending the response to the intermediate host");
        reply = new DatagramPacket(replyBytes, 4, packet.getAddress(), packet.getPort());
        print(reply);//prints the response packet informatione
        DatagramSocket responseSocket = new DatagramSocket();
        
        responseSocket.send(reply);
        System.out.println("Closing sockets, bye");
        
        responseSocket.close();
        socket.close();
        System.out.println("======================================================");
        
      }
    } catch(Exception e) {
      System.out.println("Error in run");
      if(e.getMessage().equals("Invalid request")){
        System.out.println("An invalid request was received, Closing sockets, bye");
        socket.close();
      } else {
        System.out.println("Error wasn't because of an invalid request");
        e.printStackTrace();
        System.exit(1);
      }
      //e.printStackTrace();
      //System.exit(1);
    }
    
  }
  //Functions checks if the message if valid, if it's valid returns true, otherwise returns false
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
    //String str = new String(b1);
    //System.out.println("Filename: "+str);
    
    //System.out.println("received[i]: "+received[i]);
    if(received[i]!=0) return false;
    i++;
    j=i;
    size=0;
    //get the mode
    while(received[i]!=0){
      i++;
      size++;
    }
    byte[] b2 = new byte[size];
    for(int ii =0;ii<size;ii++){
      b2[ii]=received[j];
      j++;
    }
    //String str1 = new String(b2);
    //System.out.println("Mode: "+str1);
    if(received[i]!=0) return false;
    
    return true;
    
  }
  void print(String str){
    //print out the information you're about to send
    System.out.print("Message as text: ");
    System.out.println(str);//print as a string
    
    System.out.print("Message as binary: ");
    byte [] b = str.getBytes();
    System.out.println(Arrays.toString(b));//print as a binary
  }
    void print(DatagramPacket response){
    //print out the information you're about to send
    System.out.print("Message as binary: ");
    System.out.println(Arrays.toString(response.getData()));//print as a binary
    System.out.println("Length: "+response.getLength());
    System.out.println("Response Addres: "+response.getAddress());
    System.out.println("Response port: "+response.getPort());
  }
  
  public static void main(String[] args){
    Server server = new Server();
    server.run();
  }
}