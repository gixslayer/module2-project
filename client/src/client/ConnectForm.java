package client;

import client.server.Client;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by joran on 21-1-15.
 */
public class ConnectForm extends Thread{
    private JPanel connect;
    private JButton connectButton;
    private Client client;
    private JFormattedTextField port ;
    private JFormattedTextField ipadres;
    private JFrame frame;


    public ConnectForm(Client c) {
        this.client = c;
        this.port.setValue(6666);
        this.ipadres.setValue("127.0.0.1");
        connectButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                startClient();
            }
        });

    }


    public void startClient(){
        client.setServerport((int) port.getValue());
        client.setServername((String) ipadres.getValue());
        client.start();
        frame.setVisible(false);
        try {
            GameGUI.main(null);
            this.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        frame = new JFrame("ConnectForm");
        frame.setContentPane(this.connect);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }


}
