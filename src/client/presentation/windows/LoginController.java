package client.presentation.windows;

import client.presentation.UIControllerWithInfo;
import common.Constants;
import common.data.LoginUser;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import static common.Constants.Contexts.LOGIN;

public class LoginController extends UIControllerWithInfo {

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
        Platform.runLater(() -> txtUsername.requestFocus());
    }

    private void onLoginClicked() {
        if(txtUsername.getText().isEmpty() || txtPassword.getText().isEmpty()) {
            showInfo("Invalid Login Credentials!", InfoType.ERROR);
            return;
        }
        if(!client.isConnected()) {
            showInfo("Not connected to the server!", InfoType.ERROR);
            return;
        }
        LoginUser loginUser=new LoginUser(txtUsername.getText(), txtPassword.getText());
        client.sendToServer(LOGIN, loginUser);
    }

    private void onRegisterClicked() {
        if(client.isConnected()) {
            client.openWindow(Constants.Windows.REGISTER);
        }
        else {
            showInfo("Not connected to the server!", InfoType.ERROR);
        }
    }

    @FXML
    public void onEnter(ActionEvent ae) {
        this.onLoginClicked();
    }
}
