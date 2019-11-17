package client;

import client.presentation.LoginController;
import client.presentation.RegisterController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import server.Server;

import java.net.Socket;

public class Main extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception{
        /*FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("presentation/login.fxml"));
        Parent root = fxmlLoader.load();
        primaryStage.setTitle("Sojaping Login");
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);*/

        Client client=Client.getInstance(Server.SERVER_HOST, Server.SERVER_PORT);
        client.openWindow("login");
       /* LoginController loginController=(LoginController)fxmlLoader.getController();
        loginController.setClient(client);
        client.setController(loginController);*/
        /*primaryStage.show();*/
        new Thread(()->client.run()).start();
    }



    public static void main(String[] args) {
        launch(args);
    }
}
