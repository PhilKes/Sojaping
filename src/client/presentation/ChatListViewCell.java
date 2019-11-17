package client.presentation;

import common.data.Message;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.text.SimpleDateFormat;


public class ChatListViewCell extends ListCell<Message> {
    //Todo https://www.turais.de/how-to-custom-listview-cell-in-javafx/
    //Todo new line for long text
    @FXML
    private Label labelSender;

    @FXML
    private Label labelTime;

    @FXML
    private Label labelText;

    @FXML
    private HBox hbox;

    private FXMLLoader fxmlLoader;
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
            labelTime.setText(new SimpleDateFormat("HH:mm:ss \n dd-MM-yy").format(message.getTimestamp()));//message.getTimestamp()//.toString().split("\\.")[0]);
            labelText.setText(message.getText());
            labelSender.setText(message.getSender());
            setGraphic(hbox);
        }

    }



}
