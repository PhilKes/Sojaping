package client.presentation;

import client.Client;
import common.data.Account;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import server.Server;

import static common.Constants.Contexts.PROFILE_UPDATE;

public class UserProfileController extends UIController {

	@FXML
	private Button btnSave, btnCancel;

	@FXML
	private TextField txtAboutMe, txtNewPassword, txtNewPasswordConfirm, txtCurrentPassword;

	@FXML
	private Label lblUserName;

	private Account loggedInAccount;

	@FXML
	private void initialize() {
		btnSave.setOnMouseClicked(ev -> onSaveClick());
		btnCancel.setOnMouseClicked(ev -> onCancelClick());
		this.loggedInAccount = Client.getInstance(Server.SERVER_HOST, Server.SERVER_PORT).getAccount();
		if (this.loggedInAccount != null) {
			lblUserName.setText("Hi, " + this.loggedInAccount.getUserName() + "!");
			this.txtAboutMe.setText(this.loggedInAccount.getAboutMe() != null ? this.loggedInAccount.getAboutMe() : "");
		}
	}

	private void onSaveClick() {
		this.loggedInAccount.setAboutMe(this.txtAboutMe.getText());
		this.handlePasswordGuardedProfileChanges();
		this.client.sendToServer(PROFILE_UPDATE, this.loggedInAccount);
		this.close();
	}

	private void handlePasswordGuardedProfileChanges() {
		if (this.loggedInAccount.getPassword().equals(this.txtCurrentPassword.getText())) {
			if (txtNewPassword.getText().isEmpty() || !txtNewPassword.getText().equals(txtNewPasswordConfirm.getText())) {
				System.err.println("New passwords do not match.");
				return;
			}
		} else {
			System.err.println("Invalid password.");
			return;
		}
		this.loggedInAccount.setPassword(txtNewPassword.getText());
	}

	private void onCancelClick() {
		Platform.runLater(() -> ((Stage) btnCancel.getScene().getWindow()).close());
	}

	public void close() {
		Platform.runLater(() -> ((Stage) btnSave.getScene().getWindow()).close());
	}
}
