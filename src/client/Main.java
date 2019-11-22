package client;

import javafx.application.Application;
import javafx.stage.Stage;
import server.Server;

public class Main extends Application {


    @Override
    public void start(Stage primaryStage) {
        Client client=Client.getInstance(Server.SERVER_HOST, Server.SERVER_PORT);
        client.openWindow("login");
        new Thread(() -> client.run()).start();
    }

    public static void main(String[] args) {
        if(args.length>0) {
            Server.SERVER_HOST=args[0];
            if(args.length>1) {
                Server.SERVER_PORT=Integer.parseInt(args[1]);
            }
        }
        launch(args);
    }
}
