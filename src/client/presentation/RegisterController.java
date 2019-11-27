package client.presentation;

import common.data.Account;
import common.data.AccountBuilder;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import server.TranslationService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static common.Constants.Contexts.REGISTER;


public class RegisterController extends UIController {

    @FXML
    private TextField txtUsername;
    @FXML
    private PasswordField txtPassword, txtRepeat;
    @FXML
    private MenuButton menuLanguages;
    private List<String> selectedLanguages;
    @FXML
    private Button btnRegister;

    private int languageCounter;

    @FXML
    private void initialize() {
        languageCounter = 0;
        selectedLanguages = new ArrayList<>();
        /** Init Item List with Languages from TranslationService */
        final List<CheckMenuItem> items = TranslationService.getSupportedLanguages().keySet()
                .stream().sorted().map(CheckMenuItem::new).collect(Collectors.toList());
        menuLanguages.getItems().addAll(items);

        /** Add languages to selectedLangauges and highlight in item List*/
        for (final CheckMenuItem item : items) {
            item.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
                if (newValue) {
                    /** Add language */
                    selectedLanguages.add(item.getText());
                    languageCounter++;
                    item.setText(languageCounter + "." + item.getText());
                    menuLanguages.setStyle(""); //TODO Styling via .css
                } else {
                    /** Remove language*/
                    String t = item.getText();
                    String[] s = t.split("\\.");
                    item.setText(s[1]);
                    selectedLanguages.remove(s[1]);
                    languageCounter--;
                    /** Update selected numbers*/
                    for (int i = 0; i < languageCounter; i++) {
                        String lang = selectedLanguages.get(i);
                        for (CheckMenuItem menuItem : items.stream().filter(CheckMenuItem::isSelected)
                                .collect(Collectors.toList())) {
                            if (menuItem.getText().split("\\.")[1].equals(lang)) {
                                menuItem.setText((i + 1) + "." + lang);
                                break;
                            }
                        }
                    }
                    if (selectedLanguages.isEmpty()) {
                        //TODO Styling via .css
                        menuLanguages.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                    }
                }
                menuLanguages.setText(String.join(",", selectedLanguages));
            });
        }
        /** Select English as default language*/
        for (CheckMenuItem item : items) {
            if (item.getText().equals(TranslationService.ENGLISH_KEY)) {
                item.setSelected(true);
                break;
            }
        }
        btnRegister.setOnMouseClicked(ev -> onRegisterClicked());
    }

    private void onRegisterClicked() {
        /** Check if all Inputs are valid */
        if(txtUsername.getText().isEmpty() || txtPassword.getText().isEmpty()
                || !txtPassword.getText().equals(txtRepeat.getText())
                || selectedLanguages.isEmpty()) {
            System.err.println("Invalid Input");
            return;
        }

        Account acc=new AccountBuilder()
                .setUserName(txtUsername.getText())
                .setPassword(txtPassword.getText())
                .setLanguages(selectedLanguages.stream()
                        .map(TranslationService.languages::get)
                        .collect(Collectors.toList()))
                .createAccount();
        client.sendToServer(REGISTER, acc);
    }

    @Override
    public void close() {
        Platform.runLater(() -> ((Stage) btnRegister.getScene().getWindow()).close());
    }
}
