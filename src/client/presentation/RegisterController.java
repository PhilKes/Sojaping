package client.presentation;

import common.data.Account;
import common.data.AccountBuilder;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import static common.Constants.Contexts.REGISTER;


public class RegisterController extends UIController {

    @FXML
    private TextField txtUsername, txtPassword, txtRepeat;
    @FXML
    private ChoiceBox<String> boxLanguages;
    @FXML
    private Button btnRegister;

    @FXML
    private void initialize() {
        btnRegister.setOnMouseClicked(ev -> onRegisterClicked());
    }

    private void onRegisterClicked() {
        if(txtUsername.getText().isEmpty() || txtPassword.getText().isEmpty()
                || !txtPassword.getText().equals(txtRepeat.getText())) {
            System.err.println("Invalid Input");
            return;
        }
        //TODO (Next Sprint) Get selected Languages from boxLanguages
        Account acc=new AccountBuilder()
                .setUserName(txtUsername.getText())
                .setPassword(txtPassword.getText())
                .createAccount();
        client.sendToServer(REGISTER, acc);
    }

    @Override
    public void close() {
        Platform.runLater(() -> ((Stage) btnRegister.getScene().getWindow()).close());
    }
}
