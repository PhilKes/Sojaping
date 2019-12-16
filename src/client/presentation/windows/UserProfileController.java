package client.presentation.windows;

import client.Client;
import client.presentation.FXUtil;
import client.presentation.TitleBarController;
import client.presentation.UIControllerWithInfo;
import common.Constants;
import common.data.Account;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static common.Constants.Contexts.*;
import static common.Constants.SERVER_HOST;
import static common.Constants.SERVER_PORT;

public class UserProfileController extends UIControllerWithInfo {

	@FXML
	private Button btnSave, btnCancel, btnDeleteAccount, btnUploadPic, btnInvite;

	@FXML
	private TextField txtAboutMe, txtNewPassword, txtNewPasswordConfirm, txtCurrentPassword;

	@FXML
    private Label lblUserName;

	private Account loggedInAccount;

	@FXML
	private MenuButton menuLanguages;

	private List<String> selectedLanguages;

	private String base64ProfilePic;

    private int[] languageCounter;

	@FXML
	private void initialize() {
		btnSave.setOnMouseClicked(ev -> onSaveClick());
		btnCancel.setOnMouseClicked(ev -> onCancelClick());
		btnDeleteAccount.setOnMouseClicked(ev -> onDeleteAccountClick());
		btnUploadPic.setOnMouseClicked(ev -> onUploadPicClick());
		btnInvite.setOnMouseClicked(event -> onInviteClick());
        this.loggedInAccount=Client.getInstance(SERVER_HOST, SERVER_PORT).getAccount();
		if (this.loggedInAccount != null) {
			lblUserName.setText("Hi, " + this.loggedInAccount.getUserName() + "!");
			this.txtAboutMe.setText(this.loggedInAccount.getAboutMe() != null ? this.loggedInAccount.getAboutMe() : "");
		}

		this.initializeLanguageDropDown();
	}
	private void onUploadPicClick() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Choose profile picture");

		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif", "*.bmp", "*.jpeg"));

		File file = fileChooser.showOpenDialog(stage);
		if (file != null && file.length() > 3000000) {
			showInfo("Image file is too large. Please, upload a picture < 3 MB", InfoType.ERROR);
		} else if(file != null){
			this.base64ProfilePic = FXUtil.convertFileToBase64(file.getAbsolutePath());
		}
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
		if(this.base64ProfilePic != null && !"".equals(this.base64ProfilePic)){
			this.loggedInAccount.setProfilePicture(this.base64ProfilePic);
		}
		try {
			this.handlePasswordGuardedProfileChanges();
			this.client.sendToServer(PROFILE_UPDATE, this.loggedInAccount);
            client.getGUIController().loadAccount(loggedInAccount);
            client.closeCurrentWindowNoExit();
		} catch (Exception e) {
            showInfo(e.getMessage(), InfoType.ERROR);
		}
	}
	private void onInviteClick(){
		String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
				"[a-zA-Z0-9_+&*-]+)*@" +
				"(?:[a-zA-Z0-9-]+\\.)+[a-z" +
				"A-Z]{2,7}$";
		Pattern pat = Pattern.compile(emailRegex);
		Stage window = new Stage();
		window.initModality(Modality.APPLICATION_MODAL);
		window.setMinWidth(300);
		window.setTitle("Enter E-Mail");
		window.initStyle(StageStyle.UNDECORATED);
		window.getIcons().add(FXUtil.getDefaultIcon());
		TextField textField = new TextField();
		Button yButton = new Button("Send E-Mail");
		Label labelError = new Label();
		yButton.getStyleClass().add("green");
		yButton.setOnAction(e -> {
			if(pat.matcher(textField.getText()).matches()){
				client.sendToServer(INVITATION_EMAIL,textField.getText());
				FXUtil.showInfo(labelError, "E-Mail send", InfoType.INFO);
				System.out.println("E-Mail send");
			}
			else
				FXUtil.showInfo(labelError, "Invalid E-Mail",InfoType.ERROR);
				System.err.println("Invalid E-Mail");
		});
		VBox layout = new VBox(5);
		layout.getChildren().addAll(textField,labelError, yButton);
		layout.setAlignment(Pos.CENTER);
		VBox wrapBox=new VBox();
		wrapBox.setId("window-wrapper");
		wrapBox.getStylesheets().add(getClass().getResource("../resources/main.css").toExternalForm());
		FXMLLoader titleBarLoader=new FXMLLoader(getClass().getResource("../TitleBar.fxml"));
		TitleBarController titleBarController=new TitleBarController();
		titleBarController.setStage(window);
		titleBarLoader.setController(titleBarController);
		HBox titleBar=null;
		try {
			titleBar=titleBarLoader.load();
			titleBar.prefWidthProperty().bind(layout.prefWidthProperty());
			wrapBox.getChildren().addAll(titleBar, layout);
			Scene scene=new Scene(wrapBox);
			window.setScene(scene);
			Platform.runLater(() -> textField.requestFocus());
			window.showAndWait();
		}
		catch(IOException e) {
			e.printStackTrace();
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

    public void onResetStoreClicked(ActionEvent actionEvent) {
        client.resetLocalMessageStore();
    }
}
