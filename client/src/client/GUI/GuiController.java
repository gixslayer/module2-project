package client.GUI;

import client.ClientController;

/**
 * Created by joran on 21-1-15.
 */
public class GuiController extends Thread{
    private static ClientController clientController;
    ConnectForm connectForm;
    GameGUI gameGUI;

    public GuiController(ClientController clientController){
        this.clientController = clientController;
        connectForm = new ConnectForm(clientController, this);
    }
    @Override
    public void run(){
        connectForm.start();
    }
    public void runGameGui(){
        gameGUI = new GameGUI(clientController);
    }
}
