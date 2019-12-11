package client;

import client.presentation.UIControllerWithInfo;
import common.Util;
import common.data.*;
import javafx.application.Platform;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

import static common.Constants.Contexts.*;
import static common.JsonHelper.getPacketFromJson;

class ClientHandler implements Runnable {

    private Client client;
    private boolean running;
    private Scanner scanner;

    public ClientHandler(Client client, InputStream inputStream) {
        this.client=client;
        this.running=true;
        scanner=new Scanner(inputStream, "UTF-8");
    }

    /**
     * Continuously wait for and handle new Packets
     */
    public void run() {
        while(running && scanner.hasNextLine()) {
            Packet receivedPacket=getPacketFromJson(scanner.nextLine());
            try {
                if(receivedPacket==null) {
                    throw new Exception("Invalid JSON received");
                }
                Util.logPacket(true, "Server", receivedPacket);
                handlePacket(receivedPacket);
            }
            catch(Exception e) {
                System.err.println(e.getMessage());
                client.sendToServer(FAIL, e);
            }
        }
        scanner.close();
        System.out.println("Shutting down ClientHandler");
    }

    private void handlePacket(Packet receivedPacket) throws Exception {
        String context=receivedPacket.getContext();
        if(context.contains(FAIL) && !context.equals(FAIL)) {
            System.err.println(context.split(FAIL)[0].toUpperCase() + " failed");
        }
        switch (context) {
            case LOGIN_SUCCESS:
                Account account = receivedPacket.getData();
                client.setAccount(account);
                System.out.println("Logged into " + account);
                client.closeCurrentWindowNoexit();
                client.openWindow("gui");
                break;
            case (LOGIN + FAIL):
            case (REGISTER + FAIL):
                Exception error1 = receivedPacket.getData();
                ((UIControllerWithInfo) client.getController()).showInfo(error1.getMessage(), UIControllerWithInfo.InfoType.ERROR);
                break;
            case REGISTER_SUCCESS:
                System.out.println("Successfully registered !");
                String message = receivedPacket.getData();
                ((UIControllerWithInfo) client.getController()).showInfo(message, UIControllerWithInfo.InfoType.SUCCESS);
                break;
            case MESSAGE_RECEIVED:
                Message msg = receivedPacket.getData();
                Platform.runLater(() -> client.getGUIController().displayNewMessage(msg));
                break;
            case USERLIST:
                ArrayList<Profile> userList = receivedPacket.getData();
                System.out.println("Profiles received:");
                userList.forEach(u -> System.out.println(u));
                Platform.runLater(() -> client.getGUIController().displayOnlineProfiles(userList));
                break;
            case SHUTDOWN:
                String text = receivedPacket.getData();
                System.out.println("SERVER is shutting down: " + text);
                running = false;
                break;
            case GROUPLIST:
                ArrayList<Group> groupList = receivedPacket.getData();
                System.out.println("Groups received:");
                groupList.forEach(g -> System.out.println(g));
                Platform.runLater(() -> client.getGUIController().displayGroupChats(groupList));
                Platform.runLater(() -> client.getGUIController().fillContextMenuAddToGroup(groupList));
                break;
            case FRIEND_LIST:
                ArrayList<Profile> contacts=receivedPacket.getData();
                Platform.runLater(() -> client.getGUIController().displayContactsProfiles(contacts));
                break;
            default:
                System.err.println("Received unknown Packet context:\t" + receivedPacket.getContext());
                //throw new Exception("Unknown Packet context('" + receivedPacket.getContext() + "') sent!");
                break;
        }
    }

    /**
     * Wait for next packet, return true if CONNECT_SUCCESS received, else return false
     */
    public boolean waitForConnectSuccess() {
        Packet response=getPacketFromJson(scanner.nextLine());
        if(response==null) {
            return false;
        }
        if(response.getContext().equals(CONNECT_SUCCESS)) {
            System.out.println("CONNECT_SUCCESS Packet received");
            return true;
        }
        System.err.println("CONNECT_SUCCESS Packet not received!");
        return false;
    }
}