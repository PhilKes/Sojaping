package client.presentation.listcells;

import common.data.Group;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class GroupChatListViewCell extends ListCell<Group> {
    @FXML
    private Label labelGroupname;
    @FXML
    private HBox hBox;

    private FXMLLoader fxmlLoader;

    @Override
    protected void updateItem(Group group, boolean empty){
        super.updateItem(group, empty);
        if(empty || group == null){
            setGraphic(null);
            setText(null);
        }
        else {
            if(fxmlLoader == null){
                fxmlLoader = new FXMLLoader(getClass().getResource("GroupChatListCell.fxml"));
                fxmlLoader.setController(this);
                try {
                    fxmlLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            labelGroupname.setText(group.getName());
            setGraphic(hBox);
        }
    }
}
