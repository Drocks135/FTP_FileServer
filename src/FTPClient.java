import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

public class FTPClient {
    private Socket ControlSocket;
    private ServerSocket ReceiveSocket;
    private Socket dataSocket;

    public FTPClient(){
        ControlSocket = new Socket();

    }

    public void ProcessCommand(String command){
        if(ControlSocket.isConnected()){
            String rawCommand = command.substring(0, command.indexOf(":"));

            switch (rawCommand) {
                case "get":
                    GetFileContents(command);
                    break;
                case "stor":
                    StoreFile(command);
                    break;
                case "list":
                    ListServerContents();
                    break;
                case "close":
                    Disconnect();
                    break;
                default:
                    System.out.println("Invalid command");
            }
        }
        else if(command.startsWith("connect"))
            EstablishConnection(command);
    }

    private void EstablishConnection(String command){
        StringTokenizer tokenizedCommand = new StringTokenizer(command);
        tokenizedCommand.nextToken(); //Use the connect command token
        String serverName = tokenizedCommand.nextToken();
        int connectionPort = Integer.parseInt(tokenizedCommand.nextToken());

        try {
            ControlSocket = new Socket(serverName, connectionPort);
            ReceiveSocket = new ServerSocket(ControlSocket.getPort() + 2);
            System.out.println("You are connected to " + serverName);
        } catch (Exception e){
            System.out.println("There was a problem connecting to the server");
        }
    }

    private void ListServerContents(){
        try {
            //int port = ControlSocket.getPort() + 2; //This is the port the list will be receiving data on

            //ServerSocket welcomeData = new ServerSocket(port);

            //System.out.println(port);

            DataOutputStream outToServer = new DataOutputStream(ControlSocket.getOutputStream());
            outToServer.writeBytes( ReceiveSocket.getLocalPort() + " " + "list:" + " " + '\n');

            dataSocket = ReceiveSocket.accept();
            DataInputStream inData = new DataInputStream(new BufferedInputStream(dataSocket.getInputStream()));

            String list;
            while(true){
                list = inData.readUTF();
                if(list.equals("eof"))
                    break;
                System.out.println("    " + inData.readUTF());
            }

            //welcomeData.close();
            //dataSocket.close();
            //inData.close();
            //outToServer.close();

            System.out.println("\nWhat would you like to do next: \nget: file.txt ||  stor: file.txt  || close");

        } catch (Exception e){
            System.out.println("There was a problem connecting to a port\nlist command unable to process");
        }
    }

    private void GetFileContents(String command){
        if(command.matches("(get):\\s\\w+\\.(txt)")){
            StringTokenizer tokenizedCommand = new StringTokenizer(command);
            tokenizedCommand.nextToken(); //Use the command token
            String fileName = tokenizedCommand.nextToken();

            System.out.println(fileName);

            int port = ControlSocket.getPort() + 2; //This is the port the get command will receive data on

            try {
                ServerSocket welcomeData = new ServerSocket(port);
                Socket dataSocket = welcomeData.accept();
                DataInputStream inData = new DataInputStream(new BufferedInputStream(dataSocket.getInputStream()));
                DataOutputStream outToServer = new DataOutputStream(ControlSocket.getOutputStream());
                outToServer.writeBytes(port + " " + "get: " + fileName + " " + '\n');

                File tempFile = new File(fileName);

                if ( !tempFile.exists() )
                {
                    tempFile.createNewFile();
                }

                FileWriter fw = new FileWriter( tempFile);
                BufferedWriter bw = new BufferedWriter( fw );

                System.out.println("File Downloading");

                String nextLine = inData.readUTF();
                while(true){
                    if(nextLine.equals("eof")){
                        System.out.println("File Downloaded");
                        break;
                    } else {
                        bw.write(nextLine);
                        nextLine = inData.readUTF();
                        if(!nextLine.equals("eof"))
                            bw.newLine();
                    }
                }

                fw.close();
                bw.close();
                dataSocket.close();
                welcomeData.close();
                outToServer.close();
            } catch (Exception e){
                System.out.println("There was a problem connecting to a port\nget command unable to process");
            }

        } else
            System.out.println("Invalid get command");
    }

    private void StoreFile(String command){

    }

    private void Disconnect(){
        try {
            ControlSocket.close();
            System.out.println("Server connection closed");
        } catch (Exception e) {
            System.out.println("There was an error closing the connection to the server");
        }
    }

}
