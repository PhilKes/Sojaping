package client.presentation;

import common.data.Account;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

public class RegisterController {
    @FXML
    private TextField txtUsername,txtPassword,txtRepeat;
    @FXML
    private ChoiceBox<String> boxLanguages;
    @FXML
    private Button btnRegister;

    @FXML
    private void initialize(){
        btnRegister.setOnMouseClicked(ev->{
            if(txtUsername.getText().isEmpty() || txtPassword.getText().isEmpty()
            || txtPassword.getText() != txtRepeat.getText() ){
                System.err.println("Invalid Input");
                return;
            }
           // Account acc = new Account();
            System.out.printf("Register:\n" +
                    "");
            //((Stage)btnRegister.getScene().getWindow()).close();
        });
    }

}
