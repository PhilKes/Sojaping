package server;

import common.data.Account;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;

/**
 * Socket connection to host with Input/Output Streams and loggedAcount if logged in
 */
public class Connection {

    private PrintStream streamOut;
    private InputStream streamIn;
    private Socket socket;
    private String nickname;
    private Account loggedAccount;

    public Connection(String host, int port, String name) throws IOException {
        this(new Socket(host, port), name);
    }

    public Connection(Socket socket, String name) throws IOException {
        this.socket = socket;
        this.nickname = name;
        this.loggedAccount = null;
        this.streamOut = new PrintStream(socket.getOutputStream());
        this.streamIn = socket.getInputStream();
    }

    public PrintStream getOutStream() {
        return this.streamOut;
    }

    public InputStream getInputStream() {
        return this.streamIn;
    }

    public Socket getSocket() {
        return socket;
    }

    public String getNickname() {
        return this.nickname;
    }

    public Account getLoggedAccount() {
        return loggedAccount;
    }

    public void setLoggedAccount(Account loggedAccount) {
        if (this.loggedAccount != null) {
            if (loggedAccount == null) {
                this.loggedAccount.setStatus(0);
            } else {
                this.loggedAccount.setStatus(1);
            }
        }
        else if(loggedAccount!=null) {
            loggedAccount.setStatus(1);
        }
        this.loggedAccount = loggedAccount;
    }

    public boolean isLoggedIn() {
        return this.loggedAccount != null;
    }
}
