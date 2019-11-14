package client.presentation;

import client.Client;
import client.LoginUser;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import static common.Constants.Contexts.LOGIN;


public class LoginController extends UIController {

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
    @Override
    public void close(){
        Platform.runLater(()-> ((Stage)btnRegister.getScene().getWindow()).close());
    }

    private void onRegisterClicked() {
        client.openWindow("register");
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }
}
