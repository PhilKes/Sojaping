package server;

import common.Util;
import common.data.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static common.Constants.Contexts.*;
import static common.JsonHelper.getPacketFromJson;

public class Server {
    private static final String SOJAPING="sojaping.db";

    public static String SERVER_HOST="141.59.128.171";
//    public static String SERVER_HOST="192.168.178.26";
//    public static String SERVER_HOST="10.0.75.1";


    public static int SERVER_PORT=9999;//443;

    private int port;
    private ServerSocket server;
    private AtomicBoolean running;
    /**
     * userName -> Connection
     * is used by Server Thread and ServerDispatcher Thread -> synchronized
     */
    private final HashMap<String, Connection> connections;

    private final DatabaseService dbService;
    private final TranslationService translateService;

    private final ServerDispatcher dispatcher;

    public Server(int port, DatabaseService dbService) {
        this.port=port;
        this.connections=new HashMap<>();
        this.dbService=dbService;
        this.translateService=new TranslationService();
        dispatcher=new ServerDispatcher(this, connections);
        running=new AtomicBoolean(true);
    }

    public static void main(String[] args) throws IOException {
        if(args.length>0) {
            SERVER_PORT=Integer.parseInt(args[0]);
        }
        new Server(SERVER_PORT, new DatabaseService(SOJAPING)).run();
    }

    private void run() throws IOException {
        InetAddress localHost = InetAddress.getLocalHost();
        String inetAddress=localHost.getHostAddress();
        server=new ServerSocket(port, 50, InetAddress.getByName(inetAddress)) {
            protected void finalize() throws IOException {
                this.close();
            }
        };
        InterfaceAddress network=NetworkInterface.getByInetAddress(localHost)
                .getInterfaceAddresses().get(0);
        System.out.println("Server started on Host: " + inetAddress + " Port: " + port);
        System.out.println("Network: "+network.getAddress()+"\tMask: /"+network.getNetworkPrefixLength());

        /** Start Command Handler Thread */
        new Thread(this::handleCommands).start();
        /** Start Dispatcher Thread */
        new Thread(dispatcher).start();

        /** Continouesly accept new Socket connections, add connection to list if CONNECT received*/
        while(running.get()) {
            Socket clientSocket=null;
            try {
                 clientSocket=server.accept();
            }catch(SocketException e){
                System.out.println("Interrupted Accept Connection");
                continue;
            }
            /** Do not accept connections from outside the network of the Server */
            if(!Util.sameNetwork(localHost,clientSocket.getInetAddress(),network.getNetworkPrefixLength())){
                System.err.println("Refused external connection from: "+clientSocket.getInetAddress().getHostAddress());
                continue;
            }
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
        System.out.println("Shutting down server...");
        broadcastPacket(SHUTDOWN,"Shutdown");
        dispatcher.setRunning(false);
    }

    /** Runnable for reading commands in Console */
    private void handleCommands() {
        Scanner in = new Scanner(System.in);
        while(in.hasNextLine()) {
            String command=in.nextLine();
            switch(command.toLowerCase()) {
                case "stop":
                    setRunning(false);
                    try {
                        server.close();
                    }
                    catch(IOException e) {
                        e.printStackTrace();
                    }
                    in.close();
                    return;
                case "clients":
                    System.out.println("Connected clients:");
                    synchronized (connections) {
                        if(connections.isEmpty()) {
                            System.out.println("No connections...");
                        }else {
                            connections.forEach((k, v) -> {
                                System.out.println(k+" ("+v.getNickname()+")\t" + (v.isLoggedIn() ? v.getLoggedAccount() : "Not logged in"));
                            });
                        }
                    }
                    break;
                default:
                    System.err.println("Invalid COMMAND entered: " + command);
                    break;
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
            return account;
        }
        else {
            throw new Exception("Invalid credentials");
        }
    }

    /**
     * Links Connection to Account, updates connections HashMap
     */
    public void setConnectionAccount(Connection connection, Account account) {
        /** Replace clientIP with newly logged in account.userName */
        synchronized (connections) {
            connections.remove(connection.getNickname());
            connection.setLoggedAccount(account);
            connections.put(account.getUserName(), connection);
        }

    }

    /**
     * User logs out, remove from connections
     */
    public void removeConnectionAccount(Connection connection, Account account) {
        if(account==null) {
            connections.remove(connection.getNickname());
        }
        else {
            connections.remove(connection.getLoggedAccount().getUserName());
        }
    }

    public void registerUser(Account account) throws Exception {
        this.dbService.insertAccount(account);
    }

	public void updateUser(Account account) {
        this.dbService.updateAccount(account);
	}

	public void deleteUser(Account account) {
		this.dbService.deleteAccount(account);
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
        Connection receiverCon=connections.get(receiver);
        Profile receiverProfile= receiverCon.getLoggedAccount().getProfile();

        //Connection senderCon=connections.get(message.getSender());
        //Profile senderProfile= senderCon.getLoggedAccount().getProfile();
        /** Only check translation if user wants message to be translated */
        if(message.isTranslate()) {
            /** Identify message's original language*/
            String msgLanguage=translateService.identifyLanguage(message.getText());
            if(msgLanguage!=null) {
                String receiverLanguage=receiverProfile.getLanguages().get(0);
                /** Check if receiver doesnt speak message's language */
                if(!msgLanguage.equals(receiverLanguage)) {
                    /** Translate to receiver's language*/
                    String translated=translateService.translate(message.getText(), msgLanguage, receiverLanguage);
                    message.putTranslation(receiverLanguage, translated);
                    /** Store original message text and langauge*/
                    message.setOriginalLang(msgLanguage);
                    message.setOriginalText(message.getText());
                    /** Set text to translated text for receiver*/
                    message.setText(translated);
                }
            }
        }
        sendToUser(receiverCon, MESSAGE_RECEIVED, message);
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

    public boolean isRunning() {
        return running.get();
    }

    public void setRunning(boolean running) {
        this.running.set(running);
    }
}