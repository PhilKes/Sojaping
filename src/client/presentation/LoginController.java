package client.presentation;

import common.data.LoginUser;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import static common.Constants.Contexts.LOGIN;


public class LoginController extends UIController {

    @FXML
    private Button btnRegister, btnLogin;
    @FXML
    private TextField txtUsername;
    @FXML
    private PasswordField txtPassword;


    @FXML
    private void initialize() {
        btnRegister.setOnMouseClicked(ev -> onRegisterClicked());
        btnLogin.setOnMouseClicked(ev -> onLoginClicked());
    }

    private void onLoginClicked() {
        if(txtUsername.getText().isEmpty() || txtPassword.getText().isEmpty()) {
            System.err.println("Invalid Login Input");
            return;
        }
        LoginUser loginUser=new LoginUser(txtUsername.getText(), txtPassword.getText());
        client.sendToServer(LOGIN, loginUser);
    }

    private void onRegisterClicked() {
        client.openWindow("register");
    }

    @FXML
    public void onEnter(ActionEvent ae){
        this.onLoginClicked();
    }

    @Override
    public void close() {
        Platform.runLater(() -> ((Stage) btnRegister.getScene().getWindow()).close());
    }


}
