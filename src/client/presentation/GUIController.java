package client.presentation;


import common.data.Message;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

import java.sql.Timestamp;

public class GUIController {
	@FXML
	private Button btnSend;
	@FXML
	private Button btnMyProfile;
	@FXML
	private TextArea textASendText;
	@FXML
	private ListView listVChat;
	@FXML
	private CheckBox checkTranslate;

	@FXML
	private void initialize() {
		btnSend.setOnMouseClicked(ev ->onSendClicked());
	}

	private void onSendClicked() {
		if(!textASendText.getText().isEmpty()){
			Message newMessage = new Message(checkTranslate.isSelected(), textASendText.getText(), new Timestamp(System.currentTimeMillis()));
			listVChat.getItems().add(listVChat.getItems().size(),newMessage.getTimestamp().toString() +" "+ newMessage.getText());
			textASendText.clear();
			//Todo send Message to Server
		}
		else{

		}
	}


}
