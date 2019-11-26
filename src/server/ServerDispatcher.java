package server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ServerDispatcher implements Runnable {

    private final Server server;
    private final HashMap<String, Connection> connections;
    private boolean running;
    private Executor executor;

    public ServerDispatcher(Server server, HashMap<String, Connection> connections) {
        this.server=server;
        this.connections=connections;
        this.running=true;
        executor = Executors.newFixedThreadPool(5);
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
                            executor.execute(new ServerHandler(server, connection, sc.nextLine()));
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
