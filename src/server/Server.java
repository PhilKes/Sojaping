package server;

import common.Connection;
import common.Constants;
import common.Util;
import common.Util.PacketException;
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
    private final MailService mailService;

    private final ServerDispatcher dispatcher;

    public Server(int port, DatabaseService dbService) {
        this.port = port;
        this.connections = new HashMap<>();
        this.dbService = dbService;
        this.translateService = new TranslationService();
        this.mailService = new MailService();
        dispatcher = new ServerDispatcher(this, connections);
        running = new AtomicBoolean(true);
    }

    public static void main(String[] args) throws IOException {
        if(args.length>0) {
            Constants.SERVER_PORT=Integer.parseInt(args[0]);
        }
        new Server(Constants.SERVER_PORT, new DatabaseService(SOJAPING)).run();
    }

    private void run() throws IOException {
        InetAddress localHost=InetAddress.getLocalHost();
        String inetAddress=localHost.getHostAddress();
        server=new ServerSocket(port, 50, InetAddress.getByName(inetAddress)) {
            protected void finalize() throws IOException {
                this.close();
            }
        };
        InterfaceAddress network=NetworkInterface.getByInetAddress(localHost)
                .getInterfaceAddresses().get(0);
        System.out.println("Server started on Host: " + inetAddress + " Port: " + port);
        System.out.println("Network: " + network.getAddress() + "\tMask: /" + network.getNetworkPrefixLength());

        /** Start Command Handler Thread */
        new Thread(this::handleCommands).start();
        /** Start Dispatcher Thread */
        new Thread(dispatcher).start();

        /** Continouesly accept new Socket connections, add connection to list if CONNECT received*/
        while(running.get()) {
            Socket clientSocket=null;
            try {
                clientSocket=server.accept();
            }
            catch(SocketException e) {
                System.out.println("Interrupted Accept Connection");
                continue;
            }
            /** Do not accept connections from outside the network of the Server */
            if(!Util.sameNetwork(localHost, clientSocket.getInetAddress(), network.getNetworkPrefixLength())) {
                System.err.println("Refused external connection from: " + clientSocket.getInetAddress().getHostAddress());
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
                    putIPConnection(newConnection);
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
        broadcastPacket(SHUTDOWN, "Shutdown");
        dispatcher.setRunning(false);
    }

    public void putIPConnection(Connection connection) {
        synchronized (connections) {
            connections.put(connection.getNickname(), connection);
        }
    }

    public void updateGroup(Group group) {
        try {
            dbService.insertGroup(group);
        }
        catch(Exception e) {
            e.printStackTrace();
            System.out.println("GROUP UPDATE FAILED");
        }
    }

    /**
     * Runnable for reading commands in Console
     */
    private void handleCommands() {
        Scanner in=new Scanner(System.in);
        while(in.hasNextLine()) {
            String command=in.nextLine();
            String arr[] = command.split(" ");
            switch (arr[0].toLowerCase()) {
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
                        }
                        else {
                            connections.forEach((k, v) -> {
                                System.out.println(k + " (" + v.getNickname() + ")\t" + (v.isLoggedIn() ? v.getLoggedAccount() : "Not logged in"));
                            });
                        }
                    }
                    break;
                case "kick":
                    if (arr.length < 2) {
                        break;
                    }
                    String user = arr[1];
                    synchronized (connections) {
                        if (user.toLowerCase().equals("all")) {
                            connections.forEach((name, con) -> sendToUser(con, SHUTDOWN, "You have been kicked from the server"));
                        } else {
                            Connection userCon = getConnectionOfUser(user);
                            sendToUser(userCon, SHUTDOWN, "You have been kicked from the server");
                            removeConnectionAccount(userCon, userCon.getLoggedAccount());
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
    public Account loginUser(LoginUser loginUser) throws PacketException {
        //LoginUser loginUser = JsonHelper.convertJsonToObject(accountOrLoginAsJson);
        Account account=this.dbService.getAccountByLoginUser(loginUser);
        if(account!=null) {
            return account;
        }
        else {
            throw new PacketException("Invalid credentials");
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
        synchronized (connections) {
            if(account==null) {
                connections.remove(connection.getNickname());
            }
            else {
                connections.remove(account.getUserName());
            }
        }
        connection.setLoggedAccount(null);
    }

    public void registerUser(Account account) throws PacketException {
        this.dbService.insertAccount(account);
    }

    public void updateUser(Connection con, Account account) {
        synchronized (connections) {
            con.setLoggedAccount(account);
        }
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
    public boolean sendMessage(Message message, Connection receiverCon) {
        Profile receiverProfile=receiverCon.getLoggedAccount();

        /** Only check translation if user wants message to be translated */
        if(message.isTranslate()) {
            /** Identify message's original language*/
            String msgLanguage=translateService.identifyLanguage(message.getText());
            if(msgLanguage!=null) {
                String receiverLanguage = receiverProfile.getLanguages().get(0);
                /** Check if receiver doesnt speak message's language */
                // if(!receiverProfile.getLanguages().contains(msgLanguage))
                if (!receiverProfile.getLanguages().contains(msgLanguage)) {
                    /** Translate to receiver's language*/
                    String translated = translateService.translate(message.getText(),
                            msgLanguage, receiverLanguage);
                    //String translated=translateService.translate(message.getText(), msgLanguage, receiverLanguage);
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

    public boolean sendMessage(Message message) {
        String receiver=message.getReceiver();
        synchronized (connections) {
            if(!connections.containsKey(receiver)) {
                return false;
            }
            return sendMessage(message, connections.get(receiver));
        }
    }

    public void sendMessageToGroup(String groupName, Message message) {
        Connection receiverCon=null;
        ArrayList<Group.Participant> groupMembers=getUsersForGroup(groupName);
        for(Group.Participant p : groupMembers) {
            if (message.getSender().equals(p.getUserName())) {
                continue;
            }
            if (connections.containsKey(p.getUserName())) {
                receiverCon=connections.get(p.getUserName());
                sendMessage(message, receiverCon);
            } else {
                System.out.println("User not online, storing message in DB");
                storeMessage(message, p.getUserName());
            }
        }
    }

    public void broadcastMessages(Message message) {
        this.connections.values().stream()
                .filter(Connection::isLoggedIn)
                .forEach(client -> {
                    if (!message.getSender().equals(client.getLoggedAccount().getUserName())) {
                        sendMessage(message, client);
                            }
                        }
                );
    }

    public void broadcastPacket(String context, Object data) {
        synchronized (connections) {
            connections.values().stream()
                    .filter(Connection::isLoggedIn)
                    .forEach(c -> sendToUser(c, context, data));
        }
    }

    public List<Profile> getOnlineUsers() {
        synchronized (connections) {
            List<Profile> userList=connections.values().stream()
                    .filter(c -> c.isLoggedIn())
                    .map(c -> c.getLoggedAccount().getProfile()).collect(Collectors.toList());
            return userList;
        }
    }

    public void addContact(Account currentAcc, Profile newFriend, boolean block) throws PacketException {
        dbService.insertContactOfAccount(currentAcc, newFriend, block);
    }

    public void removeContact(Account loggedAccount, Profile contact) throws PacketException {
        dbService.removeContactOfAccount(loggedAccount, contact);
    }

    public boolean hasUserBlocked(Account account, String contact) {
        return dbService.hasBlocked(contact, account);
    }

    public ArrayList<Profile> getFriendList(Account currentAcc) {
        return dbService.getAllContactsOfAccount(currentAcc);
    }

    public Connection getConnectionOfUser(String userName) {
        synchronized (connections) {
            return connections.get(userName);
        }
    }

    public ArrayList<Group> getGroups(Account loggedAccount) {
        return dbService.getMyGroups(loggedAccount);
    }

    public ArrayList<Group.Participant> getUsersForGroup(String groupName) {
        return dbService.getParticipants(groupName);
    }

    public void storeMessage(Message message, String receiver) {
        dbService.insertMessage(message, receiver);
    }

    public List<Message> getStoredMessages(Account loggedAccount) {
        List<Message> messages = dbService.getStoredMessagesOfAccount(loggedAccount);
        dbService.removeStoredMessagesOfAcoount(loggedAccount);
        return messages;
    }

    public boolean isRunning() {
        return running.get();
    }

    public void setRunning(boolean running) {
        this.running.set(running);
    }

    public boolean sendInvitationEmail(String receiver, Profile sender) {
        return mailService.sendInviteMail(receiver, sender);
    }
}
