package client.presentation.windows;

import client.presentation.FXUtil;
import client.presentation.UIControllerWithInfo;
import common.Constants;
import common.data.Account;
import common.data.AccountBuilder;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static common.Constants.Contexts.REGISTER;

public class RegisterController extends UIControllerWithInfo {

    @FXML
    private TextField txtUsername;
    @FXML
    private PasswordField txtPassword, txtRepeat;
    @FXML
    private MenuButton menuLanguages;
    private List<String> selectedLanguages;
    @FXML
    private Button btnRegister;

    /**
     * Array with length 1, wrapper needed, so filllanguageMenu does not call by value
     */
    private int[] languageCounter;

    @FXML
    private void initialize() {
        languageCounter=new int[]{0};
        selectedLanguages=new ArrayList<>();
        FXUtil.fillLanguageMenu(menuLanguages, selectedLanguages, languageCounter);
        ((CheckMenuItem) menuLanguages.getItems().get(1)).setSelected(true);
        btnRegister.setOnMouseClicked(ev -> onRegisterClicked());
    }

    private void onRegisterClicked() {
        /** Check if all Inputs are valid */
        if(txtUsername.getText().isEmpty() || txtPassword.getText().isEmpty() || selectedLanguages.isEmpty()) {
            showInfo("Please fill all fields!", InfoType.ERROR);
            return;
        }
        if(!txtPassword.getText().equals(txtRepeat.getText())) {
            showInfo("Passwords do not match!", InfoType.ERROR);
            return;
        }

        Account acc=new AccountBuilder()
                .setUserName(txtUsername.getText())
                .setPassword(txtPassword.getText())
                .setLanguages(selectedLanguages.stream()
                        .map(Constants.Translation.languages::get)
                        .collect(Collectors.toList()))
                .createAccount();
        client.sendToServer(REGISTER, acc);
    }
}
