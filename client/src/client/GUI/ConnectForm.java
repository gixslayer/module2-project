package client.GUI;

import client.ClientController;
import client.network.Connection;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
    private JLabel connectionStatus;
    private JFrame frame;

    public ConnectForm(ClientController c, GuiController argGuiController) {
        this.client = c;
        connectionStatus.setText("Not connected");
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

    public void startClient() {
        client.setConnection(new Connection(client));
        client.getConnection().setServerport((int) port.getValue());
        client.getConnection().setServername((String) ipadres.getValue());
        client.getConnection().start();
        connectionStatus.setText("Connecting...");
        try{
            sleep(200);
        }catch(InterruptedException e){
            e.printStackTrace();
        }
        if (!client.isConnected()){
            connectionStatus.setText("Connection failed");
        }
    }

    public void stopFrame(){
        connectionStatus.setText("Connected");
        try {
            sleep(150);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        frame.setVisible(false);
        frame.dispose();
        try {
            guiController.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public  void run() {
        frame = new JFrame("ConnectForm");
        frame.setContentPane(this.connect);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

}
