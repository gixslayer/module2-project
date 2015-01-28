package client.GUI;

import client.ClientController;
import findfour.shared.game.Disc;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by joran on 26-1-15.
 */
public class ControlForm extends Thread {
    //--------------------------------------------Fields----------------------------------------------------------------
    //Canvas fields
    public static final int ROWS = 6;
    public static final int COLS = 7;
    public static final int CELLSIZE = 100;
    public static final int CANVAS_WIDTH = CELLSIZE * COLS;
    public static final int CANVAS_HEIGHT = CELLSIZE * ROWS;
    public static final int GRID_WIDTH = 8;
    public static final int CELL_PAD = CELLSIZE / 8;
    public static final int SYMBOLSIZE = CELLSIZE - CELL_PAD * 2;
    public static final int SYMBOL_STROKE = 8;

    public final GuiController guiController;
    public final ClientController client;

    private DrawBoard canvas; // Drawing canvas (JPanel) for the game board
    private JLabel statusBar; // Status Bar

    //Form fields
    JFrame frame;
    private JPanel mainPanel;
    private JPanel panel;
    private JLabel gameState;

    private JFormattedTextField formattedTextField1;
    private JTextArea textArea1;
    private JButton enableAIButton;
    private JButton disableAIButton;
    private JButton hintButton;
    private JFormattedTextField formattedTextField2;
    private JButton setDepthButton;

    //--------------------------------------------Constructor-----------------------------------------------------------
    public ControlForm(GuiController argGuiController) {
        formattedTextField2.setValue(6);
        disableAIButton.setEnabled(false);
        hintButton.setEnabled(false);
        this.guiController = argGuiController;
        this.client = GuiController.getClientController();
        setGameState("Not your turn;");
        canvas = new DrawBoard();
        canvas.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int mouseX = e.getX();
                int colSelected = mouseX / CELLSIZE;
                client.tryMove(colSelected, client.getClientName());
            }
        });
        formattedTextField1.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    sendLocalMessage(formattedTextField1.getText());
                    newMessage(formattedTextField1.getText());
                    formattedTextField1.setText("");
                }
            }
        });

        Container cp = panel;
        cp.setLayout(new BorderLayout());
        cp.add(canvas, BorderLayout.CENTER);
        enableAIButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                turnAiOn();
            }
        });
        disableAIButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                turnAiOff();
            }
        });
        hintButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                getHint();
            }
        });

        setDepthButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setDepth();
            }
        });
    }
    //-------------------------------------------Methods----------------------------------------------------------------
    public void turnAiOn() {
        client.setAiOn(true);
        if (client.isMyTurn()) {
            client.getAi().doMove(true);
        }
        enableAIButton.setEnabled(false);
        disableAIButton.setEnabled(true);
    }
    public void setDepth(){
        client.getAi().setLookahead((int)formattedTextField2.getValue());
    }
    public void disableHintButton() {
        hintButton.setEnabled(false);
    }

    public void enableHintButton() {
        hintButton.setEnabled(true);
    }

    public void turnAiOff() {
        client.setAiOn(false);
        enableAIButton.setEnabled(true);
        disableAIButton.setEnabled(false);
    }

    public void getHint() {
        int col = client.getAi().getHint();
        cleanUpChat();
        textArea1.append(String.format("[AI] You could go for: %s %n", col));
    }

    public void sendMessageChatNotEnabeled() {
        textArea1.append(String
                .format("[System] I'm sorry, but Chatting is not enabeled on this server. %n"));
    }

    public void run() {
        frame = new JFrame("ControlForm");
        frame.setContentPane(this.mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public void close() {
        turnAiOff();
        frame.setVisible(false);
        frame.dispose();
        try {
            guiController.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void sendLocalMessage(String m) {
        client.sendLocalMessage(m);
    }

    public void setGameState(String arg) {
        gameState.setText(arg);
    }

    public void repaint() {
        canvas.repaint();
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

    @SuppressWarnings("serial")
    class DrawBoard extends JPanel {
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            setBackground(Color.WHITE);

            // Draw the grid
            g.setColor(Color.LIGHT_GRAY);
            for (int row = 1; row < ROWS; ++row) {
                g.fillRoundRect(0, CELLSIZE * row - (GRID_WIDTH / 2), CANVAS_WIDTH - 1, GRID_WIDTH,
                        GRID_WIDTH, GRID_WIDTH);
            }
            for (int col = 1; col < COLS; ++col) {
                g.fillRoundRect(CELLSIZE * col - (GRID_WIDTH / 2), 0, GRID_WIDTH,
                        CANVAS_HEIGHT - 1, GRID_WIDTH, GRID_WIDTH);
            }

            //Draw the Discs
            Graphics2D g2d = (Graphics2D) g;
            g2d.setStroke(new BasicStroke(SYMBOL_STROKE, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND));
            for (int row = 0; row < ROWS; ++row) {
                for (int col = 0; col < COLS; ++col) {
                    int x1 = col * CELLSIZE + CELL_PAD;
                    int y1 = row * CELLSIZE + CELL_PAD;
                    if (client.getBoard().getSlot(col, row) == Disc.Red) {
                        g2d.setColor(Color.RED);
                        g2d.drawOval(x1, y1, SYMBOLSIZE, SYMBOLSIZE);
                    } else if (client.getBoard().getSlot(col, row) == Disc.Yellow) {
                        g2d.setColor(Color.YELLOW);
                        g2d.drawOval(x1, y1, SYMBOLSIZE, SYMBOLSIZE);
                    }
                }
            }
        }
    }

}
