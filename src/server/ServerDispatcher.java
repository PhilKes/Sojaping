package server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

public class ServerDispatcher implements Runnable {

    private final Server server;
    private final HashMap<String, Connection> connections;
    private boolean running;

    public ServerDispatcher(Server server, HashMap<String, Connection> connections) {
        this.server=server;
        this.connections=connections;
        this.running=true;
    }

    /**
     * Loop through synchronized (with Server) Map connections and check if new data is available
     * -> Dispatch new Packet to ServerHandler
     */
    @Override
    public void run() {
        while(running) {
            synchronized (connections) {
                for(Connection connection : connections.values()) {
                    try {
                        /** If data is available start ServerHandler to handle the Packet */
                        if(connection.getInputStream().available()>0) {
                            Scanner sc=new Scanner(connection.getInputStream(), "UTF-8");
                            //TODO use ThreadPool
                            new Thread(new ServerHandler(server, connection, sc.nextLine())).start();
                        }
                    }
                    catch(IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    Thread.sleep(50);
                }
                catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
