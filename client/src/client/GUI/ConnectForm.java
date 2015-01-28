package client.GUI;

import client.ClientController;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by joran on 21-1-15.
 */
public class ConnectForm extends Thread {
    private static final String INITIALIP = "127.0.0.1";
    private static final String INITIALNAME = "Name";
    private static final String INITIALGROUP = "19";
    private static final Integer INITALPORT = 6666;
    private JPanel connect;
    private JButton connectButton;
    private ClientController client;
    private GuiController guiController;
    private JFormattedTextField port;
    private JFormattedTextField ipadres;
    private JLabel connectionStatus;
    private JFormattedTextField group;
    private JFormattedTextField name;
    private JFrame frame;

    public ConnectForm(ClientController c, GuiController argGuiController) {
        this.client = c;
        connectionStatus.setText("Not connected");
        this.guiController = argGuiController;
        this.port.setValue(INITALPORT);
        this.ipadres.setValue(INITIALIP);
        this.name.setValue(INITIALNAME);
        this.group.setValue(INITIALGROUP);
        connectButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                startClient();
            }
        });

    }

    public void startClient() {
        client.newConnection();
        //Set the port en servername
        client.getConnection().setServerport((int) port.getValue());
        client.getConnection().setServername((String) ipadres.getValue());
        //Set the group and username
        client.setClientName((String) name.getValue());
        client.setGroup((String) group.getValue());


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
