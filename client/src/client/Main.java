package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main {
    //Variables
    public static final Main INSTANCE = new Main();
    private boolean keepRunning;
    private BufferedReader input;

    //Constructor
    private Main(){
        this.input = new BufferedReader(new InputStreamReader(System.in));
        keepRunning= true;
    }
    //Main
    public static void main(String[] args) {
        new ClientController().start();
    }


}
