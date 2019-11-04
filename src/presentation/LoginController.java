package presentation;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class LoginController {

    @FXML
    private Button btnRegister;

    @FXML
    private void initialize(){
        btnRegister.setOnMouseClicked(ev->{
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../presentation/register.fxml"));
                Parent root1 = (Parent) fxmlLoader.load();
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.initStyle(StageStyle.DECORATED);
                stage.setTitle("Register");
                stage.setScene(new Scene(root1));
                stage.show();
            }catch (Exception e){
                e.printStackTrace();
            }
        });
    }
}
