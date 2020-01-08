package server;

import common.Connection;
import common.Util;
import common.Util.PacketException;
import common.data.*;

import static common.Constants.Contexts.*;
import static common.JsonHelper.getPacketFromJson;

public class ServerHandler implements Runnable {

    private Server server;
    private Connection connection;

    private String receivedJson;

    public ServerHandler(Server server, Connection connection, String json) {
        this.receivedJson=json;
        this.server=server;
        this.connection=connection;
    }

    /**
     * Handles single Packet received from connection InputStream
     */
    public void run() {
        Packet receivedPacket=getPacketFromJson(receivedJson);
        if(receivedPacket==null) {
            server.sendToUser(connection, FAIL, new Exception("Invalid JSON received!"));
            return;
        }
        Util.logPacket(true, connection, receivedPacket);
        try {
            handlePacket(receivedPacket);
        }
        catch(PacketException e) {
            e.setFailedPacket(receivedPacket);
            server.sendToUser(connection, receivedPacket.getContext() + FAIL, e);
        }
    }

    private void handlePacket(Packet receivedPacket) throws PacketException {
        String context=receivedPacket.getContext();
        if(context.contains(FAIL) && !context.equals(FAIL)) {
            System.err.println(context.split(FAIL)[0]);
        }
        else {
            /** Determine context of receivedPacket*/
            switch(context) {
                case REGISTER:
                    /** Try to register account to DB, send Account from DB to user or send failed Exception */
                    Account account=receivedPacket.getData();
                    server.registerUser(account);
                    server.sendToUser(connection, REGISTER_SUCCESS, "Welcome to Sojaping " + account.getUserName() + " !");
                    break;
                /** Try to authenticate sent LoginUser*/
                case LOGIN:
                    System.out.println("Login Account");
                    LoginUser loginUser=receivedPacket.getData();
                    /** Check login credentials, send Account from DB to user or throw failed Exception */
                    Account loginAccount=server.loginUser(loginUser);
                    /** Check if user is already logged in somewhere else*/
                    if(server.getConnectionOfUser(loginAccount.getUserName())!=null) {
                        throw new PacketException("User is already logged in!");
                    }
                    server.setConnectionAccount(connection, loginAccount);
                    server.sendToUser(connection, LOGIN_SUCCESS, loginAccount);
                    //server.sendToUser(connection, FRIEND_LIST, server.getFriendList(connection.getLoggedAccount()));
                    //server.broadcastPacket(USERLIST, server.getOnlineUsers());
                    // server.sendToUser(connection, GROUPLIST, server.getGroups(connection.getLoggedAccount()));
                    break;
                case MESSAGE_SENT:
                    System.out.println("Send message");
                    Message message=receivedPacket.getData();
                    /** Check login credentials, send Account from DB to user or send failed Exception */
                    String receiver=message.getReceiver();
                    if(server.hasUserBlocked(connection.getLoggedAccount(), receiver)) {
                        System.out.println("Not sending message, Receiver has blocked the Sender!");
                        throw new PacketException(receiver + " has blocked you!");
                    }
                    if(receiver.equals(BROADCAST)) {
                        server.broadcastMessages(message);
                    }
                    else if(receiver.startsWith("#")) {
                        server.sendMessageToGroup(receiver, message);
                    }
                    else {
                        /** Private message */
                        if(!server.sendMessage(message)) {
                            System.out.println("User not online, storing message in DB");
                            server.storeMessage(message, message.getReceiver());
                        }
                    }
                    break;
                case MESSAGE_FETCH:
                    /** Get stored messages from DB of user */
                    for (Message msg : server.getStoredMessages(connection.getLoggedAccount())) {
                        server.sendToUser(connection, MESSAGE_RECEIVED, msg);
                    }
                    server.sendToUser(connection, MESSAGE_FETCH, "complete");
                    break;
                case USERLIST:
                    System.out.println("Send online User list");
                    /** Send updated online Userlist to clients*/
                    server.broadcastPacket(USERLIST, server.getOnlineUsers());
                    break;
                case FRIEND_LIST:
                    server.sendToUser(connection, FRIEND_LIST, server.getFriendList(connection.getLoggedAccount()));
                    break;
                case GROUPLIST:
                    server.sendToUser(connection, GROUPLIST, server.getGroups(connection.getLoggedAccount()));
                    break;
                case SHUTDOWN:
                    server.removeConnectionAccount(connection, receivedPacket.getData());
                    server.broadcastPacket(USERLIST, server.getOnlineUsers());
                    break;
                case LOGOFF:
                    server.removeConnectionAccount(connection, receivedPacket.getData());
                    server.putIPConnection(connection);
                    server.broadcastPacket(USERLIST, server.getOnlineUsers());
                    break;
                case ADD_FRIEND:
                case BLOCK:
                case UNBLOCK:
                    Profile contact=receivedPacket.getData();
                    System.out.println(receivedPacket.getContext() + " : " + contact.getUserName());
                    server.addContact(connection.getLoggedAccount(), contact, receivedPacket.getContext().equals(BLOCK));
                    server.sendToUser(connection, FRIEND_LIST, server.getFriendList(connection.getLoggedAccount()));
                    server.sendToUser(connection, USERLIST, server.getOnlineUsers());
                    break;
                case REMOVE_FRIEND:
                    server.removeContact(connection.getLoggedAccount(), receivedPacket.getData());
                    server.sendToUser(connection, FRIEND_LIST, server.getFriendList(connection.getLoggedAccount()));
                    server.sendToUser(connection, USERLIST, server.getOnlineUsers());
                    break;
                case PROFILE_UPDATE:
                    Account updatedAccount=receivedPacket.getData();
                    server.updateUser(connection, updatedAccount);
                    server.broadcastPacket(USERLIST, server.getOnlineUsers());
                    break;
                case DELETE_ACCOUNT:
                    Account accountForDeletion=receivedPacket.getData();
                    server.deleteUser(accountForDeletion);
                    break;
                case GROUP_UPDATE:
                    Group group = receivedPacket.getData();
                    server.updateGroup(group);
                    for (Profile member : group.getParticipants()) {
                        Connection memberCon = server.getConnectionOfUser(member.getUserName());
                        if (memberCon != null) {
                            server.sendToUser(memberCon, GROUPLIST, server.getGroups(memberCon.getLoggedAccount()));
                        }
                    }
                    break;
                case INVITATION_EMAIL:
                    if (server.sendInvitationEmail(receivedPacket.getData(), connection.getLoggedAccount().getProfile())) {
                        server.sendToUser(connection, INVITATION_EMAIL, true);
                    } else {
                        server.sendToUser(connection, INVITATION_EMAIL, false);
                    }
                    break;
                default:
                    System.err.println("Received unknown Packet context:\t" + receivedPacket.getContext());
                    throw new PacketException("Unknown Packet context('" + receivedPacket.getContext() + "') sent!");
            }
        }
    }
}
