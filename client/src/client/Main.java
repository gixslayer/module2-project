package client;

public class Main {
    //Variables
    public static final Main INSTANCE = new Main();


    //Constructors
    private Main() {

    }

    //Mainstart
    public static void main(String[] args) {
        new ClientController().start();
    }

}
