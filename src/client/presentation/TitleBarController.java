package client.presentation;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Custom TitleBar with minimize + close Buttons
 */
public class TitleBarController extends UIController {
    @FXML
    private HBox titleBar;
    @FXML
    private Button btnClose, btnMinimize;
    @FXML
    private Label labelTitle;

    private Stage stage;
    private double xOffset, yOffset=0;

    @FXML
    private void initialize() {
        btnClose.setOnMouseClicked(ev -> close());
        btnMinimize.setOnMouseClicked(ev -> minimize());
        titleBar.setOnMousePressed(event -> {
            xOffset=stage.getX() - event.getScreenX();
            yOffset=stage.getY() - event.getScreenY();
        });
        titleBar.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() + xOffset);
            stage.setY(event.getScreenY() + yOffset);
        });
    }

    private void minimize() {
        stage.setIconified(true);
    }

    @Override
    public void close() {
        if(client!=null) {
            client.closeCurrentWindow();
        }
        else {
            Platform.runLater(() -> stage.close());
        }
    }

    public void setStage(Stage stage) {
        this.stage=stage;
        Platform.runLater(() -> labelTitle.setText(stage.getTitle()));
    }
}
