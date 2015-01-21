package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import client.server.Client;

public class Main {
    //Variables
    public static final Main INSTANCE = new Main();
    private boolean keepRunning;
    private BufferedReader input;

    //Constructor
    private Main() {
        this.input = new BufferedReader(new InputStreamReader(System.in));
        keepRunning = true;
    }

    //Main
    public static void main(String[] args) {
        INSTANCE.gui();
    }

    public void gui() {
        Client client = new Client();
        ConnectForm m = new ConnectForm(client);
        m.start();
    }

}
