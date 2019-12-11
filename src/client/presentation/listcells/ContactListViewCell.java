package client.presentation.listcells;

import common.data.Profile;
import javafx.beans.property.DoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Circle;

import java.io.IOException;


public class ContactListViewCell extends ListCell<Profile> {
    @FXML
    private Label labelUsername;
    @FXML
    private Circle circleStatus;
    @FXML
    private BorderPane borderPane;

    private FXMLLoader fxmlLoader;

    private DoubleProperty listViewWidthProperty;

    public ContactListViewCell(DoubleProperty listViewWidthProperty) {
        this.listViewWidthProperty=listViewWidthProperty;
    }

    @Override
    protected void updateItem(Profile profile, boolean empty){
    super.updateItem(profile, empty);
        if(empty || profile == null){
            setGraphic(null);
            setText(null);
        }
        else {
            if(fxmlLoader == null){
                fxmlLoader = new FXMLLoader(getClass().getResource("ContactListCell.fxml"));
                fxmlLoader.setController(this);
                try {
                    fxmlLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            labelUsername.setText(profile.getUserName());
            if (profile.getStatus() == 1) {
                circleStatus.getStyleClass().clear();
                circleStatus.getStyleClass().add("online");
            }
            else if(profile.getStatus()==0) {
                circleStatus.getStyleClass().clear();
                circleStatus.getStyleClass().add("offline");
            }
            else if(profile.getStatus()==-1) {
                circleStatus.getStyleClass().clear();
                circleStatus.setVisible(false);
                circleStatus.setDisable(true);
            }
            borderPane.prefWidthProperty().bind(listViewWidthProperty);
            setGraphic(borderPane);
        }
    }

}
