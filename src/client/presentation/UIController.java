package client.presentation;

import client.Client;

public abstract class UIController {
    protected Client client;
    public abstract void close();

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client=client;
    }
}
