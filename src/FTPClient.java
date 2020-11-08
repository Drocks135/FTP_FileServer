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
        int port1 = 1221;
        int port = 1200;
        String statusCode;
        boolean clientgo = true;

        System.out.println("Welcome to the simple FTP App   \n     Commands  \nconnect servername port# connects to a specified server \nlist: lists files on server \nget: fileName.txt downloads that text file to your current directory \nstor: fileName.txt Stores the file on the server \nclose terminates the connection to the server");
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        sentence = inFromUser.readLine();
        StringTokenizer tokens = new StringTokenizer(sentence);


        if (sentence.startsWith("connect")) {
            String serverName = tokens.nextToken(); // pass the connect command
            serverName = tokens.nextToken();
            port1 = Integer.parseInt(tokens.nextToken());
            System.out.println("You are connected to " + serverName);
            Socket ControlSocket = new Socket(serverName, port1);
            while (isOpen && clientgo) {

                sentence = inFromUser.readLine();
                DataOutputStream outToServer = new DataOutputStream(ControlSocket.getOutputStream());
                DataInputStream inFromServer = new DataInputStream(new BufferedInputStream(ControlSocket.getInputStream()));

                if (sentence.equals("list:")) {

                    port = port + 2;
                    System.out.println(port);
                    ServerSocket welcomeData = new ServerSocket(port);


                    System.out.println("\n \n \nThe files on this server are:");
                    outToServer.writeBytes(port + " " + sentence + " " + '\n');

                    Socket dataSocket = welcomeData.accept();
                    DataInputStream inData = new DataInputStream(new BufferedInputStream(dataSocket.getInputStream()));
                    while (notEnd) {
                        modifiedSentence = inData.readUTF();
                        if (modifiedSentence.equals("eof"))
                            break;
                        System.out.println("	" + modifiedSentence);
                    }

                    welcomeData.close();
                    dataSocket.close();
                    printCommands();

                }

                else if (sentence.startsWith("get: ")) {
                    String fName = sentence.substring(5);

                    System.out.println(fName);

                    port = port + 2;
                    System.out.println(port);
                    ServerSocket welcomeData = new ServerSocket(port);

                    outToServer.writeBytes(port + " " + sentence + " " + '\n');

                    Socket dataSocket = welcomeData.accept();

                    modifiedSentence = "";

                     boolean f = true;
                     while (notEnd) {

                        if(f){
                            System.out.println("\n \n \nDownloading File");
                            f = false;
                        }

                        String newInp = inFromServer.readUTF();

                        if (newInp.equals("eof")){
                            System.out.println("File Downloaded");
                            break;
                        }
                        else{
                            modifiedSentence += newInp +"\n";
                        }

                    }

                     //need to fix directory later
                    File tempFile = new File(fName);

                    if ( ! tempFile.exists() )
                    {
                        tempFile.createNewFile();
                    }

                    System.out.println(modifiedSentence);

                    FileWriter fw = new FileWriter( tempFile);
                    BufferedWriter bw = new BufferedWriter( fw );
                    bw.write(modifiedSentence);
                    bw.close();
                    fw.close();

                    welcomeData.close();
                    dataSocket.close();

                    printCommands();
                }
                else if(sentence.startsWith("stor: ")){
                    String fName = sentence.substring(5);

                    System.out.println(fName);

                    port = port + 2;
                    System.out.println(port);
                    ServerSocket welcomeData = new ServerSocket(port);

                    outToServer.writeBytes(port + " " + sentence + " " + '\n');

                    Socket dataSocket = welcomeData.accept();



                }

                else {
                    if (sentence.equals("close")) {
                        clientgo = false;
                        System.out.println("goodbye.");
                    }
                    else
                    System.out.print("No server exists with that name or server not listening on that port try agian");

                }
            }
        }

    }

    public static void printCommands(){
        System.out.println("\nWhat would you like to do next: \nget: file.txt ||  stor: file.txt  || close");
    }
}