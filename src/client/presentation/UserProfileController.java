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

import static common.Constants.Contexts.DELETE_ACCOUNT;
import static common.Constants.Contexts.PROFILE_UPDATE;

public class UserProfileController extends UIController {

	@FXML
	private Button btnSave, btnCancel, btnDeleteAccount;

	@FXML
	private TextField txtAboutMe, txtNewPassword, txtNewPasswordConfirm, txtCurrentPassword;

	@FXML
	private Label lblUserName, lblError;

	private Account loggedInAccount;

	@FXML
	private void initialize() {
		btnSave.setOnMouseClicked(ev -> onSaveClick());
		btnCancel.setOnMouseClicked(ev -> onCancelClick());
		btnDeleteAccount.setOnMouseClicked(ev -> onDeleteAccountClick());
		this.loggedInAccount = Client.getInstance(Server.SERVER_HOST, Server.SERVER_PORT).getAccount();
		if (this.loggedInAccount != null) {
			lblUserName.setText("Hi, " + this.loggedInAccount.getUserName() + "!");
			this.txtAboutMe.setText(this.loggedInAccount.getAboutMe() != null ? this.loggedInAccount.getAboutMe() : "");
		}
	}
	private void onDeleteAccountClick() {
		if (this.correctPasswordEntered()) {
			this.client.sendToServer(DELETE_ACCOUNT, this.loggedInAccount);
			Platform.exit();
		} else {
			lblError.setText("Current password is invalid");
		}
	}

	private void onSaveClick() {
		this.loggedInAccount.setAboutMe(this.txtAboutMe.getText());
		try {
			this.handlePasswordGuardedProfileChanges();
			this.client.sendToServer(PROFILE_UPDATE, this.loggedInAccount);
			client.closeCurrentWindowNoexit();
		} catch (Exception e) {
			System.err.println(e.getMessage());
			lblError.setText(e.getMessage());
		}
	}

	private void handlePasswordGuardedProfileChanges() throws Exception {
		if (!"".equals(this.txtNewPassword.getText())) {
			if (this.correctPasswordEntered()) {
				if (txtNewPassword.getText().isEmpty() || !txtNewPassword.getText().equals(txtNewPasswordConfirm.getText())) {
					throw new Exception("New passwords do not match.");
				}
			} else {
				throw new Exception("Current password is invalid");
			}
			this.loggedInAccount.setPassword(txtNewPassword.getText());
		}
	}

	private boolean correctPasswordEntered() {
		return this.loggedInAccount.getPassword().equals(this.txtCurrentPassword.getText());
	}

	private void onCancelClick() {
		Platform.runLater(() -> ((Stage) btnCancel.getScene().getWindow()).close());
	}

	public void close() {
		Platform.runLater(() -> ((Stage) btnSave.getScene().getWindow()).close());
	}
}
