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

            StringTokenizer tokens = new StringTokenizer(fromClient);

            frstln = tokens.nextToken();
            port = Integer.parseInt(frstln);
            clientCommand = tokens.nextToken();


            if (clientCommand.equals("list:")) {
                String curDir = System.getProperty("user.dir");

                Socket dataSocket = new Socket(connectionSocket.getInetAddress(), port);
                DataOutputStream dataOutToClient =
                        new DataOutputStream(dataSocket.getOutputStream());
                File dir = new File(curDir);

                String[] children = dir.list();
                if (children == null) {
                } else {
                    for (int i = 0; i < children.length; i++) {
                        String filename = children[i];

                        if (filename.endsWith(".txt"))
                            dataOutToClient.writeUTF(children[i]);
                        if (i - 1 == children.length - 2) {
                            dataOutToClient.writeUTF("eof");
                        }


                    }

                    dataSocket.close();
                }


            }

            else if (clientCommand.equals("get:")) {
                clientCommand = tokens.nextToken();

                String fName = clientCommand;

                Socket dataSocket = new Socket(connectionSocket.getInetAddress(), port);
                DataOutputStream dataOutToClient = new DataOutputStream(dataSocket.getOutputStream());

                File sendData = new File(fName);

                if (sendData.exists()) {

                    outToClient.writeUTF("200 OK");

                    BufferedReader reader;

                    try {
                        reader = new BufferedReader(new FileReader(clientCommand));
                        String line = reader.readLine();

                        while (line != null) {
                            dataOutToClient.writeUTF(line);
                            line = reader.readLine();
                        }
                        dataOutToClient.writeUTF("eof");
                        System.out.println("File: " + fName + " sent");
                        dataSocket.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                else {
                    outToClient.writeUTF("505");
                    System.out.println("File: " + fName + " Not Found");
                    dataSocket.close();
                }

            }
            else if (clientCommand.equals("stor:")) {
                clientCommand = tokens.nextToken();

                String fName = clientCommand;

                Socket dataSocket = new Socket(connectionSocket.getInetAddress(), port);

                DataInputStream dataInFromClient = new DataInputStream(new BufferedInputStream(dataSocket.getInputStream()));

                PrintWriter output = new PrintWriter(new File(fName));

                while (true) {
                    String newLine = dataInFromClient.readUTF();

                    if (newLine.equals("eof")) {
                        System.out.println("File: " + fName + " Saved");
                        break;
                    } else {
                        output.println(newLine);
                        output.flush();
                    }
                }
                output.close();
                dataSocket.close();
            }

            else if (clientCommand.equals("close")){

                System.out.println("User " + connectionSocket.getInetAddress() +  " disconnected.");
                connectionSocket.close();
                count = 0;
                break;

            }

        }
    }
}
	

