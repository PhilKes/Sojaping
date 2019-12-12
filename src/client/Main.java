package client;

import common.Constants;
import javafx.application.Application;
import javafx.stage.Stage;

import static common.Constants.SERVER_HOST;
import static common.Constants.SERVER_PORT;

public class Main extends Application {


    @Override
    public void start(Stage primaryStage) {
        Client client=Client.getInstance(SERVER_HOST, SERVER_PORT);
        client.openWindow(Constants.Windows.LOGIN);
        new Thread(() -> client.run()).start();
    }

    public static void main(String[] args) {
        if(args.length>0) {
            SERVER_HOST=args[0];
            if(args.length>1) {
                SERVER_PORT=Integer.parseInt(args[1]);
            }
        }
        launch(args);
    }
}
