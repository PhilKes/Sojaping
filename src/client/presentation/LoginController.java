package client.presentation;

import client.Client;
import client.LoginUser;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import server.Server;

import java.io.IOException;

import static common.Constants.Contexts.LOGIN;


public class LoginController {

    @FXML
    private Button btnRegister,btnLogin;
    @FXML
    private TextField txtUsername,txtPassword;

    private Client client;

    @FXML
    private void initialize(){
        btnRegister.setOnMouseClicked(ev->onRegisterClicked());
        btnLogin.setOnMouseClicked(ev->onLoginClicked());
    }

    private void onLoginClicked() {
        if(txtUsername.getText().isEmpty() || txtPassword.getText().isEmpty()){
            System.err.println("Invalid Login Input");
            return;
        }
        LoginUser loginUser= new LoginUser(txtUsername.getText(),txtPassword.getText());
        client.sendToServer(LOGIN,loginUser);


        //TODO
    }
    public void close(){
        Platform.runLater(()-> ((Stage)btnRegister.getScene().getWindow()).close());
    }
    private void onRegisterClicked() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("register.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            RegisterController registerCtrl=(RegisterController)fxmlLoader.getController();
            registerCtrl.setClient(client);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.DECORATED);
            stage.setTitle("Register");
            stage.setScene(new Scene(root1));
            stage.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }
}
