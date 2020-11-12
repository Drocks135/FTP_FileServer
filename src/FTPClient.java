import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;
import java.lang.*;
import javax.swing.*;
class FTPClient {

    public static void main(String argv[]) throws Exception {
        String sentence;
        String modifiedSentence;
        boolean isOpen = true;
        int number = 1;
        boolean notEnd = true;
        int dataConnection = 1220;
        int controlConnection;
        String statusCode;
        boolean clientGo = true;

        System.out.println("Welcome to the simple FTP App   \n---Commands---" +
                "\n| connect servername controlConnection# connects to a specified server " +
                "\n| list: lists files on server " +
                "\n| get: fileName.txt downloads that text file to your current directory " +
                "\n| stor: fileName.txt Stores the file on the server " +
                "\n| close terminates the connection to the server");
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        sentence = inFromUser.readLine();
        StringTokenizer tokens = new StringTokenizer(sentence);


        if (sentence.startsWith("connect")) {
            String serverName = tokens.nextToken(); // pass the connect command
            serverName = tokens.nextToken();
            controlConnection = Integer.parseInt(tokens.nextToken());
            Socket ControlSocket = new Socket(serverName, controlConnection);
            if (ControlSocket.isConnected())
                System.out.println("You are connected to " + serverName);
            while (isOpen && clientGo) {

                sentence = inFromUser.readLine();
                DataOutputStream outToServer = new DataOutputStream(ControlSocket.getOutputStream());
                DataInputStream inFromServer = new DataInputStream(new BufferedInputStream(ControlSocket.getInputStream()));

                if (sentence.equals("list:")) {
                    ServerSocket welcomeData = new ServerSocket(dataConnection);


                    System.out.println("\n\nThe files on this server are:");
                    outToServer.writeBytes(dataConnection + " " + sentence + " " + '\n');

                    Socket dataSocket = welcomeData.accept();
                    DataInputStream inData = new DataInputStream(new BufferedInputStream(dataSocket.getInputStream()));
                    while (notEnd) {
                        modifiedSentence = inData.readUTF();
                        if (modifiedSentence.equals("eof"))
                            break;
                        System.out.println("  " + modifiedSentence);
                    }

                    welcomeData.close();
                    dataSocket.close();
                    printCommands();

                }

                else if (sentence.startsWith("get: ")) {
                    String fName = sentence.substring(5);

                    ServerSocket welcomeData = new ServerSocket(dataConnection);

                    outToServer.writeBytes(dataConnection + " " + sentence + " " + '\n');

                    Socket dataSocket = welcomeData.accept();
                    DataInputStream inData = new DataInputStream(new BufferedInputStream(dataSocket.getInputStream()));
                    modifiedSentence = "";

                    statusCode = inFromServer.readUTF();

                    boolean status = true;
                    if (statusCode.equals("200 OK")) {
                        while (notEnd) {

                            if (status) {
                                System.out.println("\n \nDownloading File....\n");
                                status = false;
                            }

                            String newInp = inData.readUTF();

                            if (newInp.equals("eof")) {
                                System.out.print("File Downloaded");
                                break;
                            } else {
                                modifiedSentence += newInp + "\n";
                            }

                        }


                        //need to fix directory later
                        File tempFile = new File(fName);

                        if (!tempFile.exists()) {
                            tempFile.createNewFile();
                        }

                        FileWriter fw = new FileWriter(tempFile);
                        BufferedWriter bw = new BufferedWriter(fw);
                        bw.write(modifiedSentence);
                        bw.close();
                        fw.close();


                    }
                    else{
                        System.out.println(statusCode + " Bad Response");
                    }

                    welcomeData.close();
                    dataSocket.close();
                    printCommands();
                }

                else if(sentence.startsWith("stor: ")){

                    String fName = sentence.substring(6);

                    File storeFile = new File(fName);
                    if(storeFile.exists()) {

                        ServerSocket messageData = new ServerSocket(dataConnection);

                        outToServer.writeBytes(dataConnection + " " + sentence + " " + '\n');

                        Socket dataSocket = messageData.accept();

                        DataOutputStream outData = new DataOutputStream(dataSocket.getOutputStream());

                        System.out.println("\n File Uploading to server...Please wait...");

                        Scanner contents = new Scanner(storeFile);
                        String line = "";

                        try {


                            line = contents.nextLine();

                            while (contents.hasNextLine()) {
                                outData.writeUTF(line);
                                line = contents.nextLine();
                            }
                            outData.writeUTF(line);
                            outData.writeUTF("eof");

                            dataSocket.close();
                            contents.close();
                            messageData.close();

                            System.out.print("\nUploading file Complete...");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else{
                        System.out.println("\nERROR: Unable to store File was not found!!");
                    }

                    printCommands();

                }

                else if (sentence.equals("close")) {

                        outToServer.writeBytes(dataConnection + " " + sentence + " " + '\n');
                        outToServer.close();
                        ControlSocket.close();


                        clientGo = false;
                        isOpen = false;

                        System.out.println("goodbye.");
                    }
                else{
                    System.out.println("ERROR: No command exists with that name or server not listening on that controlConnection try again");
                    printCommands();

                }
            }
        }

    }

    public static void printCommands(){
        System.out.println("\nWhat would you like to do next: \nget: file.txt ||  stor: file.txt  || close");
    }
}