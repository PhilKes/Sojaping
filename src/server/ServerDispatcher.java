package server;

import common.Connection;

import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerDispatcher implements Runnable {

    private final Server server;
    private final HashMap<String, Connection> connections;
    private AtomicBoolean running;
    /** Using ThreadPool for instantiating ServerHandler Threads*/
    private ExecutorService executor;

    public ServerDispatcher(Server server, HashMap<String, Connection> connections) {
        this.server=server;
        this.connections=connections;
        this.running=new AtomicBoolean(true);
        executor = Executors.newFixedThreadPool(5);
    }

    /**
     * Loop through synchronized (with Server) Map connections and check if new data is available
     * -> Dispatch new Packet to ServerHandler
     */
    @Override
    public void run() {
        while(running.get()) {
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
                    Thread.sleep(10);
                }
                catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        executor.shutdownNow();
    }

    public boolean isRunning() {
        return running.get();
    }

    public void setRunning(boolean running) {
        this.running.set(running);
    }
}
