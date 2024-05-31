
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;


public class NextShape extends JPanel{
    
    private Shape nextShape;
    private Shape currentShape;
    public static final int NUM_ROWS = 4;
    public static final int NUM_COLS = 4;
    public static final Color COLORS[] = Board.COLORS;
    
    public NextShape(){
        currentShape = new Shape();
    }
    
    private void paintShape(Graphics2D g2d) {
        for (int i = 0; i<=3; i++) {
            drawSquare(g2d, NUM_ROWS/3 + currentShape.getY(i), 
                            NUM_COLS/3 + currentShape.getX(i),
                            currentShape.getShape());
        }
    }
    
    public int squareWidth() {
        return getWidth() / NUM_COLS;
    }

    public int squareHeight() {
        return getHeight() / NUM_ROWS;
    }
    
    private void drawSquare(Graphics g, int row, int col, Tetrominoes shape) {
        int x = col * squareWidth();
        int y = row * squareHeight();
        Color color = COLORS[shape.ordinal()];
        g.setColor(color);
        g.fillRect(x + 1, y + 1, squareWidth() - 2, squareHeight() - 2);
        g.setColor(color.brighter());
        g.drawLine(x, y + squareHeight() - 1, x, y);
        g.drawLine(x, y, x + squareWidth() - 1, y);
        g.setColor(color.darker());
        g.drawLine(x + 1, y + squareHeight() - 1, x + squareWidth() - 1, y + squareHeight() - 1);
        g.drawLine(x + squareWidth() - 1, y + squareHeight() - 1, x + squareWidth() - 1, y + 1);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        paintShape(g2d);
    }
    
    public Shape newShape() {
        nextShape = currentShape;
        currentShape = new Shape();
        repaint();
        
        return nextShape;
    }
}
