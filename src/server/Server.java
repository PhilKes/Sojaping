package server;

import common.Util;
import common.data.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import static common.Constants.Contexts.*;
import static common.JsonHelper.getPacketFromJson;

public class Server {
    private static final String SOJAPING="sojaping.db";
    //public static String SERVER_HOST="192.168.178.26";
    public static String SERVER_HOST = "192.168.0.190";

    public static int SERVER_PORT=9999;//443;

    private int port;
    private ServerSocket server;
    /**
     * userName -> Connection
     * is used by Server Thread and ServerDispatcher Thread -> synchronized
     */
    private final HashMap<String, Connection> connections;

    private TranslationService translationService;
    private DatabaseService dbService;

    public Server(int port, DatabaseService dbService) {
        this.port=port;
        this.connections=new HashMap<>();
        this.dbService=dbService;
    }

    public static void main(String[] args) throws IOException {
        if(args.length>0) {
            SERVER_PORT=Integer.parseInt(args[0]);
        }
        new Server(SERVER_PORT, new DatabaseService(SOJAPING)).run();
    }

    private void run() throws IOException {
        String inetAddress=InetAddress.getLocalHost().getHostAddress();
        server=new ServerSocket(port, 50, InetAddress.getByName(inetAddress)) {
            protected void finalize() throws IOException {
                this.close();
            }
        };
        System.out.println("Server started on Host: " + inetAddress + " Port: " + port);

        /** Start Dispatcher Thread */
        new Thread(new ServerDispatcher(this, connections)).start();
        /** Continouesly accept new Socket connections, add connection to list if CONNECT received*/
        while(true) {
            Socket clientSocket=server.accept();
            /** Receive connectPacket with Clients IP as data, add to connection list*/
            Packet connectPacket=getPacketFromJson(new Scanner(clientSocket.getInputStream(), "UTF-8").nextLine());
            if(connectPacket==null) {
                clientSocket.close();
                continue;
            }
            if(connectPacket.getContext().equals(CONNECT)) {
                String clientIP=connectPacket.getData();
                System.out.println("New Client: " + clientIP);
                try {
                    Connection newConnection=new Connection(clientSocket, clientSocket.getInetAddress().getHostAddress());
                    /** Temporarily put clientIP as userName until client loggs into an Account */
                    synchronized (connections) {
                        connections.put(clientIP, newConnection);
                    }
                    sendToUser(newConnection, CONNECT_SUCCESS, "Hello " + clientIP);
                }
                catch(IOException e) {
                    e.printStackTrace();
                    continue;
                }
                /** Main Thread: (Server)Accept connections,
                 /	2.Thr: (ServerDispatcher) Loop through InputStreams
                 /				-> dispatch Handler Thread (ServerHandler) for individual new Packets */
            }
        }
    }

    /**
     * Checks if loginUser has valid credentials, returns Account from DB
     * throws Exception if invalid credentials
     */
    public Account loginUser(LoginUser loginUser) throws Exception {
        //LoginUser loginUser = JsonHelper.convertJsonToObject(accountOrLoginAsJson);
        Account account=this.dbService.getAccountByLoginUser(loginUser);
        if(account!=null) {
            if(account.getPassword().equals(loginUser.getPassword())) {
                return account;
            }
            else {
                throw new Exception("Invalid password");
            }
        }
        else {
            throw new Exception("Unknown username");
        }
    }

    /**
     * Links Connection to Account, updates clients HashMap
     */
    //TODO Multiple logged in instances on same PC?(Next Sprint)
    public void setLoggedUser(Connection connection, Account account) {
        /** Replace clientIP with newly logged in account.userName */
        synchronized (connections) {
            /** Logout */
            if(account==null) {
                connections.remove(connection.getLoggedAccount().getUserName());
            }
            else {
                connections.remove(connection.getNickname());
                connection.setLoggedAccount(account);
                connections.put(account.getUserName(), connection);
            }
        }
    }

    public void registerUser(Account account) throws Exception {
        this.dbService.insertAccount(account);
    }

    /**
     * Send object as JSON through user OutputStream
     */
    public void sendToUser(Connection connection, String context, Object object) {
        Packet packet=new Packet(context, object);
        /** Use PrintWriter with UTF-8 Charset*/
        PrintWriter out=new PrintWriter(
                new BufferedWriter(new OutputStreamWriter(
                        connection.getOutStream(), StandardCharsets.UTF_8)), true);
        out.println(packet.getJson());
        out.flush();
        Util.logPacket(false, connection, packet);
    }

    /**
     * Try to send message to receiver if present, return if message was sent
     */
    public boolean sendMessage(Message message) {
        String receiver=message.getReceiver();
        if(!connections.containsKey(receiver)) {
            return false;
        }
        Connection con=connections.get(receiver);
        sendToUser(con, MESSAGE_RECEIVED, message);
        return true;
    }

    public void broadcastPacket(String context, Object data) {
        connections.values().stream()
                .filter(c -> c.isLoggedIn())
                .forEach(c -> sendToUser(c, context, data));
    }

    public void broadcastMessages(Message message) {
        this.connections.values().stream()
                .filter(c -> c.isLoggedIn())
                .forEach(client -> {
                            if(!message.getSender().equals(client.getLoggedAccount().getUserName())) {
                                sendToUser(client, MESSAGE_RECEIVED, message);
                            }
                        }
                );
    }

    public List<Profile> getOnlineUsers() {
        synchronized (connections) {
            List<Profile> userList=new ArrayList<>();
            userList.addAll(connections.values().stream()
                    .filter(c -> c.isLoggedIn())
                    .map(c -> c.getLoggedAccount().getProfile())
                    .collect(Collectors.toList()));
            return userList;
        }
    }

    public Connection getConnectionOfUser(String userName) {
        return connections.get(userName);
    }
}