import java.io.BufferedReader;
import java.io.InputStreamReader;

public class FTPClientInterface {
    public static void main(String argv[]) throws Exception{
        FTPClient client = new FTPClient();
        System.out.println("Welcome to the simple FTP App   \n     Commands\n" +
                "connect servername port# connects to a specified server\n" +
                "list: lists files on server\n" +
                "get: fileName.txt downloads that text file to your current directory\n" +
                "stor: fileName.txt Stores the file on the server\n" +
                "close terminates the connection to the server\n");

        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        String userInput;

        while(true){

            userInput = inFromUser.readLine();
            client.ProcessCommand(userInput);
        }

    }
}
