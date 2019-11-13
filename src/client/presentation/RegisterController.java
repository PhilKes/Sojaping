package client.presentation;

import client.Client;
import common.data.Account;
import common.data.AccountBuilder;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

import static common.Constants.Contexts.*;


public class RegisterController {

    @FXML
    private TextField txtUsername,txtPassword,txtRepeat;
    @FXML
    private ChoiceBox<String> boxLanguages;
    @FXML
    private Button btnRegister;

    private Client client;

    @FXML
    private void initialize(){
        btnRegister.setOnMouseClicked(ev->onRegisterClicked());
    }

    private void onRegisterClicked() {
        if(txtUsername.getText().isEmpty() || txtPassword.getText().isEmpty()
                || !txtPassword.getText().equals(txtRepeat.getText()) ){
            System.err.println("Invalid Input");
            return;
        }
        //TODO Get selected Languages from boxLanguages
        Account acc = new AccountBuilder().setUserName(txtUsername.getText()).setPassword(txtPassword.getText()).createAccount();
        //System.out.println("Register:\n"+acc);
        client.sendToServer(REGISTER,acc);
        //((Stage)btnRegister.getScene().getWindow()).close();
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

}
