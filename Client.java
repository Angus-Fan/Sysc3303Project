//Group 8
/*
 * This is the client code, the code sends "read" and "write" request to the server.
 * */
import java.net.*;
import java.util.*;
import java.io.*;

public class Client extends Thread{
  private DatagramSocket socket;//both send and receive
  Scanner scanner = null;
  boolean loop = true;
  String numAnswer = "";
  
  String path = "C:/Users/Patrick/Documents/Courses/Sysc3303/Project/Iteration4/Code/Client/";
  
  Client(){
    try{
      socket = new DatagramSocket();
      scanner = new Scanner(System.in);
      
    } catch (Exception e){
      System.out.println("Error in constructor");
    }
  }
  /*
   * Function handles sending a read request to the errorSimulator
   * */
  private void sendReadRequest(String filename){
    System.out.println("\n==================================================");
    System.out.println("Client: Sending a read request");
    InetAddress address = null;
    
    try{
      address = InetAddress.getByName("127.0.0.1");
    } catch(Exception e){
      System.out.println("Error in InetAddress");
    }
    byte[] readRequestBytes = new byte[516];
    
    //set the opcode to 0 and 1
    if(numAnswer.equals("0")){
      readRequestBytes[0] = 0;
      readRequestBytes[1] = 1;
    } else if(numAnswer.equals("1")){
      //Invalid
      readRequestBytes[0] = -2;
      readRequestBytes[1] = -2;
    } else if(numAnswer.equals("2")){
      readRequestBytes[0] = 0;
      readRequestBytes[1] = 1;
    }
    
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
    String mode = "";
    if(numAnswer.equals("0")){
      mode = "octet";
    } else if(numAnswer.equals("1")){
      mode = "octet";
    } else if(numAnswer.equals("2")){
      mode = "notOctet";
    }
    System.out.println("mode: "+mode);
    byte[] b1 = mode.getBytes();
    //
    ++j;
    for(int i =0;i<mode.length();i++){
      readRequestBytes[j]=b1[i];
      j++;
    }
    readRequestBytes[j]=0;
    //print information
    
    String str = new String(readRequestBytes);
    print(readRequestBytes);
    
    
    //Create the readRequest datagramPacket
    DatagramPacket readRequestPacket = new DatagramPacket(readRequestBytes,516,address,23);
    try{
      Thread.sleep(2000);
      //send read request
      socket.send(readRequestPacket);
    } catch (Exception e){
      System.out.println("Error in sending Read request");
    }
    
    boolean loop = true;
  }
  //function sends a read request to the server
  private void read(String filename){
    byte blockNum1 =0;
    byte blockNum2 =1;
    InetAddress address = null;
    
    try{
      address = InetAddress.getByName("127.0.0.1");
    } catch(Exception e){
      System.out.println("Error in InetAddress");
    }
    
    sendReadRequest(filename);
    boolean loop = true;
    FileOutputStream fo = null;
    try{
      fo = new FileOutputStream(path+filename);
    } catch (Exception e){
      System.out.println("Error in opening file");
    }
    
    //keep accepting stuffs from the server until the size of the message is less than 512
    while(loop){
      byte[] dataBytes = new byte[516];
      DatagramPacket dataPacket = new DatagramPacket(dataBytes, 516);
      
      //datapacket received from Server
      try{
        System.out.println("Client: waiting for Data packet");
        socket.receive(dataPacket);
        //print(dataPacket.getData());
        
        System.out.println("Client: Data packet received");
      } catch(IOException e){
        System.out.println("Error in receiving response");
      }
      byte[] dataPacket2 = dataPacket.getData();
      
      
      //---------------------check Illegal TFTP operation ------------------------------------------------------------------
      if((dataPacket2[0]!=0) || (dataPacket2[1]!=3)) {
        System.out.println("Server: an invalid opcode was received, sending error packet");
        byte[] errorBytes = errorPacket(4);
        DatagramPacket errorPacket = new DatagramPacket(errorBytes,516,
                                                        dataPacket.getAddress(),dataPacket.getPort());
        try{
          socket.send(errorPacket);
          socket.close();
        } catch(Exception e){
          System.out.println("Error sending errorPacket");
        }
        return;
      }
      //--------------------------------------------------------------------------------------------------------------------
      //check----------------------------------------------------------------------------------------------------------
      //if block number is less than current block number
      
      if(checkForErrors(dataPacket.getData()) == true){
        System.out.println("Client: Server sent an errorPacket, Bye");
        socket.close();
        break;
      }
      //If the Client received a duplicate package ignore it
      
      
      if(dataPacket2[0]==0 && dataPacket2[1]==3){
        if(dataPacket2[2]<blockNum1){
          System.out.println("Client: duplicate Data block received and, ignored");
          System.out.println("BlockNum1: "+blockNum1);
          System.out.println("BlockNum2: "+blockNum2);
          System.out.println("dataPacket2[2]: "+dataPacket2[2]);
          System.out.println("dataPacket2[3]: "+dataPacket2[3]);
          continue;
        } else if(dataPacket2[2]==blockNum1 && dataPacket2[3]<blockNum2){
          System.out.println("Client: duplicate Data block received, and ignored");
          System.out.println("BlockNum1: "+blockNum1);
          System.out.println("BlockNum2: "+blockNum2);
          System.out.println("dataPacket2[2]: "+dataPacket2[2]);
          System.out.println("dataPacket2[3]: "+dataPacket2[3]);
          continue;
        }
      }
      
      //checks if the size of the data is less than 512
      
      if(checkSize(dataPacket.getData()) == false){
        //send the last acknowledgement
        loop = false;
        System.out.println("Final set of blocks sent");
      }
      //--
      byte[] dataBytes2 = dataPacket.getData();//
      //write to file
      try{
        if(loop==true){
          fo.write(dataBytes2,4, 512);
        } else if(loop=false){
          int n = checkSizeAndReturn(dataBytes2);
          fo.write(dataBytes2,4,n-1);
        }
      } catch (Exception e){
        System.out.println("Error writing to file");
      }
      
      //send acknowledgement
      byte[] ackBytes = new byte[516];
      
      ackBytes[0]=0;
      ackBytes[1]=4;
      ackBytes[2]= dataBytes2[2];
      ackBytes[3]= dataBytes2[3];
      
      System.out.println("Client: Sending acknowledgement");
      DatagramPacket ackPacket = new DatagramPacket(ackBytes, 516,address,dataPacket.getPort());
      try{
        Thread.sleep(2000);
        socket.send(ackPacket);
      } catch(Exception e){
        System.out.println("Error in sending acknowledgement");
      }
      //increase blockNumber
      if(blockNum2==9){
        blockNum1+=1;
        blockNum2=0;
      } else {
        blockNum2++;
      }
    }
    
    try{
      fo.close();
    } catch (Exception e){
      System.out.println("Error closing fileoutputStream");
    }
    
  }
  //Function creates a packet to be sent
  private void sendWriteRequest( String filename){
    DatagramPacket writeRequestPacket = null;
    
    String string = null;
    byte[] writeRequest = new byte[516];
    
    InetAddress address = null;
    try{
      address = InetAddress.getByName("127.0.0.1");
    } catch(Exception e){
      System.out.println("Error in inetAddress");
    }
    //set the opcode to 0 and 1
    if(numAnswer.equals("0")){
      writeRequest[0] = 0;
      writeRequest[1] = 2;
    } else if(numAnswer.equals("1")){
      writeRequest[0] = -2;
      writeRequest[1] = -2;
    } else if(numAnswer.equals("2")){
      writeRequest[0] = 0;
      writeRequest[1] = 2;
    }
    
    //get the filename
    byte[] fileBytes = filename.getBytes();
    int j = 2;
    
    for(int i = 0;i<filename.length();i++){
      writeRequest[j]=fileBytes[i];
      j++;
    }
    writeRequest[j] =0;
    
    //Set the mode
    String mode = "";
    if(numAnswer.equals("0")){
      mode = "octet";
    } else if(numAnswer.equals("1")){
      mode = "octet";
    } else if(numAnswer.equals("2")){
      mode = "notoctet";
    }
    System.out.println("mode: "+mode);
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
    
    //print out the message
    print(writeRequest);
    
    try{
      writeRequestPacket = new DatagramPacket(writeRequest,516,address,23);
      
      Thread.sleep(2000);
      socket.send(writeRequestPacket);
    } catch(Exception e){
      System.out.println("Error in sending writeRequest/sleep");
    }
  }
  
  
  /*
   * Function sends a write request to the ErrorSiumulator
   * */
  private void write(String filename){
    InetAddress address = null;
    FileInputStream fileInputStream = null;
    byte blockNum1 = 0;
    byte blockNum2 =0;
    
    DatagramPacket lastDataPacket = null;
    
    try{
      address = InetAddress.getByName("127.0.0.1");
    } catch (Exception e){
      System.out.println("Error in InetAddress");
    }
    
    System.out.println("\n==================================================");
    System.out.println("Client: Sending a write request");
    sendWriteRequest( filename);
    
    
    //open the file
    try{
      fileInputStream = new FileInputStream(path+filename);
    } catch (Exception e){
      System.out.println("Error in opeingn file inputStream");
    }
    
    while(loop){
      byte[] ackBytes = new byte[516];
      DatagramPacket ackPacket = new DatagramPacket(ackBytes, 516);
      
      System.out.println("Client: waiting for an acknowledgement ");
      boolean innerLoop = true;
      while(innerLoop){
        try{
          socket.setSoTimeout(10000);
          socket.receive(ackPacket);
          innerLoop = false;
        }  catch (SocketException se){
          System.out.println("Client: didn't receive an acknowledgement withing time period, retransmiting...");
          //resend the last thing you sent
          try{
            socket.send(lastDataPacket);
          } catch (Exception eee){
            System.out.println("Client: error sending lastDataPacket");
          }
        } catch (IOException e){
          System.out.println("Error in receiving acknowledgment");
        }
      }
      //Check if the Server sent an error
      if(checkForErrors(ackPacket.getData()) == true){
        System.out.println("Client: Server sent an errorPacket");
        socket.close();
        return;
      }
      
      //check for duplicate ACKPacket
      byte[] checkDuplicate = ackPacket.getData();
      
      if(checkDuplicate[2] < blockNum1){
        System.out.println("Client: duplicate ACK received, and ignored");
        continue;
      } else if(checkDuplicate[2]==blockNum1 && checkDuplicate[3]<blockNum2){
        System.out.println("Client: duplicate ACK received, and ignored");
        continue;
      }
      //-----------------------------------------------------------------------------------------------------------------------
      //check for a duplicate ACK packet for the read request
      if(blockNum1>0 || blockNum2>0){
        if(checkDuplicate[2] ==0 && checkDuplicate[3]==0){
          //Duplicate ACK packet received for read request
          System.out.println("Client: duplicate ACK packet received for read request");
          System.out.println("sending error packet");
          byte[] errorBytes = errorPacket(4);
          DatagramPacket errorPacket = new DatagramPacket(errorBytes,516, address,ackPacket.getPort());
          try{
            socket.send(errorPacket);
          } catch (Exception e){
            System.out.println("Error occored in sending packet");
          }
          continue;
        }
      }
      //------------------------------------------------------------------------------------------------------------------------
      //---------------------check Illegal TFTP operation ------------------------------------------------------------------
      if((checkDuplicate[0]!=0) || checkDuplicate[1]!=3){
        System.out.println("Server: an invalid opcode was received, sending error packet");
        byte[] errorBytes = errorPacket(4);
        DatagramPacket errorPacket = new DatagramPacket(errorBytes,516,
                                                        ackPacket.getAddress(),ackPacket.getPort());
        try{
          socket.send(errorPacket);
          socket.close();
        } catch(Exception e){
          System.out.println("Error sending errorPacket");
        }
        return;
      }
      //--------------------------------------------------------------------------------------------------------------------
      
      System.out.println("Client: acknowledgment received");
      //print the message received
      System.out.println("Client: printing packet information");
      print(ackPacket.getData());
      
      byte[] answer = handleACK(ackPacket.getData());
      
      //System.out.println(answer[0]);
      if(answer[0]==-1){
        System.out.println("System didn't acknowledge, an error occured");
        socket.close();
        return;
      } else {
        //Server has sent back an acknowledgement, send the rest of the file
        
        byte[] dataBytes = new byte[516];
        
        //opcode
        dataBytes[0]=0;
        dataBytes[1]=3;
        //
        if(blockNum2==9){
          blockNum2=0;
          blockNum1+=1;
        } else {
          blockNum2 +=1;
        }
        
        dataBytes[2]=blockNum1;
        dataBytes[3]=blockNum2;
        
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
        
        DatagramPacket dataPacket =  new DatagramPacket(dataBytes,516,address,ackPacket.getPort());
        lastDataPacket = dataPacket;
        //send the file to the server
        try{
          Thread.sleep(2000);
          System.out.println("Client: Sending file bytes");
          socket.send(dataPacket);
        } catch (Exception e){
          System.out.println("Error sending the fileBytes ");
        }
        
        
        //receive last acknowledgement
        if(loop==false){
          byte[] ackBytes2 = new byte[516];
          DatagramPacket ackPacket2 = new DatagramPacket(ackBytes2, 516);
          
          System.out.println("Client: waiting for an acknowledgement ");
          
          try{
            socket.receive(ackPacket2);
          } catch (Exception e){
            System.out.println("Error in receiving acknowledgment");
          }
          //check if the Server sent an error
          if(checkForErrors(ackPacket2.getData()) == true){
            System.out.println("Client: Server sent an errorPacket");
            socket.close();
            return;
          }
          //-----------------------------------------------------------------------------------------------------------------------
          //check for repeated ACK packet for the read request
          if(blockNum1>0 || blockNum2>0){
            if(checkDuplicate[2] ==0 && checkDuplicate[3]==0){
              //Duplicate ACK packet received for read request
              System.out.println("Client: duplicate ACK packet received for read request");
              System.out.println("sending error packet");
              byte[] errorBytes = errorPacket(4);
              DatagramPacket errorPacket = new DatagramPacket(errorBytes,516, address,ackPacket.getPort());
              try{
                socket.send(errorPacket);
              } catch (Exception e){
                System.out.println("Error occored in sending packet");
              }
              continue;
            }
          }
          //------------------------------------------------------------------------------------------------------------------------
          //---------------------check Illegal TFTP operation ------------------------------------------------------------------
          if((checkDuplicate[0]!=0) || checkDuplicate[1]!=3){
            System.out.println("Server: an invalid opcode was received, sending error packet");
            byte[] errorBytes = errorPacket(4);
            DatagramPacket errorPacket = new DatagramPacket(errorBytes,516,
                                                            ackPacket.getAddress(),ackPacket.getPort());
            try{
              socket.send(errorPacket);
              socket.close();
            } catch(Exception e){
              System.out.println("Error sending errorPacket");
            }
            return;
          }
          //--------------------------------------------------------------------------------------------------------------------
          
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
      //System.out.print(received[i]+" ");
      if(received[i]==0) return false;
    }
    return true;
  }
  //check the size and return an int (index of last byte)
  private int checkSizeAndReturn(byte[] received){
    for(int i = 0;i<514;i++){
      if(received[i]==0) return i;
    }
    return -1;
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
      byte[] error = new byte[2];
      error[0]=-1;
      return error;
    }
    
    byte blockNum [] = new byte[2];
    blockNum[0]=response[2];
    blockNum[1]=response[3];
    return blockNum;
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
  //function: errorPacket
  //in: error number
  //out: byte[] to send back
  //desc: takes input for the number and creates a proper errorMsg bytes
  //ERROR PACKET FOLLOWS THIS (OP CODE (2bytes), ERR CODE (2bytes), ERR MSG (string),0 byte)
  public byte[] errorPacket(int errNum){
    int errorMsgLength; 
    byte[] errorMsgBytes;
    byte[] errMsg = new byte[516];
    String errorString;
    
    if(errNum == 4) {
      //  Illegal TFTP operation(might want to add the file name to the parameters)
      errorString = " Illegal TFTP operation"; //like put the file name here
      errorMsgBytes = errorString.getBytes();
      errorMsgLength = (errorString.getBytes().length);
      errMsg[0]=(byte)0;
      errMsg[1]=(byte)5;
      errMsg[2]=(byte)0;
      errMsg[3]=(byte)4;
      
      System.arraycopy(errorMsgBytes,0,errMsg,4,errorMsgBytes.length);
      errMsg[errMsg.length-1]=(byte)0;
    } else if(errNum == 5) {
      //
      errorString = "An unknown transfer ID was sent"; 
      errorMsgBytes = errorString.getBytes();
      errorMsgLength = (errorString.getBytes().length);
      errMsg[0]=0;
      errMsg[1]=5;
      errMsg[2]=0;
      errMsg[3]=5;
      
      System.arraycopy(errorMsgBytes,0,errMsg,4,errorMsgBytes.length);
      errMsg[errMsg.length-1]=(byte)0;
      
    } else {
      System.out.println("This err number you entered is not an errorCode: "+ errNum);
    }
    System.out.println("RETURNING THIS BYTE ARRAY");
    //for(int i=0;i<errMsg.length;i++) {
    //  System.out.print(errMsg[i]);
    //}
    System.out.println();
    return errMsg;
  }
  //check if the client sent an error packet
  Boolean checkForErrors(byte[] data){
    if(data[0] ==0 && data[1]==5){
      return true;
    }
    return false;
  }
  //function 
  public void run(){
    //while(true){
      System.out.println("Hi, what type of request would you like to send: (read), (write) or would you like to (shutdown)");
      //String answer = scanner.nextLine().toLowerCase();
      String answer = "read";
      
      if(answer.equals("read")){
        System.out.println("What is the name of the file");
        //answer = scanner.nextLine().toLowerCase();
        answer = "test.txt";
        System.out.println("Would you like to enter a pathname to that file: (yes or no)");
        //String answer2 = scanner.nextLine().toLowerCase();
        String answer2 = "no";
        
        if(answer2.equals("yes")){
          System.out.println("Please enter the pathname to the file");
          this.path = scanner.nextLine();
        }
        boolean modeBoolean = true;
        while(modeBoolean){
          System.out.println("0 - normal operation, 1 - Invalid TFTP opcode on WRQ or RRQ, 2 - Invalid mode");
          //numAnswer = scanner.nextLine().toLowerCase();
          //numAnswer = "0";
          numAnswer = "0";
          if(numAnswer.equals("0") ||  numAnswer.equals("1") || numAnswer.equals("2")){
            modeBoolean = false;
            System.out.println("numAnswer is: "+numAnswer);
          } else {
            System.out.println("Please, try again");
          }
        }
        
        read(answer);
      } else if(answer.equals("write")){
        System.out.println("What is the name of the file");
        answer = scanner.nextLine().toLowerCase();
        System.out.println("Would you like to enter a pathname: (yes or no)");
        String answer2 = scanner.nextLine().toLowerCase();
        
        if(answer2.equals("yes")){
          System.out.println("Please enter the path name");
          this.path = scanner.nextLine();
        }
        boolean modeBoolean = true;
        
        while(modeBoolean){
          System.out.println("0 - normal operation, 1 - Invalid TFTP opcode on WRQ or RRQ, 2 - Invalid mode");
          numAnswer = scanner.nextLine().toLowerCase();
          if(numAnswer.equals("0")==false &&  numAnswer.equals("1")==false && numAnswer.equals("2")==false){
            System.out.println("Please, try again");
          } else {
            modeBoolean = false;
          }
        }
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
