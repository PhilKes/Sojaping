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
import java.util.stream.Collectors;

import static common.Constants.Contexts.*;
import static common.JsonHelper.getPacketFromJson;

public class Server {
    private static final String SOJAPING="sojaping.db";
    public static String SERVER_HOST="192.168.178.26";
    //public static String SERVER_HOST = "141.59.129.129";

    public static int SERVER_PORT=9999;//443;

    private int port;
    private ServerSocket server;
    /**
     * userName -> Connection
     * is used by Server Thread and ServerDispatcher Thread -> synchronized
     */
    private final HashMap<String, Connection> connections;

    private final DatabaseService dbService;
    private final TranslationService translateService;

    public Server(int port, DatabaseService dbService) {
        this.port=port;
        this.connections=new HashMap<>();
        this.dbService=dbService;
        this.translateService=new TranslationService();
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

        /** Start Dispatcher Thread */
        new Thread(new ServerDispatcher(this, connections)).start();
        /** Continouesly accept new Socket connections, add connection to list if CONNECT received*/
        while(true) {
            Socket clientSocket=server.accept();
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
}