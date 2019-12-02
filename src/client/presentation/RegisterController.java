package client.presentation;

import common.Util;
import common.data.Account;
import common.data.AccountBuilder;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.apache.commons.lang3.mutable.MutableInt;
import server.TranslationService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static common.Constants.Contexts.REGISTER;


public class RegisterController extends UIController {

    @FXML
    private TextField txtUsername;
    @FXML
    private PasswordField txtPassword, txtRepeat;
    @FXML
    private MenuButton menuLanguages;
    private List<String> selectedLanguages;
    @FXML
    private Button btnRegister;

    private MutableInt languageCounter;

    @FXML
    private void initialize() {
        languageCounter = new MutableInt(0);
        selectedLanguages=new ArrayList<>();
        Util.fillLanguageMenu(menuLanguages, selectedLanguages, languageCounter);
        btnRegister.setOnMouseClicked(ev -> onRegisterClicked());
    }

    private void onRegisterClicked() {
        /** Check if all Inputs are valid */
        if(txtUsername.getText().isEmpty() || txtPassword.getText().isEmpty()
                || !txtPassword.getText().equals(txtRepeat.getText())
                || selectedLanguages.isEmpty()) {
            System.err.println("Invalid Input");
            return;
        }

        Account acc=new AccountBuilder()
                .setUserName(txtUsername.getText())
                .setPassword(txtPassword.getText())
                .setLanguages(selectedLanguages.stream()
                        .map(TranslationService.languages::get)
                        .collect(Collectors.toList()))
                .createAccount();
        client.sendToServer(REGISTER, acc);
    }

    @Override
    public void close() {
        Platform.runLater(() -> ((Stage) btnRegister.getScene().getWindow()).close());
    }
}
