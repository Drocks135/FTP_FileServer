import java.io.*;
import java.net.*;
import java.nio.Buffer;
import java.util.*;
import java.lang.*;

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


            else if (clientCommand.equals("get:")) {

                System.out.println("gets to get");

                String curDir = System.getProperty("user.dir");

                clientCommand = tokens.nextToken();


                String fName = clientCommand;

                Socket dataSocket = new Socket(connectionSocket.getInetAddress(), port);
                DataOutputStream dataOutToClient = new DataOutputStream(dataSocket.getOutputStream());
                File dir = new File(curDir);

                String[] children = dir.list();

                System.out.println(fName);

                for (int i = 0; i < children.length; i++) {


                    if (children[i].equals(fName)) {

                        BufferedReader reader;

                        try {
                            reader = new BufferedReader(new FileReader(clientCommand));
                            String line = reader.readLine();

                            while (line != null) {
                                dataOutToClient.writeUTF(line);
                                line = reader.readLine();
                            }
                            dataOutToClient.writeUTF("eof");
                            dataSocket.close();
                            break;

                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                    }


                }


            } else if (clientCommand.equals("stor:")) {

                System.out.println("stores to store.");

                clientCommand = tokens.nextToken();

                String fName = clientCommand;

                Socket dataSocket = new Socket(connectionSocket.getInetAddress(), port);

                DataInputStream dataInFromClient = new DataInputStream(new BufferedInputStream(dataSocket.getInputStream()));

                File saveFile = new File(fName);

                if (!(saveFile.createNewFile())) {
                    // System.out.println("File Exists. Would you to overwrite it? Y/N"); // TODO: Add if we have time otherwise just save over the file regardless.
                } else {
                    PrintWriter output = new PrintWriter(new File(fName));

                    boolean status = true;

                    while (true) {

                        if (status) {
                            System.out.println("File Created Saving..... ");
                            status = false;
                        }

                        String newLine = dataInFromClient.readUTF();

                        if (newLine.equals("eof")) {
                            System.out.println("File " + fName + " Saved");
                            break;
                        } else {
                            output.println(newLine);
                            output.flush();
                        }
                    }

                    output.close();
                    dataSocket.close();

                }
            }

        }
    }
}
	

