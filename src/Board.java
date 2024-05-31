
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 *
 * @author alu20923266m
 */
public class Board extends JPanel{

    class MyKeyAdapter extends KeyAdapter {
        
        @Override
        public void keyPressed(KeyEvent e) {
            
            switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT: 
                    moveLeft();
                break;
            case KeyEvent.VK_RIGHT: 
                    moveRight();
                break;
            case KeyEvent.VK_UP: 
                    rotateCurrentShape();
                break;
            case KeyEvent.VK_DOWN: 
                    moveDown();
                break;
            default:
                break;
            }
            repaint();
        }
    }

    public static final int NUM_ROWS = 22;
    public static final int NUM_COLS = 10;
    public static final int FIRST_ROW = -2;
    public static final Color COLORS[] = {
            new Color(10, 0, 40), 
            new Color(204, 102, 102), 
            new Color(102, 204, 102), 
            new Color(102, 102, 204), 
            new Color(204, 204, 102), 
            new Color(204, 102, 204), 
            new Color(102, 204, 204), 
            new Color(218, 170, 0)
    };
    
    private Tetrominoes[][] board;
    private Shape currentShape;
    private int currentRow;
    private int currentCol;
    private int deltaTime;
    private Timer timer;
    private MyKeyAdapter keyAdapter;
    private ScoreBoard scoreBoard;
    private boolean move = true;
    private JLabel jLabel;
    private JFrame gameOver;
    private NextShape nextShape;
    private Clip clip;
    private JLabel jLabel2;
    
    public void setGameOver(JFrame jFrame) {
        this.gameOver = jFrame;
    }
    
    public void setFinalScore(JLabel jLabel) {
        this.jLabel2 = jLabel;   
    }
    
    public void setNextShape(NextShape jFrame) {
        this.nextShape = jFrame;
    }
    
    public void setScoreBoard(ScoreBoard scoreBoard) {
        this.scoreBoard = scoreBoard;
    }
    
    public void setPauseDialog(JLabel jLabel) {
        this.jLabel = jLabel;
    }
    
    public void setPause() {
        pauseGame();
    }
    
    public void setRestart() {
        newGame();
    }
    
    private void backGroundMusic() {
         try {
            File yourFile = new File("src/tetris.wav");
            AudioInputStream stream;
            AudioFormat format;
            DataLine.Info info;
           
            stream = AudioSystem.getAudioInputStream(yourFile);
            format = stream.getFormat();
            info = new DataLine.Info(Clip.class, format);
            clip = (Clip) AudioSystem.getLine(info);
            clip.open(stream);
           
            clip.setMicrosecondPosition(0);
            clip.start();
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
        catch (Exception e) {
            System.out.println("Couldn't find file" + e);
        }      
    }
    
    public Board() {
        super();
        board = new Tetrominoes[NUM_ROWS][NUM_COLS];
        for (int row = 0; row<NUM_ROWS; row++) {
            for (int col = 0; col<NUM_COLS; col++) {
                board[row][col] = Tetrominoes.NoShape;
            }
        }
        currentShape = new Shape(); 
        currentRow = FIRST_ROW;
        currentCol = NUM_COLS / 2;
        
        MyKeyAdapter keyAdapter = new MyKeyAdapter();
        addKeyListener(keyAdapter);
        setFocusable(true);
        backGroundMusic();
        
        deltaTime = 500;
        timer = new Timer(deltaTime, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                mainLoop();
            }
        });
        timer.start();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        paintBoard(g2d);
        paintShape(g2d);
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
    
    private void rotateCurrentShape() {
        if(move) {
        Shape rotatedShape = currentShape.rotateRight();
        if (canMove(rotatedShape, currentCol) ) {
            currentShape = rotatedShape;
        }
        }
    }
    
    public int squareWidth() {
        return getWidth() / NUM_COLS;
    }

    public int squareHeight() {
        return getHeight() / NUM_ROWS;
    }

    public void paintShape(Graphics2D g2d) {
        for (int i = 0; i<=3; i++) {
            drawSquare(g2d, currentRow + currentShape.getY(i), 
                            currentCol + currentShape.getX(i),
                            currentShape.getShape());
        }
    }

    public void paintBoard(Graphics2D g2d) {
        for(int row = 0; row<NUM_ROWS; row++) {
            for(int col = 0; col<NUM_COLS; col++) {
                drawSquare(g2d, row, col, board[row][col]);
            }
        }
    }
    
    public void moveLeft() {
        if(move) {
        if (canMove(currentShape, currentCol - 1)) {
            currentCol--;
        }
        }
    }
    
    public void moveRight() {
        if(move) {
        if (canMove(currentShape, currentCol + 1)) {
            currentCol++;
        }
        }
    }
    
    public void moveDown() {
        if (move) {
        if (!collisions(currentRow +1)) {
            currentRow++;
        } else {
            makeCollision();
        }
        repaint();
        }
    }
    
    private boolean collisions(int newRow) {
        if(newRow + currentShape.maxY() >= NUM_ROWS) {
            return true;
        } else {
            for (int i=0; i<=3; i++) {
                int row = newRow + currentShape.getY(i);
                int col = currentCol + currentShape.getX(i);
                if(row >= 0){
                    if (board[row][col] != Tetrominoes.NoShape) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    public void mainLoop() {
        moveDown();
    }
    
    private void makeCollision() {
        if (!movePieceToBoard()) {
            makeGameOver();
        }
        else {        
            currentShape = nextShape.newShape();
            currentRow = FIRST_ROW;
            currentCol = NUM_COLS/2;
        }
        checkLine();
    }
    
    private void makeGameOver()  {
        timer.stop();
        removeKeyListener(keyAdapter);
        gameOver.setVisible(true);
        jLabel2.setText("Total lines: " + scoreBoard.getScore());
    }
    
    private boolean movePieceToBoard() {
            for (int i=0; i< 4; i++) {
                if ((currentRow + currentShape.getY(i)) < 0) {
                    return false;
                }
                board[currentRow + currentShape.getY(i)][currentCol + currentShape.getX(i)] = currentShape.getShape();
            }
            return true;
    }
    
    private void checkLine() {
        
        int counter;
        
        for(int row = 0; row< NUM_ROWS; row++) {
            counter = 0;
            for(int col = 0; col< NUM_COLS; col++) {
                if(board[row][col] != Tetrominoes.NoShape) {
                    counter++;
                }
            }
            if(counter == NUM_COLS) {
                deleteLine(row);
                setNewTime();
            }
        }
        
    }

    public void setNewTime() {      
        timer.setDelay(timer.getDelay()-10);
        timer.start();
    }
    
    private void deleteLine(int rowToDelete) {
        for(int row = rowToDelete; row>1; row--) {
            for(int col = 0; col<NUM_COLS; col++) {
                board[row][col] = board[row-1][col];
            }
        }
        for(int col = 0; col< NUM_COLS; col++) {
            board[0][col] = Tetrominoes.NoShape;
        }
        scoreBoard.incrementScore();
    }
    
    public void newGame() {
        for(int row = 0; row<NUM_ROWS; row++) {
            for(int col = 0; col<NUM_COLS; col++) {
                board[row][col] = Tetrominoes.NoShape;
            }
        timer.stop();
        clip.stop();
        backGroundMusic();
        move = false;
        pauseGame();
        currentRow = FIRST_ROW;
        currentShape = new Shape();
        currentCol = NUM_COLS / 2;
        scoreBoard.restartScore();
        
        timer.setDelay(deltaTime);
        timer.start();
        gameOver.setVisible(false);
        repaint();
        }
    }
    
    private void pauseGame() {
        if(move) {
            clip.stop();
            timer.stop();
            move = false;
            jLabel.setVisible(true);
        }else {
            jLabel.setVisible(false);
            clip.start();
            timer.start();
            move = true;
            
        }
    }
    
    private boolean canMove(Shape shape, int newCol) {
        if (newCol + shape.minX() < 0) {
            return false;
        }
        if (newCol + shape.maxX() > NUM_COLS - 1) {
            return false;
        }
        for (int i=0; i< 4; i++) {
                int row = currentRow + shape.getY(i);
                int col = newCol + shape.getX(i);
                
                if (row >= 0) {
                    if (board[row][col] != Tetrominoes.NoShape) {
                        return false;
                    }
                }
            }
        return true;
    }
    
    
}
    