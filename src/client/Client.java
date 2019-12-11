package client;

import client.presentation.TitleBarController;
import client.presentation.UIController;
import client.presentation.UIControllerWithInfo;
import client.presentation.windows.GUIController;
import common.Util;
import common.data.Account;
import common.data.Packet;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import server.Connection;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;

import static common.Constants.Contexts.CONNECT;
import static common.Constants.Contexts.SHUTDOWN;

public class Client {

    private static Client instance;
    private String host;
    private Connection connection;
    private int port;
    private PrintWriter output;
    private AtomicBoolean running;

    /**
     * Storing all Controllers of open FXMLs, top of stack = current active Controller
     */
    private Stack<UIController> controllerStack;
    private Account account;

    /**
     * Client as Singleton
     */
    public static Client getInstance(String host, int port) {
        if(instance==null) {
            instance=new Client(host, port);
        }
        return instance;
    }

    private Client(String host, int port) {
        this.host=host;
        this.port=port;
        this.controllerStack=new Stack<>();
        running=new AtomicBoolean(true);
    }

    public void run() {
        /** Loop to try to connect to server*/
        do {
            try {
                connection=new Connection(host, port, InetAddress.getLocalHost().getHostAddress());
                break;
            }
            catch(IOException e) {
                //e.printStackTrace();
                System.err.println("Connection failed\n retrying in 1.5 Seconds...");
                try {
                    Thread.sleep(1500);
                }
                catch(InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            if(!running.get()) {
                System.out.println("Cancel connection attempt");
                return;
            }
        } while(true);
        System.out.println("Socket connection successfully established to server(" + host + ")!");
        output=new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                connection.getOutStream(), StandardCharsets.UTF_8)), true);
        ClientHandler handler=new ClientHandler(this, connection.getInputStream());
        sendToServer(CONNECT, connection.getNickname());
        if(handler.waitForConnectSuccess())
        /** Start Thread to handle packets from the server if CONNECT_SUCCESS received*/ {
            Platform.runLater(() -> ((UIControllerWithInfo) peekController())
                    .showInfo("Connected to server", UIControllerWithInfo.InfoType.INFO));
            new Thread(handler).start();
        }
    }

    /**
     * Send object as Packet with context to Server
     */
    public void sendToServer(String context, Object object) {
        Packet packet=new Packet(context, object);
        output.println(packet.getJson());
        output.flush();
        Util.logPacket(false, "Server", packet);
    }

    /**
     * Open new GUI window, add UIController to Stack
     */
    public void openWindow(String window) {
        Platform.runLater(() -> {
            try {
                FXMLLoader windowLoader=new FXMLLoader(getClass().getResource("presentation/windows/" + window + ".fxml"));
                Pane root1 = windowLoader.load();
                UIController controller = (UIController) windowLoader.getController();
                controller.setClient(this);

                Stage stage=new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.initStyle(StageStyle.UNDECORATED);
                stage.setTitle(window.substring(0, 1).toUpperCase() + window.substring(1));
                controller.setStage(stage);

                /** Wrap Window in custom VBox with TitleBar*/
                VBox wrapBox = new VBox();
                wrapBox.setId("window-wrapper");
                wrapBox.getStylesheets().add(getClass().getResource("presentation/resources/main.css").toExternalForm());
                FXMLLoader titleBarLoader = new FXMLLoader(getClass().getResource("presentation/TitleBar.fxml"));
                TitleBarController titleBarController = new TitleBarController();
                titleBarController.setClient(this);
                titleBarController.setStage(stage);
                titleBarLoader.setController(titleBarController);

                HBox titleBar = titleBarLoader.load();
                titleBar.prefWidthProperty().bind(root1.prefWidthProperty());
                wrapBox.getChildren().addAll(titleBar, root1);

                stage.setScene(new Scene(wrapBox));
                stage.setOnCloseRequest(ev -> closeCurrentWindow());
                stage.setResizable(false);
                stage.getIcons().add(Util.getDefaultIcon());
                if (account != null) {
                    stage.setTitle(stage.getTitle() + " " + account.getUserName());
                }
                stage.show();
                /** Center new window in current window*/
                if(!controllerStack.isEmpty()) {
                    Stage currStage=peekController().getStage();
                    stage.setX(currStage.getX() + currStage.getWidth() / 2 - stage.getWidth() / 2);
                    stage.setY(currStage.getY() + currStage.getHeight() / 2 - stage.getHeight() / 2);
                }
                pushController(controller);
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void closeCurrentWindow() {
        closeCurrentWindowNoExit();
        if(controllerStack.isEmpty()) {
            this.stop();
        }
    }

    public void closeCurrentWindowNoExit() {
        controllerStack.pop().close();
    }

    /**
     * Called if last Window is closed
     */
    public void stop() {
        System.out.println("Closing connection...");
        running.set(false);
        if(output!=null) {
            sendToServer(SHUTDOWN, account);
            try {
                Thread.sleep(500);
                output.close();
                connection.getSocket().close();
            }
            catch(IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public GUIController getGUIController() {
        for(UIController controller : controllerStack) {
            if(controller instanceof GUIController) {
                return (GUIController) controller;
            }
        }
        return null;
    }

    public UIController peekController() {
        return controllerStack.peek();
    }

    public void pushController(UIController controller) {
        controllerStack.push(controller);
    }

    public void setAccount(Account account) {
        this.account=account;
    }

    public Account getAccount() {
        return account;
    }

    public boolean isConnected() {
        return output != null;
    }
}

