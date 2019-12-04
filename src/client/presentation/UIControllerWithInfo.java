package client.presentation;

import common.Util;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Window with Label for Errors
 */
public abstract class UIControllerWithInfo extends UIController {

    /**
     * Info types to style Label text -> main.css #labelError,...
     */
    public enum InfoType {
        ERROR("labelError"),
        INFO("labelInfo"),
        SUCCESS("labelSuccess");
        private final String type;

        InfoType(String type) {
            this.type = type;
        }

        public String get() {
            return type;
        }
    }

    @FXML
    private Label labelError;

    public void showInfo(String message, InfoType type) {
        Util.showError(labelError, message, type);
    }
}
