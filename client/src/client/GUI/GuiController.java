package client.GUI;

import client.ClientController;

/**
 * Created by joran on 21-1-15.
 */
public class GuiController extends Thread {
    //--------------------------------------------Fields----------------------------------------------------------------
    private static ClientController clientController;
    ConnectForm connectForm;
    ControlForm controlForm;
    MainForm mainForm;
    //--------------------------------------------Constructor-----------------------------------------------------------
    public GuiController(ClientController argClientController) {
        clientController = argClientController;
        connectForm = new ConnectForm(argClientController, this);
        mainForm = new MainForm(clientController);
    }
    //-------------------------------------------Methods----------------------------------------------------------------
    @Override
    public void run() {
        openConnectForm();
    }

    //--------------------Open and close forms-------------------------
    public void openMainForm() {
        mainForm.start();
    }

    public void closeMainform() {
        mainForm.close();
    }

    public void openConnectForm() {
        connectForm.start();
    }

    public void closeConnectForm() {
        connectForm.stopFrame();
        openMainForm();
    }

    public void openControlForm() {
        controlForm = new ControlForm(this);
        controlForm.start();
    }

    public void closeControlForm() {
        controlForm.close();
    }

    //------------------SendMessages--------------------------------------
    public void sendWinnerMessage(String winner) {
        mainForm.winnerMessage(winner);

    }

    public void sendMessageChatNotEnabeled() {
        mainForm.sendMessageChatNotEnabeled();
        if (controlForm != null) {
            controlForm.sendMessageChatNotEnabeled();
        }
    }

    //-------------------GettersAndSetters-----------------------------------

    public static ClientController getClientController() {
        return clientController;
    }

    public ControlForm getControlForm() {
        return controlForm;
    }

    public ConnectForm getConnectForm() {
        return connectForm;
    }

    public MainForm getMainForm() {
        return mainForm;
    }
}