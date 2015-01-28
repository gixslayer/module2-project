package client.GUI;

import client.ClientController;

/**
 * Created by joran on 21-1-15.
 */
public class GuiController extends Thread {
    private static ClientController clientController;
    ConnectForm connectForm;
    ControlForm controlForm;
    MainForm mainForm;

    public GuiController(ClientController argClientController) {
        clientController = argClientController;
        connectForm = new ConnectForm(argClientController, this);
        mainForm = new MainForm(clientController);
    }

    @Override
    public void run() {
        openConnectForm();
    }

    public void openMainForm() {
        mainForm.start();
    }
    public void closeMainform(){mainForm.close();}
    public void openConnectForm(){
        connectForm.start();
    }

    public void closeConnectForm(){
        connectForm.stopFrame();
        openMainForm();
    }

    public void openControlForm(){
        controlForm = new ControlForm(this);
        controlForm.start();
    }
    public void closeControlForm(){
        controlForm.close();
    }
    public void sendWinnerMessage(String winner){
        mainForm.winnerMessage(winner);

    }
    public ConnectForm getConnectForm() {
        return connectForm;
    }

    public void sendMessageChatNotEnabeled(){
        mainForm.sendMessageChatNotEnabeled();
        if (controlForm != null){
            controlForm.sendMessageChatNotEnabeled();
        }
    }

    public static ClientController getClientController() {
        return clientController;
    }

    public ControlForm getControlForm() {
        return controlForm;
    }

    public MainForm getMainForm() {
        return mainForm;
    }
}