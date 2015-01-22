package client.GUI;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JPanel;

import client.ClientController;

/**
 * Created by joran on 21-1-15.
 */
public class ConnectForm extends Thread {
    private JPanel connect;
    private JButton connectButton;
    private ClientController client;
    private GuiController guiController;
    private JFormattedTextField port;
    private JFormattedTextField ipadres;
    private JFrame frame;

    public ConnectForm(ClientController c, GuiController argGuiController) {
        this.client = c;
        this.guiController = argGuiController;
        this.port.setValue(6666);
        this.ipadres.setValue("127.0.0.1");
        connectButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                startClient();
            }
        });

    }

    public synchronized void startClient() {
        client.getConnection().setServerport((int) port.getValue());
        client.getConnection().setServername((String) ipadres.getValue());
        client.getConnection().start();
        frame.setVisible(false);
        frame.dispose();
        guiController.runGameGui();
        try {
            join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void run() {
        frame = new JFrame("ConnectForm");
        frame.setContentPane(this.connect);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

}
