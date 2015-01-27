package client.GUI;

import client.ClientController;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;

/**
 * Created by joran on 26-1-15.
 */
public class MainForm extends Thread{
    JFrame frame;
    private ClientController clientController;
    private JPanel panel1;
    private JList list1;
    private JTextArea textArea1;
    private JFormattedTextField formattedTextField1;
    private JButton readyButton;


    public MainForm(ClientController clientController){
        textArea1.setEditable(false);
        this.clientController = clientController;
        formattedTextField1.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER){
                    sendGlobalMessage(formattedTextField1.getText());
                    newMessage(formattedTextField1.getText());
                    formattedTextField1.setText("");
                }
            }
        });

        readyButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                sendReady();
            }
        });
    }
    public void sendReady(){
        clientController.getConnection().getProtocol().sendReady();

    }
    public void sendGlobalMessage(String m){
        clientController.sendGlobalMessage(m);
    }

    public void run(){
        frame = new JFrame("MainForm");
        frame.setContentPane(this.panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
    public void close(){
        frame.setVisible(false);
        frame.dispose();
        try {
            clientController.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void updateLobby(){
        HashSet e = clientController.getLobby();
        list1.setListData(e.toArray());
    }


    public void winnerMessage(String winner){
        if(textArea1.getLineCount()>20) {
            int end = 0;
            try {
                end = textArea1.getLineEndOffset(0);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
            textArea1.replaceRange("", 0, end);
        }
        if (winner == null){
            textArea1.append(String.format("[System] The game ended in a draw! %n"));
        }else{
            textArea1.append(String.format("[System] The winner is %n"));
        }
    }
    public void newMessage(String arg){
        if(textArea1.getLineCount()>(textArea1.getSize().getHeight()/15)){
            int end = 0;
            try {
                end = textArea1.getLineEndOffset(0);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
            textArea1.replaceRange("", 0, end);
        }
        textArea1.append(String.format("[Me] %s %n",arg));
    }

    public void newMessage(String[] args){


        if(textArea1.getLineCount()>20) {
            int end = 0;
            try {
                end = textArea1.getLineEndOffset(0);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
            textArea1.replaceRange("", 0, end);
        }
        textArea1.append(String.format("[%s]",args[0]));
        for (int i =1; i < args.length ; i ++){
            textArea1.append(String.format(" %s", args[i]));
        }
        textArea1.append(String.format("%n"));
    }
}
