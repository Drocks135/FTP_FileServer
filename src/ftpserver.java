

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;
import java.lang.*;
import javax.swing.*;

    public class ftpserver extends Thread {
        private Socket connectionSocket;
        int port;
        int count = 1;

        public ftpserver(Socket connectionSocket) {
            this.connectionSocket = connectionSocket;
        }


        public void run() {
            if (count == 1)
                System.out.println("User connected" + connectionSocket.getInetAddress());
            count++;

            try {
                processRequest();

            } catch (Exception e) {
                System.out.println(e);
            }

        }


        private void processRequest() throws Exception {
            String fromClient;
            String clientCommand;
            byte[] data;
            String frstln;

            while (true) {
                if (count == 1)
                    System.out.println("User connected" + connectionSocket.getInetAddress());
                count++;

                DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                fromClient = inFromClient.readLine();

                //System.out.println(fromClient);
                StringTokenizer tokens = new StringTokenizer(fromClient);

                frstln = tokens.nextToken();
                port = Integer.parseInt(frstln);
                clientCommand = tokens.nextToken();
                //System.out.println(clientCommand);


                if (clientCommand.equals("list:")) {
                    String curDir = System.getProperty("user.dir");

                    Socket dataSocket = new Socket(connectionSocket.getInetAddress(), port);
                    DataOutputStream dataOutToClient =
                            new DataOutputStream(dataSocket.getOutputStream());
                    File dir = new File(curDir);

                    String[] children = dir.list();
                    if (children == null) {
                        // Either dir does not exist or is not a directory
                    } else {
                        for (int i = 0; i < children.length; i++) {
                            // Get filename of file or directory
                            String filename = children[i];

                            if (filename.endsWith(".txt"))
                                dataOutToClient.writeUTF(children[i]);
                            //System.out.println(filename);
                            if (i - 1 == children.length - 2) {
                                dataOutToClient.writeUTF("eof");
                                // System.out.println("eof");
                            }//if(i-1)


                        }//for

                        dataSocket.close();
                        //System.out.println("Data Socket closed");
                    }//else


                }//if list:


                if (clientCommand.equals("get:")) {

                    System.out.println("gets to get");

                    String curDir = "C:/Users/conti/Documents/Fall 2020/CIS 457/Project01-P/SampleCode/src";

                    clientCommand = tokens.nextToken();


                    String fName = clientCommand;

                    Socket dataSocket = new Socket(connectionSocket.getInetAddress(), port);
                    DataOutputStream dataOutToClient = new DataOutputStream(dataSocket.getOutputStream());
                    File dir = new File(curDir);

                    String[] children = dir.list();

                    System.out.println(fName);

                    for(int i = 0; i < children.length; i++){


                        if(children[i].equals(fName)){


                            BufferedReader reader;

                            try{
                                reader = new BufferedReader(new FileReader("C:/Users/conti/Documents/Fall 2020/CIS 457/Project01-P/SampleCode/src/" + clientCommand));
                                String line = reader.readLine();

                                while(line != null){
                                    dataOutToClient.writeUTF(line);
                                    line = reader.readLine();
                                }
                                dataOutToClient.writeUTF("eof");
                                dataSocket.close();
                                break;

                            }

                            catch(IOException e){
                                e.printStackTrace();
                            }


                        }


                    }





                }//main
            }
        }
    }
	

