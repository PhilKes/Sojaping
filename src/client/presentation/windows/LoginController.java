package client.presentation.windows;

import client.presentation.UIControllerWithInfo;
import common.data.LoginUser;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import static common.Constants.Contexts.LOGIN;


public class LoginController extends UIControllerWithInfo {

    @FXML
    private Button btnRegister, btnLogin;
    @FXML
    private TextField txtUsername;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private Label labelError;

    @FXML
    private void initialize() {
        btnRegister.setOnMouseClicked(ev -> onRegisterClicked());
        btnLogin.setOnMouseClicked(ev -> onLoginClicked());
        Platform.runLater(() -> {
            txtUsername.requestFocus();
        });
    }

    private void onLoginClicked() {
        //TODO Show popup errors
        if(txtUsername.getText().isEmpty() || txtPassword.getText().isEmpty()) {
            System.err.println("Invalid Login Input");
            //client.openPopup("Invalid Login Input",btnLogin);
            showInfo("Invalid Login Credentials!", InfoType.ERROR);
            return;
        }
        if (!client.isConnected()) {
            //client.openPopup("Not connected to the server!",btnLogin);
            showInfo("Not connected to the server!", InfoType.ERROR);
            return;
        }
        LoginUser loginUser=new LoginUser(txtUsername.getText(), txtPassword.getText());
        client.sendToServer(LOGIN, loginUser);
    }

    private void onRegisterClicked() {
        if (client.isConnected()) {
            client.openWindow("register");
        } else {
            showInfo("Not connected to the server!", InfoType.ERROR);
        }
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
