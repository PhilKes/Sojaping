package client;

import client.presentation.FXUtil;
import client.presentation.TitleBarController;
import client.presentation.UIController;
import client.presentation.UIControllerWithInfo;
import client.presentation.windows.GUIController;
import client.presentation.windows.LoginController;
import common.Connection;
import common.Constants;
import common.Util;
import common.data.Account;
import common.data.LoginUser;
import common.data.Message;
import common.data.Packet;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;

import static common.Constants.Contexts.*;

public class Client {

    private static Client instance;
    private String host;
    private Connection connection;
    private int port;
    private PrintWriter output;
    private AtomicBoolean running;
    private MessageStore messageStore;

    private int loadingCount;
    private AtomicBoolean loading;
    private Stage loadingStage;
    private String theme = "Default";

    /**
     * Storing all Controllers of open FXMLs, top of stack = current active Controller
     */
    private Stack<UIController> controllerStack;

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
        this.loading=new AtomicBoolean(false);
        messageStore=new MessageStore();
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
    public void openWindow(String window, boolean show) {
        Platform.runLater(() -> {
            try {
                FXMLLoader windowLoader=new FXMLLoader(getClass().getResource("presentation/windows/" + window + ".fxml"));
                Pane root1=windowLoader.load();
                UIController controller=(UIController) windowLoader.getController();
                Stage stage=new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.initStyle(StageStyle.UNDECORATED);
                stage.setTitle(window.substring(0, 1).toUpperCase() + window.substring(1));
                if(controller!=null) {
                    controller.setClient(this);
                    controller.setStage(stage);
                }

                /** Wrap Window in custom VBox with TitleBar*/
                VBox wrapBox=new VBox();
                wrapBox.setId("window-wrapper");
                wrapBox.getStylesheets().add(getClass().getResource("presentation/resources/main.css").toExternalForm());
                FXMLLoader titleBarLoader=new FXMLLoader(getClass().getResource("presentation/TitleBar.fxml"));
                TitleBarController titleBarController=new TitleBarController();
                titleBarController.setClient(this);
                titleBarController.setStage(stage);
                titleBarLoader.setController(titleBarController);

                HBox titleBar=titleBarLoader.load();
                titleBar.prefWidthProperty().bind(root1.prefWidthProperty());
                wrapBox.getChildren().addAll(titleBar, root1);

                stage.setScene(new Scene(wrapBox));
                stage.setOnCloseRequest(ev -> closeCurrentWindow());
                stage.setResizable(false);
                stage.getIcons().add(FXUtil.getDefaultIcon());
                if(connection!=null && connection.isLoggedIn()) {
                    stage.setTitle(stage.getTitle() + " " + connection.getLoggedAccount().getUserName());
                }
                stage.setOnShown(ev -> {
                    /** Center new window in current window*/
                    if(!controllerStack.isEmpty()) {
                        Stage currStage=peekController().getStage();
                        stage.setX(currStage.getX() + currStage.getWidth() / 2 - stage.getWidth() / 2);
                        stage.setY(currStage.getY() + currStage.getHeight() / 2 - stage.getHeight() / 2);
                    }
                });

                if(show) {
                    stage.show();
                }


                if(controller!=null) {
                    pushController(controller);
                    changeTheme(theme);
                }
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void openWindow(String window) {
        openWindow(window, true);
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
            sendToServer(SHUTDOWN, connection.getLoggedAccount());
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

    public LoginController getLoginController() {
        for(UIController controller : controllerStack) {
            if(controller instanceof LoginController) {
                return (LoginController) controller;
            }
        }
        return null;
    }

    public UIController peekController() {
        if(!controllerStack.isEmpty()) {
            return controllerStack.peek();
        }
        return null;
    }

    public void pushController(UIController controller) {
        controllerStack.push(controller);
    }

    public void setAccount(Account account) {
        this.connection.setLoggedAccount(account);

    }

    public void setMessageStoreUser(String userName) {
        messageStore.setUserName(userName);
    }

    public Account getAccount() {
        return connection.getLoggedAccount();
    }

    public boolean isConnected() {
        return output!=null;
    }

    public void storeMessageLocal(Message message) {
        System.out.println("Stored: " + message);
        messageStore.addMessage(message);
    }

    /**
     * Store Messages in local .xml
     */
    private void storeMessageStore() {
        System.out.println("Storing local messages");
        new MessageParser().storeMessageStore(messageStore);
        messageStore.getMessages().clear();
    }

    /**
     * Load Messages from local .xml
     */
    public void fetchAndShowLocalMessageStore() {
        System.out.println("Fetching local messages");
        List<Message> messages=new MessageParser().getMessageStore(getAccount().getUserName()).getMessages();
        for(Message msg : messages) {
            Platform.runLater(() -> getGUIController().displayNewMessage(msg, false));
        }
        if(isLoading()) {
            incLoadingCount();
        }
    }

    public void resetLocalMessageStore() {
        if(new MessageParser().resetMessageStore(getAccount().getUserName())) {
            ((UIControllerWithInfo) peekController()).showInfo("MessageStore has been reset", UIControllerWithInfo.InfoType.SUCCESS);
        }
    }

    public void logout() {
        sendToServer(LOGOFF, getAccount());
        storeMessageStore();
        setAccount(null);
        closeCurrentWindowNoExit();
        openWindow(Constants.Windows.LOGIN);
    }

    public void login(LoginUser loginUser) {
        sendToServer(LOGIN, loginUser);
        getLoginController().showLoading(true);
        setLoading(true);
    }

    /**
     * Start next loading step
     */
    public void incLoadingCount() {
        loadingCount++;
        System.out.println("Loading Step: " + loadingCount);
        LoginController loginController=getLoginController();
        switch(loadingCount) {
            case 1:
                loginController.setTxtLoading("Loading local MessageStore...");
                fetchAndShowLocalMessageStore();
                break;
            case 2:
                loginController.setTxtLoading("Fetching Messages...");
                sendToServer(MESSAGE_FETCH, null);
                break;
            case 3:
                loginController.setTxtLoading("Fetching FriendList...");
                sendToServer(FRIEND_LIST, null);
                break;
            case 4:
                loginController.setTxtLoading("Fetching Online Users...");
                sendToServer(USERLIST, null);
                break;
            case 5:
                loginController.setTxtLoading("Fetching Groups...");
                sendToServer(GROUPLIST, null);
                break;
            /** loading finished*/
            case Constants.CLIENT_LOADING_STEPS:
                Platform.runLater(() -> {
                    /** Close loading dialog and Login Window */
                    getLoginController().showLoading(false);
                    getLoginController().close();
                    controllerStack.remove(getLoginController());
                    getGUIController().getStage().show();
                });
                setLoading(false);
                break;
        }
    }

    public boolean isLoading() {
        return loading.get();
    }

    public void setLoading(boolean loading) {
        System.out.println("Loading " + (loading ? "started" : "finished"));
        this.loading.set(loading);
        if(!loading) {
            loadingCount=0;
        }
    }

    public void changeTheme(String newTheme){
        String def = getClass().getResource("./presentation/resources/main.css").toExternalForm();
        String ocean = getClass().getResource("./presentation/resources/BlueTheme.css").toExternalForm();
        theme = newTheme;
        if(newTheme.equals("Default")){
            for(UIController controller : controllerStack){
                Parent parent = controller.getStage().getScene().getRoot();
                parent.getStylesheets().clear();
                parent.getStylesheets().add(def);
            }
        }else if(newTheme.equals("Ocean")){
            for(UIController controller : controllerStack){
                Parent parent = controller.getStage().getScene().getRoot();
                parent.getStylesheets().clear();
                parent.getStylesheets().add(ocean);
            }
        }
    }

}

