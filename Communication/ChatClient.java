/**
 * Name: Abhishek Biswas Deep
 * ID: B00864230
 * Assignment 5
 */

//This class is used for communication.
import java.io.*;
import java.net.*;
public class ChatClient {
    private static final int PORT = 1234;
    private static Socket link;
    private static BufferedReader in;
    private static PrintWriter out;
    private static BufferedReader kbd;

    public static void main(String[] args) throws Exception{
        try{

            link = new Socket("127.0.0.1", PORT);

            //Input object gets an input stream from the socket.
            in = new BufferedReader(new InputStreamReader(link.getInputStream()));

            //Output object which is an output stream from the socket.
            out = new PrintWriter(link.getOutputStream(), true);

            //The BufferedReader object at the client side will receive messages sent by the PrintWriter object
            // at the server side, and vice versa.
            kbd = new BufferedReader(new InputStreamReader(System.in));
            String message, response;
            System.out.println("Connected to the chat server");
            System.out.println(in.readLine());
            String name = kbd.readLine();
            out.println(name);
            do{
                System.out.println("Enter message (BYE to quit)");
                message = kbd.readLine();
                out.println(message);
                if (message.equals("")) {
                    response = in.readLine();
                    System.out.println(response);
                }
            }while (!message.equals("BYE"));
        }

        //Catch exceptions if the above does not work.
        catch(UnknownHostException e){System.exit(1);}
        catch(IOException e){System.exit(1);}
        finally{
            try{
                if (link!=null){
                    System.out.println("Closing");
                    link.close();
                }
            }
            catch(IOException e){System.exit(1);}
        }
    }//end main
}//end class MultiEchoClient
