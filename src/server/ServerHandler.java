package server;

import common.Util;
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
        catch(Exception e) {
            server.sendToUser(connection, receivedPacket.getContext() + FAIL, e);
        }
    }

    //TODO Distinguish Exceptions -> do not throw general Exception()
    private void handlePacket(Packet receivedPacket) throws Exception {
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
                /** Try to authenticate sent LToginUser*/
                case LOGIN:
                    System.out.println("Login Account");
                    LoginUser loginUser=receivedPacket.getData();
                    /** Check login credentials, send Account from DB to user or throw failed Exception */
                    Account loginAccount=server.loginUser(loginUser);
                    /** Check if user is already logged in somewhere else*/
                    if(server.getConnectionOfUser(loginAccount.getUserName())!=null) {
                        throw new Exception("User is already logged in!");
                    }
                    //TODO Check Account profilepicture
                    server.setConnectionAccount(connection, loginAccount);
                    server.sendToUser(connection, LOGIN_SUCCESS, loginAccount);
                    server.sendToUser(connection, FRIEND_LIST, server.getFriendList(connection.getLoggedAccount()));
                    server.broadcastPacket(USERLIST, server.getOnlineUsers());
                    server.sendToUser(connection, GROUPLIST, server.getGroups(connection.getLoggedAccount()));
                    break;
                case MESSAGE_SENT:
                    System.out.println("Send message");
                    Message message=receivedPacket.getData();
                    /** Check login credentials, send Account from DB to user or send failed Exception */
                    String receiver=message.getReceiver();
                    if(receiver.equals(BROADCAST)) {
                        server.broadcastMessages(message);
                    }
                    else if(receiver.startsWith("#")) {
                        server.sendMessageToGroup(receiver, message);
                    }
                    else {
                        /** Private message */
                        if(!server.sendMessage(message)) {
                            throw new Exception("Receiver not found!");
                        }
                    }
                    break;
                case USERLIST:
                    System.out.println("Send online User list");
                    /** Send updated online Userlist to clients*/
                    server.broadcastPacket(USERLIST, server.getOnlineUsers());
                    break;
                case GROUPLIST:
                    server.sendToUser(connection, GROUPLIST, server.getGroups(connection.getLoggedAccount()));
                    break;
                case GROUP_ADD:

                    break;
                case SHUTDOWN:
                    server.removeConnectionAccount(connection, (Account) receivedPacket.getData());
                    server.broadcastPacket(USERLIST, server.getOnlineUsers());
                    break;
                case LOGOFF:
                    server.removeConnectionAccount(connection, (Account) receivedPacket.getData());
                    server.putIPConnection(connection);
                    server.broadcastPacket(USERLIST, server.getOnlineUsers());
                    break;
                case ADD_FRIEND:
                    server.addFriend(connection.getLoggedAccount(), receivedPacket.getData());
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
                        server.sendToUser(memberCon, GROUPLIST, server.getGroups(memberCon.getLoggedAccount()));
                    }
                    break;
                case INVITATION_EMAIL:
                    server.sendInvitationEmail(receivedPacket.getData(),connection.getLoggedAccount().getProfile());
                    break;
                default:
                    System.err.println("Received unknown Packet context:\t" + receivedPacket.getContext());
                    throw new Exception("Unknown Packet context('" + receivedPacket.getContext() + "') sent!");
            }
        }
    }
}
