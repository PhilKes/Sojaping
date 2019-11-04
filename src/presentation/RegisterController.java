package presentation;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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

            //((Stage)btnRegister.getScene().getWindow()).close();
        });
    }

}
