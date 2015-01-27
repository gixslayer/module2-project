package client.GUI;

import client.ClientController;
import findfour.shared.game.Disc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by joran on 26-1-15.
 */
public class ControlForm {

    public static final int ROWS = 6;
    public static final int COLS = 7;
    public static final int CELL_SIZE = 100;
    public static final int CANVAS_WIDTH = CELL_SIZE * COLS;
    public static final int CANVAS_HEIGHT = CELL_SIZE * ROWS;
    public static final int GRID_WIDTH = 8;
    public static final int GRID_WIDHT_HALF = GRID_WIDTH / 2;

    public static final int CELL_PADDING = CELL_SIZE / 6;
    public static final int SYMBOL_SIZE = CELL_SIZE - CELL_PADDING * 2;
    public static final int SYMBOL_STROKE_WIDTH = 8;
    public final ClientController client;

    private DrawCanvas canvas; // Drawing canvas (JPanel) for the game board
    private JLabel statusBar; // Status Bar


//Form fields
    JFrame frame;
    private JPanel MainPanel;
    private JPanel Panel;
    private JLabel gameState;


    public ControlForm(final ClientController clientController) {
        this.client = clientController;
        setGameState("Not your turn;");
        canvas = new DrawCanvas(); // Construct a drawing canvas (a JPanel)
        canvas.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
        // The canvas (JPanel) fires a MouseEvent upon mouse-click
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // mouse-clicked handler
                int mouseX = e.getX();
                int colSelected = mouseX / CELL_SIZE;
                System.out.println(colSelected);
                client.tryMove(colSelected, client.getClientName());
            }
        });

        Container cp = Panel;
        cp.setLayout(new BorderLayout());
        cp.add(canvas, BorderLayout.CENTER);
    }

    public void run() {
        frame = new JFrame("ControlForm");
        frame.setContentPane(this.MainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
    public void close(String winner){
        setGameState(String.format("%s won the game",winner));
        try {
            wait(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        frame.setVisible(false);
        frame.dispose();
        //TODO join
    }
    public void close(){
        setGameState(String.format("There was a draw"));
        frame.setVisible(false);
        frame.dispose();
        //TODO join
    }
    public void setGameState(String arg){
    gameState.setText(arg);
    }
    public void repaint(){
        canvas.repaint();
    }

    class DrawCanvas extends JPanel {
        @Override
        public void paintComponent(Graphics g) { // invoke via repaint()
            super.paintComponent(g); // fill background
            setBackground(Color.WHITE); // set its background color

            // Draw the grid-lines
            g.setColor(Color.LIGHT_GRAY);
            for (int row = 1; row < ROWS; ++row) {
                g.fillRoundRect(0, CELL_SIZE * row - GRID_WIDHT_HALF, CANVAS_WIDTH - 1, GRID_WIDTH,
                        GRID_WIDTH, GRID_WIDTH);
            }
            for (int col = 1; col < COLS; ++col) {
                g.fillRoundRect(CELL_SIZE * col - GRID_WIDHT_HALF, 0, GRID_WIDTH,
                        CANVAS_HEIGHT - 1, GRID_WIDTH, GRID_WIDTH);
            }

            // Draw the Seeds of all the cells if they are not empty
            // Use Graphics2D which allows us to set the pen's stroke
            Graphics2D g2d = (Graphics2D) g;
            g2d.setStroke(new BasicStroke(SYMBOL_STROKE_WIDTH, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND)); // Graphics2D only
            for (int row = 0; row < ROWS; ++row) {
                for (int col = 0; col < COLS; ++col) {
                    int x1 = col * CELL_SIZE + CELL_PADDING;
                    int y1 = row * CELL_SIZE + CELL_PADDING;
                    if (client.getBoard().getSlot(col, row) == Disc.Red) {
                        g2d.setColor(Color.RED);
                        g2d.drawOval(x1, y1, SYMBOL_SIZE, SYMBOL_SIZE);
                    } else if (client.getBoard().getSlot(col, row) == Disc.Yellow) {
                        g2d.setColor(Color.YELLOW);
                        g2d.drawOval(x1, y1, SYMBOL_SIZE, SYMBOL_SIZE);
                    }
                }
            }
        }
    }

}
