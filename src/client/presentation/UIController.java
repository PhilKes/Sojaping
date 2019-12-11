package client.presentation;

import client.Client;
import javafx.application.Platform;
import javafx.stage.Stage;

public abstract class UIController {
    protected Client client;
    protected Stage stage;

    public void close() {
        Platform.runLater(() -> stage.close());
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client=client;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage=stage;
    }
}
