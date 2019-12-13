package client.presentation.listcells;

import common.Util;
import common.data.Message;
import javafx.beans.property.DoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.BorderPane;

import java.io.IOException;


public class ChatListViewCell extends ListCell<Message> {
    //https://www.turais.de/how-to-custom-listview-cell-in-javafx/
    @FXML
    private Label labelSender;

    @FXML
    private Label labelTime;

    @FXML
    private Label labelText;

    @FXML
    private BorderPane borderPane;

    private FXMLLoader fxmlLoader;
    private DoubleProperty listViewWidthProperty;

    public ChatListViewCell(DoubleProperty listViewWidthProperty) {
        this.listViewWidthProperty=listViewWidthProperty;
    }

    @Override
    protected void updateItem(Message message, boolean empty){
        super.updateItem(message,empty);
        if(empty || message == null){
            setGraphic(null);
            setText(null);
        }
        else {
            if(fxmlLoader == null){
                fxmlLoader = new FXMLLoader(getClass().getResource("ChatListCell.fxml"));
                fxmlLoader.setController(this);
                try {
                    fxmlLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            labelTime.setText(Util.dateFormat.format(message.getTimestamp()).replace('\t', '\n'));//message.getTimestamp()//.toString().split("\\.")[0]);
            labelText.setText(message.getText());
            labelText.setWrapText(true);
            labelSender.setText(message.getSender());
            /** Fit Width to ListView width*/
            borderPane.prefWidthProperty().bind(listViewWidthProperty.subtract(2));
            setGraphic(borderPane);
        }

    }


}
