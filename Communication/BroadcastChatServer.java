/**
 * Name: Abhishek Biswas Deep
 * ID: B00864230
 * Assignment 5
 */

//This class is used to communicate with the other clients.
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class BroadcastChatServer
{
    //Declaring the ServerSocket variable and the port number for the server(constant).
    private static ServerSocket serverSock;
    private static final int PORT = 1234;

    //Declaring the number of clients.
    private static final int maxNumberOfClients = 5;

    //Creating a counter to keep count the number of clients.
    private static final CountDownLatch signal = new CountDownLatch(maxNumberOfClients);

    //Creating variables to search and go through all the clients who are going to join.
    private static final HashMap<ClientHandler, PrintWriter> clientHandlers = new HashMap<>();
    private static final LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();
    private static Map<ClientHandler, Integer> messageSent = Collections.synchronizedMap(new HashMap<ClientHandler, Integer>());

    //The main method will create the ServerSocket object and listens to inputs from multiple clients.
    public static void main(String[] args) throws IOException, InterruptedException {
        try{
            //Creating ServerSocket object.
            serverSock=new ServerSocket(PORT);
        }
        catch (IOException e)
        {
            System.out.println("Can't listen on " + PORT);
            System.exit(1);
        }
        int clientNumber = 0;
        do
        {
            //Creating socket object.
            Socket client = null;
            System.out.println("Listening for connection...");
            try{

                //For each client detected, start the client handler.
                client = serverSock.accept();
                System.out.println("New client accepted");
                ClientHandler handler = new ClientHandler(client, signal, queue, messageSent);
                handler.start();
                clientNumber++;
                messageSent.put(handler, 0);
                clientHandlers.put(handler, new PrintWriter(client.getOutputStream(),true));
            }
            catch (IOException e)
            {
                //Otherwise, this is the message executed.
                System.out.println("Accept failed");
                System.exit(1);
            }
            System.out.println("Connection successful");
            System.out.println("Listening for input ...");
        }while(clientNumber != maxNumberOfClients);
        signal.await();
        while (Thread.activeCount() > 1) {

            //This is providing a timer.
            String message = queue.poll(5, TimeUnit.MINUTES);

            //This is making sure the different messages from one client are sent to all the other clients.
            //This is also informing the other clients who just joined.
            for (Map.Entry<ClientHandler, PrintWriter> entry : clientHandlers.entrySet()) {
                ClientHandler handler = entry.getKey();
                PrintWriter out = entry.getValue();
                if (handler.isAlive()) {
                    assert message != null;
                    if (!(message.startsWith("Message from " + handler.getName()))
                            && !(message.startsWith(handler.getName() + " has joined"))) {
                        out.println(message);
                        messageSent.put(handler, messageSent.get(handler) + 1);
                    }
                }
            }

            if (message != null) {
                System.out.println(message);
            }

        }
        System.out.println("Closing connection...");

    } //end main
} //end class MultiEchoServer

//This is a support class that extends Thread, runs the client thread and sends and receives messages.
class ClientHandler extends Thread
{
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private CountDownLatch signal;
    private LinkedBlockingQueue<String> queue;
    private Map<ClientHandler, Integer> messageSent;

    public ClientHandler(Socket socket, CountDownLatch signal, LinkedBlockingQueue<String> queue, Map<ClientHandler, Integer> messageSent)
    {
        //Creating the mirror for each client.
        client = socket;
        try
        {
            this.signal = signal;
            this.queue = queue;
            this.messageSent = messageSent;
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new PrintWriter(client.getOutputStream(),true);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void run()
    {
        //Receiving data from and sending data to client.
        try
        {
            out.println("Enter name: ");
            String received = in.readLine();
            setName(received);
            queue.add(getName() + " has joined");
            signal.countDown();
            signal.await();
            do
            {
                received = in.readLine();
                if (!(received.equals(""))) {
                    queue.add("Message from " + getName() + ": " + received);
                } else {
                    if (messageSent.get(this) == 0) {
                        out.println("No message from other client.");
                    } else {
                        messageSent.put(this, messageSent.get(this) - 1);
                    }
                }
            }while (!received.equals("BYE"));
        }
        catch(IOException e)
        {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally
        {
            //Closing the connections.
            try
            {
                if(client!=null)
                {
                    System.out.println("Closing down connection...");
                    client.close();
                }
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
    }//end run
}//end ClientHandler class
