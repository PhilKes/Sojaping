package client.presentation;

import common.data.Profile;
import javafx.beans.property.DoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;

import java.io.IOException;


public class ContactListViewCell extends ListCell<Profile> {
    @FXML
    private Label labelUsername;
    @FXML
    private HBox hBox;

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
            hBox.prefWidthProperty().bind(listViewWidthProperty);
            setGraphic(hBox);
        }



    }

}
