package client.presentation.windows;

import client.presentation.UIControllerWithInfo;
import common.Constants;
import common.data.LoginUser;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

public class LoginController extends UIControllerWithInfo {

    @FXML
    private Button btnRegister, btnLogin;
    @FXML
    private TextField txtUsername;
    @FXML
    private PasswordField txtPassword;

    @FXML
    private Pane paneLoading;
    @FXML
    private ImageView imgLoading;
    @FXML
    private Label txtLoading;

    @FXML
    private void initialize() {
        btnRegister.setOnMouseClicked(ev -> onRegisterClicked());
        btnLogin.setOnMouseClicked(ev -> onLoginClicked());
        Platform.runLater(() -> txtUsername.requestFocus());
        showLoading(false);
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
        client.login(loginUser);
    }

    public void setTxtLoading(String txt) {
        Platform.runLater(() -> txtLoading.setText(txt));
    }

    private void onRegisterClicked() {
        if(client.isConnected()) {
            client.openWindow(Constants.Windows.REGISTER);
        }
        else {
            showInfo("Not connected to the server!", InfoType.ERROR);
        }
    }

    public void showLoading(boolean show) {
        paneLoading.setVisible(show);
        paneLoading.setDisable(!show);
        if(show) {
            RotateTransition rt=new RotateTransition(Duration.millis(1500), imgLoading);
            rt.setByAngle(360);
            rt.setCycleCount(Animation.INDEFINITE);
            rt.setInterpolator(Interpolator.LINEAR);
            rt.play();
        }
    }

    @FXML
    public void onEnter(ActionEvent ae) {
        this.onLoginClicked();
    }
}
