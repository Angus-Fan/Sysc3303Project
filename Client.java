1
2
3
4
5
6
7
8
9
10
11
12
13
14
15
16
17
18
19
20
21
22
23
24
25
26
27
28
29
30
31
32
33
34
35
36
37
38
39
40
41
42
43
44
45
46
47
48
49
50
51
52
53
54
55
56
57
58
59
60
61
62
63
64
65
66
67
68
69
70
71
72
73
74
75
76
77
78
79
80
81
82
83
84
85
86
87
88
89
90
91
92
93
94
95
96
97
98
99
100
101
102
103
104
105
106
107
108
109
110
111
112
113
114
115
116
117
118
119
120
121
122
123
124
125
126
127
128
129
130
131
132
133
134
135
136
137
138
139
140
141
142
143
144
145
146
147
148
149
150
151
152
153
154
155
156
157
158
159
160
161
162
163
164
165
166
167
168
169
170
171
172
173
174
175
176
177
178
179
180
181
182
183
184
185
186
187
188
189
190
191
192
193
194
195
196
197
198
199
200
201
202
203
204
205
206
207
208
209
210
211
212
213
214
215
216
217
218
219
220
221
222
223
224
225
226
227
228
229
230
231
232
233
234
235
236
237
238
239
240
241
242
243
244
245
246
247
248
249
250
251
252
253
254
255
256
257
258
259
260
261
262
263
264
265
266
267
268
269
270
271
272
273
274
275
276
277
278
279
280
281
282
283
284
285
286
287
288
289
290
291
292
293
294
295
296
297
298
299
300
301
302
303
304
305
306
307
308
309
310
311
312
313
314
315
316
317
318
319
320
321
322
323
324
325
326
327
328
329
330
331
332
333
334
335
336
337
338
339
340
341
342
343
344
345
346
347
348
349
350
351
352
353
354
355
356
357
358
359
360
361
362
363
364
365
366
367
368
369
370
371
372
373
374
375
376
377
378
379
380
381
382
383
384
385
386
387
388
389
390
391
392
393
394
395
396
397
398
399
400
401
402
403
404
405
406
407
408
409
410
411
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
  Scanner scanner = null;
  boolean loop = true;
   
  String path = null;
   
  Client(){
    try{
      socket = new DatagramSocket();
      scanner = new Scanner(System.in);
      path = "C:/Users/Patrick/Documents/Courses/Sysc3303/Project/Iteration3/Code/";
       
    } catch (Exception e){
      System.out.println("Error in constructor");
    }
  }
  /*
   * Function sends a read request to the ErrorSimulator
   * */
  private void read(String filename){
    byte blockNum1 =0;
    byte blockNum2 =1;
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
       
      //check
      //if block number is less than current block number, if the opcode is invalid
       
       
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
      //
      byte[] a = dataPacket.getData();
      //check opcode
      //check block number
      if(a[2]<blockNum1){
         
      }else if(a[2]==blockNum1 && a[3]<blockNum2){
        System.out.println("Client: Duplicate...");
        //ignore
        System.out.println("Client: ignoring duplicate");
      }
      //--
      byte[] dataBytes2 = dataPacket.getData();
       
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
         
        DatagramPacket p =  new DatagramPacket(dataBytes,516,address,ackPacket.getPort());
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
