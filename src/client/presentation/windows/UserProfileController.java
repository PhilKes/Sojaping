package client.presentation.windows;

import client.Client;
import client.presentation.FXUtil;
import client.presentation.UIControllerWithInfo;
import common.Constants;
import common.data.Account;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static common.Constants.Contexts.DELETE_ACCOUNT;
import static common.Constants.Contexts.PROFILE_UPDATE;
import static common.Constants.SERVER_HOST;
import static common.Constants.SERVER_PORT;

public class UserProfileController extends UIControllerWithInfo {

	@FXML
	private Button btnSave, btnCancel, btnDeleteAccount;

	@FXML
	private TextField txtAboutMe, txtNewPassword, txtNewPasswordConfirm, txtCurrentPassword;

	@FXML
    private Label lblUserName;

	private Account loggedInAccount;

	@FXML
	private MenuButton menuLanguages;

	private List<String> selectedLanguages;

    private int[] languageCounter;

	@FXML
	private void initialize() {
		btnSave.setOnMouseClicked(ev -> onSaveClick());
		btnCancel.setOnMouseClicked(ev -> onCancelClick());
		btnDeleteAccount.setOnMouseClicked(ev -> onDeleteAccountClick());
        this.loggedInAccount=Client.getInstance(SERVER_HOST, SERVER_PORT).getAccount();
		if (this.loggedInAccount != null) {
			lblUserName.setText("Hi, " + this.loggedInAccount.getUserName() + "!");
			this.txtAboutMe.setText(this.loggedInAccount.getAboutMe() != null ? this.loggedInAccount.getAboutMe() : "");
		}

		this.initializeLanguageDropDown();
	}

	private void initializeLanguageDropDown() {
        languageCounter=new int[]{0};
		selectedLanguages=new ArrayList<>();

        Map<String, String> languageAbbrToFull=Constants.Translation.languages.entrySet().stream().collect(Collectors.toMap(Entry::getValue, Entry::getKey));

        FXUtil.fillLanguageMenu(menuLanguages, selectedLanguages, languageCounter);
		List<String> languages = this.loggedInAccount.getLanguages();
        /** Select languages of profile*/
		for (String language: languages){
			language = languageAbbrToFull.get(language);
			ObservableList<MenuItem> items = this.menuLanguages.getItems();
			for (MenuItem item: items) {
				CheckMenuItem checkItem = (CheckMenuItem) item;
				if(checkItem.getText().equals(language)) {
					checkItem.setSelected(true);
				}
			}
		}
	}
	private void onDeleteAccountClick() {
		if (this.correctPasswordEntered()) {
			this.client.sendToServer(DELETE_ACCOUNT, this.loggedInAccount);
			Platform.exit();
		} else {
            showInfo("Current password is invalid", InfoType.ERROR);
		}
	}

	private void onSaveClick() {
		this.loggedInAccount.setAboutMe(this.txtAboutMe.getText());
		this.loggedInAccount.setLanguages(selectedLanguages.stream()
                .map(key -> Constants.Translation.languages.get(key))
				.collect(Collectors.toList()));
		try {
			this.handlePasswordGuardedProfileChanges();
			this.client.sendToServer(PROFILE_UPDATE, this.loggedInAccount);
            client.getGUIController().loadAccount(loggedInAccount);
            client.closeCurrentWindowNoExit();
		} catch (Exception e) {
            showInfo(e.getMessage(), InfoType.ERROR);
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
        //Platform.runLater(() -> ((Stage) btnCancel.getScene().getWindow()).close());
        client.closeCurrentWindowNoExit();
    }
}
