package client.presentation.listcells;

import client.presentation.FXUtil;
import common.Util;
import common.data.Message;
import javafx.beans.property.DoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.IOException;


public class ChatListViewCell extends ListCell<Message> {
    //https://www.turais.de/how-to-custom-listview-cell-in-javafx/
    @FXML
    private Label labelSender;

    @FXML
    private Label labelTime;

    @FXML
    private TextFlow textFlow;

    @FXML
    private BorderPane borderPane;

    private FXMLLoader fxmlLoader;
    private DoubleProperty listViewWidthProperty;

    public ChatListViewCell(DoubleProperty listViewWidthProperty) {
        this.listViewWidthProperty=listViewWidthProperty;
    }

    @Override
    protected void updateItem(Message message, boolean empty) {
        super.updateItem(message, empty);
        if (empty || message == null) {
            setGraphic(null);
            setText(null);
        }
        else {
            if (fxmlLoader == null) {
                fxmlLoader = new FXMLLoader(getClass().getResource("ChatListCell.fxml"));
                fxmlLoader.setController(this);
                try {
                    fxmlLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            labelTime.setText(Util.dateFormat.format(message.getTimestamp()).replace('\t', '\n'));//message.getTimestamp()//.toString().split("\\.")[0]);
            textFlow.getChildren().clear();
            /** Render text + smileys */
            String msg = message.getText();
            while (true) {
                int textEnd = msg.indexOf("<i>");
                Text txt = new Text();
                //TODO CSS CLASS
                txt.setFill(Color.WHITE);
                if (textEnd == -1) {
                    txt.setText(msg);
                    textFlow.getChildren().add(txt);
                    break;
                }
                String text = msg.substring(0, textEnd);
                if (text.length() > 0) {
                    txt.setText(text);
                    textFlow.getChildren().add(txt);
                }
                int smileyNumber = Integer.parseInt(msg.substring(textEnd + 3, textEnd + 3 + 3)) - 1;
                ImageView imgSmiley = new ImageView(FXUtil.getSmileyImage(smileyNumber));

                textFlow.getChildren().add(imgSmiley);
                msg = msg.substring(textEnd + 3 + 3 + 3 + 1);
            }

          /*  labelText.setText(message.getText());
            labelText.setWrapText(true);*/
            labelSender.setText(message.getSender());
            /** Fit Width to ListView width*/
            borderPane.prefWidthProperty().bind(listViewWidthProperty.subtract(2));
            setGraphic(borderPane);
        }

    }


}
