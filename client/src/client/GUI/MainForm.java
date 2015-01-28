package client.GUI;

import client.ClientController;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

/**
 * Created by joran on 26-1-15.
 */
public class MainForm extends Thread {
    //--------------------------------------------Fields----------------------------------------------------------------
    JFrame frame;
    private ClientController clientController;
    private JPanel panel1;
    private JList<Object> list1;
    private JTextArea textArea1;
    private JFormattedTextField formattedTextField1;
    private JButton readyButton;
    //--------------------------------------------Constructor-----------------------------------------------------------
    public MainForm(ClientController argClientController) {
        textArea1.setEditable(false);
        this.clientController = argClientController;
        formattedTextField1.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
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
    //-------------------------------------------Methods----------------------------------------------------------------

    public void sendReady() {
        clientController.getConnection().getProtocol().sendReady();
        switchReadyButton();

    }

    public void run() {
        frame = new JFrame("MainForm");
        frame.setContentPane(this.panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public void close() {
        frame.setVisible(false);
        frame.dispose();
        try {
            clientController.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void updateLobby() {
        HashMap<String, String> e = clientController.getLobby();
        Object[] result = e.keySet().toArray();
        for (int i = 0; i < result.length; i++) {
            if (e.get(result[i]) != null) {
                result[i] = result[i] + " ~" + e.get(result[i]);
            }
        }
        list1.setListData(result);
    }

    public void switchReadyButton() {
        readyButton.setEnabled(!readyButton.isEnabled());
    }

    //---------------Handle Messages-------------------------------------------

    public void sendGlobalMessage(String m) {
        clientController.sendGlobalMessage(m);
    }

    public void sendMessageChatNotEnabeled() {
        textArea1.append(String
                .format("[System] I'm sorry, but Chatting is not enabeled on this server. %n"));
    }

    public void winnerMessage(String winner) {
        if (textArea1.getLineCount() > 20) {
            int end = 0;
            try {
                end = textArea1.getLineEndOffset(0);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
            textArea1.replaceRange("", 0, end);
        }
        if (winner == null) {
            textArea1.append(String.format("[System] The game ended in a draw! %n"));
        } else {
            textArea1.append(String.format("[System] The winner is %s %n", winner));
        }
    }

    public void newMessage(String arg) {
        cleanUpChat();
        textArea1.append(String.format("[Me] %s %n", arg));
    }

    public void newMessage(String[] args) {
        cleanUpChat();
        textArea1.append(String.format("[%s]", args[0]));
        for (int i = 1; i < args.length; i++) {
            textArea1.append(String.format(" %s", args[i]));
        }
        textArea1.append(String.format("%n"));
    }

    public void cleanUpChat() {
        if (textArea1.getLineCount() > (textArea1.getSize().getHeight() / 15)) {
            int end = 0;
            try {
                end = textArea1.getLineEndOffset(0);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
            textArea1.replaceRange("", 0, end);
        }
    }


}
