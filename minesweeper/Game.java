package minesweeper;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.util.ArrayList;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.sql.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;
import javax.swing.border.TitledBorder;
import minesweeper.Score.Time;




public class Game implements MouseListener, ActionListener, WindowListener
{
    public static String dbPath;
    
    private boolean playing; 

    private Board board;

    private UI gui;
    
    private Score score;
        
    

    public Game()
    {
        
        String p = "";

        try 
        {
            p = new File(Game.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath() + "\\db.accdb";
        }
        catch (URISyntaxException ex) 
        {
            System.out.println("Error loading database file.");
        }

        dbPath =  "jdbc:ucanaccess://" + p;

        
        score = new Score();
        score.populate();
        
        UI.setLook("Nimbus");
                        
        createBoard();
        
        this.gui = new UI(board.getRows(), board.getCols(), board.getNumberOfMines());        
        this.gui.setButtonListeners(this);
                        
        this.playing = false;
        
        gui.setVisible(true);
        
        gui.setIcons();        
        gui.hideAll();
        
        resumeGame();
    }

    
    
    public void resumeGame()
    {
        if(board.checkSave())
        {
            ImageIcon question = new ImageIcon(getClass().getResource("/resources/question.png"));      

            int option = JOptionPane.showOptionDialog(null, "Do you want to continue your saved game?", 
                            "Saved Game Found", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, question,null,null);

            switch(option) 
            {
                case JOptionPane.YES_OPTION:      
      
                    
                    Pair p = board.loadSaveGame();
                    
                    
                    setButtonImages();
                    
                    
                    gui.setTimePassed((int)p.getKey());

                    
                    gui.setMines((int)p.getValue());
                    
                    gui.startTimer();
                    
                    playing = true;
                    break;

                case JOptionPane.NO_OPTION:
                    board.deleteSavedGame();
                    break;
                    
                case JOptionPane.CLOSED_OPTION:
                    board.deleteSavedGame();
                    break;
            }
        }
    }


    
    public void setButtonImages()
    {
        Cell cells[][] = board.getCells();
        JButton buttons[][] = gui.getButtons();
        
        for( int y=0 ; y<board.getRows() ; y++ ) 
        {
            for( int x=0 ; x<board.getCols() ; x++ ) 
            {
                buttons[x][y].setIcon(null);
                
                if (cells[x][y].getContent().equals(""))
                {
                    buttons[x][y].setIcon(gui.getIconTile());
                }
                else if (cells[x][y].getContent().equals("F"))
                {
                    buttons[x][y].setIcon(gui.getIconFlag());
                    buttons[x][y].setBackground(Color.blue);	                    
                }
                else if (cells[x][y].getContent().equals("0"))
                {
                    buttons[x][y].setBackground(Color.lightGray);
                }
                else
                {
                    buttons[x][y].setBackground(Color.lightGray);                    
                    buttons[x][y].setText(cells[x][y].getContent());
                    gui.setTextColor(buttons[x][y]);                                        
                }
            }
        }
    }
    
    
    
        
    public void createBoard()
    {
        
        int mines = 10;

        int r = 10;
        int c = 10;
                
        this.board = new Board(mines, r, c);        
    }
    

    
    public void newGame()
    {                
        this.playing = false;        
                                
        createBoard();
        
        gui.interruptTimer();
        gui.resetTimer();        
        gui.initGame();
        gui.setMines(board.getNumberOfMines());
    }
    
    
    public void restartGame()
    {
        this.playing = false;
        
        board.resetBoard();
        
        gui.interruptTimer();
        gui.resetTimer();        
        gui.initGame();
        gui.setMines(board.getNumberOfMines());
    }
        
    
    private void endGame()
    {
        playing = false;
        showAll();

        score.save();
    }

    
    
    
    public void gameWon()
    {
        score.incCurrentStreak();
        score.incCurrentWinningStreak();
        score.incGamesWon();
        score.incGamesPlayed();
        
        gui.interruptTimer();
        endGame();
        
        
        
        JDialog dialog = new JDialog(gui, Dialog.ModalityType.DOCUMENT_MODAL);
        
        
        JLabel message = new JLabel("Congratulations, you won the game!", SwingConstants.CENTER);
                
        
        JPanel statistics = new JPanel();
        statistics.setLayout(new GridLayout(6,1,0,10));
        
        ArrayList<Time> bTimes = score.getBestTimes();
        
        if (bTimes.isEmpty() || (bTimes.get(0).getTimeValue() > gui.getTimePassed()))
        {
            statistics.add(new JLabel("    You have the fastest time for this difficulty level!    "));
        }
        
        score.addTime(gui.getTimePassed(), new Date(System.currentTimeMillis()));
                
        JLabel time = new JLabel("  Time:  " + Integer.toString(gui.getTimePassed()) + " seconds            Date:  " + new Date(System.currentTimeMillis()));
        
        JLabel bestTime = new JLabel();
        
        
        if (bTimes.isEmpty())
        {
            bestTime.setText("  Best Time:  ---                  Date:  ---");
        }
        else
        {
            bestTime.setText("  Best Time:  " + bTimes.get(0).getTimeValue() + " seconds            Date:  " + bTimes.get(0).getDateValue());
        }
        
        JLabel gPlayed = new JLabel("  Games Played:  " + score.getGamesPlayed());
        JLabel gWon = new JLabel("  Games Won:  " + score.getGamesWon());
        JLabel gPercentage = new JLabel("  Win Percentage:  " + score.getWinPercentage() + "%");
        
        statistics.add(time);
        statistics.add(bestTime);
        statistics.add(gPlayed);
        statistics.add(gWon);
        statistics.add(gPercentage);
        
        Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);        
        statistics.setBorder(loweredetched);
        
        
        
        JPanel buttons = new JPanel();
        buttons.setLayout(new GridLayout(1,2,10,0));
        
        JButton exit = new JButton("Exit");
        JButton playAgain = new JButton("Play Again");

        
        exit.addActionListener((ActionEvent e) -> {
            dialog.dispose();
            windowClosing(null);
        });        
        playAgain.addActionListener((ActionEvent e) -> {
            dialog.dispose();            
            newGame();
        });        
        
        
        buttons.add(exit);
        buttons.add(playAgain);
        
        
        
        JPanel c = new JPanel();
        c.setLayout(new BorderLayout(20,20));
        c.add(message, BorderLayout.NORTH);
        c.add(statistics, BorderLayout.CENTER);
        c.add(buttons, BorderLayout.SOUTH);
        
        c.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        dialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                    dialog.dispose();
                    newGame();
            }
            }
        );

        dialog.setTitle("Game Won");
        dialog.add(c);
        dialog.pack();
        dialog.setLocationRelativeTo(gui);
        dialog.setVisible(true);                        
    }
    
    public void gameLost()
    {
        score.decCurrentStreak();
        score.incCurrentLosingStreak();
        score.incGamesPlayed();
        
        gui.interruptTimer();
        
        endGame();
        
        

        JDialog dialog = new JDialog(gui, Dialog.ModalityType.DOCUMENT_MODAL);
        
        
        JLabel message = new JLabel("Sorry, you lost this game. Better luck next time!", SwingConstants.CENTER);
                
        
        JPanel statistics = new JPanel();
        statistics.setLayout(new GridLayout(5,1,0,10));
        
        JLabel time = new JLabel("  Time:  " + Integer.toString(gui.getTimePassed()) + " seconds");
        
        JLabel bestTime = new JLabel();
        
        ArrayList<Time> bTimes = score.getBestTimes();
        
        if (bTimes.isEmpty())
        {
            bestTime.setText("                        ");
        }
        else
        {
            bestTime.setText("  Best Time:  " + bTimes.get(0).getTimeValue() + " seconds            Date:  " + bTimes.get(0).getDateValue());
        }
        
        JLabel gPlayed = new JLabel("  Games Played:  " + score.getGamesPlayed());
        JLabel gWon = new JLabel("  Games Won:  " + score.getGamesWon());
        JLabel gPercentage = new JLabel("  Win Percentage:  " + score.getWinPercentage() + "%");
        
        statistics.add(time);
        statistics.add(bestTime);
        statistics.add(gPlayed);
        statistics.add(gWon);
        statistics.add(gPercentage);
        
        Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);        
        statistics.setBorder(loweredetched);
        
        
        
        JPanel buttons = new JPanel();
        buttons.setLayout(new GridLayout(1,3,2,0));
        
        JButton exit = new JButton("Exit");
        JButton restart = new JButton("Restart");
        JButton playAgain = new JButton("Play Again");

        
        exit.addActionListener((ActionEvent e) -> {
            dialog.dispose();
            windowClosing(null);
        });        
        restart.addActionListener((ActionEvent e) -> {
            dialog.dispose();            
            restartGame();
        });        
        playAgain.addActionListener((ActionEvent e) -> {
            dialog.dispose();            
            newGame();
        });        
        
        
        buttons.add(exit);
        buttons.add(restart);
        buttons.add(playAgain);
        
        
        
        JPanel c = new JPanel();
        c.setLayout(new BorderLayout(20,20));
        c.add(message, BorderLayout.NORTH);
        c.add(statistics, BorderLayout.CENTER);
        c.add(buttons, BorderLayout.SOUTH);
        
        c.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        dialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                    dialog.dispose();
                    newGame();
            }
            }
        );
        
        dialog.setTitle("Game Lost");
        dialog.add(c);
        dialog.pack();
        dialog.setLocationRelativeTo(gui);
        dialog.setVisible(true);        
    }
    
    
    
    public void showScore()
    {
        
                
        JDialog dialog = new JDialog(gui, Dialog.ModalityType.DOCUMENT_MODAL);

        
        
        JPanel bestTimes = new JPanel();
        bestTimes.setLayout(new GridLayout(5,1));
        
        ArrayList<Time> bTimes = score.getBestTimes();
        
        for (int i = 0; i < bTimes.size(); i++)
        {
            JLabel t = new JLabel("  " + bTimes.get(i).getTimeValue() + "           " + bTimes.get(i).getDateValue());            
            bestTimes.add(t);
        }
        
        if (bTimes.isEmpty())
        {
            JLabel t = new JLabel("                               ");            
            bestTimes.add(t);
        }
        
        TitledBorder b = BorderFactory.createTitledBorder("Best Times");
        b.setTitleJustification(TitledBorder.LEFT);

        bestTimes.setBorder(b);
                
        
        JPanel statistics = new JPanel();
        
        statistics.setLayout(new GridLayout(6,1,0,10));        
        
        JLabel gPlayed = new JLabel("  Games Played:  " + score.getGamesPlayed());
        JLabel gWon = new JLabel("  Games Won:  " + score.getGamesWon());
        JLabel gPercentage = new JLabel("  Win Percentage:  " + score.getWinPercentage() + "%");
        JLabel lWin = new JLabel("  Longest Winning Streak:  " + score.getLongestWinningStreak());
        JLabel lLose = new JLabel("  Longest Losing Streak:  " + score.getLongestLosingStreak());
        JLabel currentStreak = new JLabel("  Current Streak:  " + score.getCurrentStreak());

        
        statistics.add(gPlayed);
        statistics.add(gWon);
        statistics.add(gPercentage);
        statistics.add(lWin);
        statistics.add(lLose);
        statistics.add(currentStreak);
                        
        Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);        
        statistics.setBorder(loweredetched);
        
        
        
        JPanel buttons = new JPanel();
        buttons.setLayout(new GridLayout(1,2,10,0));
        
        JButton close = new JButton("Close");
        JButton reset = new JButton("Reset");

        
        close.addActionListener((ActionEvent e) -> {
            dialog.dispose();
        });        
        reset.addActionListener((ActionEvent e) -> {
            ImageIcon question = new ImageIcon(getClass().getResource("/resources/question.png"));      

            int option = JOptionPane.showOptionDialog(null, "Do you want to reset all your statistics to zero?", 
                            "Reset Statistics", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, question,null,null);

            switch(option) 
            {
                case JOptionPane.YES_OPTION:      

                    score.resetScore();
                    score.save();
                    dialog.dispose();
                    showScore();
                    break;

                case JOptionPane.NO_OPTION: 
                    break;
            }
        });        
        
        buttons.add(close);
        buttons.add(reset);
        
        if (score.getGamesPlayed() == 0)
            reset.setEnabled(false);
        
        
        
        JPanel c = new JPanel();
        c.setLayout(new BorderLayout(20,20));
        c.add(bestTimes, BorderLayout.WEST);
        c.add(statistics, BorderLayout.CENTER);        
        c.add(buttons, BorderLayout.SOUTH);
        
        c.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        dialog.setTitle("Minesweeper Statistics - Haris Muneer");
        dialog.add(c);
        dialog.pack();
        dialog.setLocationRelativeTo(gui);
        dialog.setVisible(true);                        
    }
    
    
	
        
    
    private void showAll()
    {
        String cellSolution;
        
        Cell cells[][] = board.getCells();
        JButton buttons[][] = gui.getButtons();

        for (int x=0; x<board.getCols(); x++ ) 
        {
            for (int y=0; y<board.getRows(); y++ ) 
            {
                cellSolution = cells[x][y].getContent();

                
                if( cellSolution.equals("") ) 
                {
                    buttons[x][y].setIcon(null);
                    
                    
                    cellSolution = Integer.toString(cells[x][y].getSurroundingMines());

                    
                    if(cells[x][y].getMine()) 
                    {
                        cellSolution = "M";
                        
                        
                        buttons[x][y].setIcon(gui.getIconMine());
                        buttons[x][y].setBackground(Color.lightGray);                        
                    }
                    else
                    {
                        if(cellSolution.equals("0"))
                        {
                            buttons[x][y].setText("");                           
                            buttons[x][y].setBackground(Color.lightGray);
                        }
                        else
                        {
                            buttons[x][y].setBackground(Color.lightGray);
                            buttons[x][y].setText(cellSolution);
                            gui.setTextColor(buttons[x][y]);
                        }
                    }
                }

                
                else if( cellSolution.equals("F") ) 
                {
                    
                    if(!cells[x][y].getMine()) 
                    {
                        buttons[x][y].setBackground(Color.orange);
                    }
                    else
                        buttons[x][y].setBackground(Color.green);
                }
                
            }
        }
    }
    

    
    
    
    

    

    
    
    
    public boolean isFinished()
    {
        boolean isFinished = true;
        String cellSolution;

        Cell cells[][] = board.getCells();
        
        for( int x = 0 ; x < board.getCols() ; x++ ) 
        {
            for( int y = 0 ; y < board.getRows() ; y++ ) 
            {
                
                cellSolution = Integer.toString(cells[x][y].getSurroundingMines());
                
                if(cells[x][y].getMine()) 
                    cellSolution = "F";

                
                if(!cells[x][y].getContent().equals(cellSolution))
                {
                    
                    isFinished = false;
                    break;
                }
            }
        }

        return isFinished;
    }

 
    
    private void checkGame()
    {		
        if(isFinished()) 
        {            
            gameWon();
        }
    }
   
    
       
    
   
    public void findZeroes(int xCo, int yCo)
    {
        int neighbours;
        
        Cell cells[][] = board.getCells();
        JButton buttons[][] = gui.getButtons();

        
        for(int x = board.makeValidCoordinateX(xCo - 1) ; x <= board.makeValidCoordinateX(xCo + 1) ; x++) 
        {			
            
            for(int y = board.makeValidCoordinateY(yCo - 1) ; y <= board.makeValidCoordinateY(yCo + 1) ; y++) 
            {
                
                if(cells[x][y].getContent().equals("")) 
                {
                    
                    neighbours = cells[x][y].getSurroundingMines();

                    
                    cells[x][y].setContent(Integer.toString(neighbours));

                    if (!cells[x][y].getMine())
                        buttons[x][y].setIcon(null);                        
                    
                    
                    if(neighbours == 0)
                    {                        
                        
                        buttons[x][y].setBackground(Color.lightGray);
                        buttons[x][y].setText("");
                        findZeroes(x, y);
                    }
                    else
                    {
                        
                        buttons[x][y].setBackground(Color.lightGray);
                        buttons[x][y].setText(Integer.toString(neighbours));
                        gui.setTextColor(buttons[x][y]);                        
                    }
                }
            }
        }
    }
    
    
    @Override
    public void windowClosing(WindowEvent e) 
    {
        if (playing)
        {
            ImageIcon question = new ImageIcon(getClass().getResource("/resources/question.png"));      

            Object[] options = {"Save","Don't Save","Cancel"};

            int quit = JOptionPane.showOptionDialog(null, "What do you want to do with the game in progress?", 
                            "New Game", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, question, options, options[2]);

            switch(quit) 
            {
                
                case JOptionPane.YES_OPTION:
                    
                    gui.interruptTimer();
                    score.save();
                    
                    JDialog dialog = new JDialog(gui, Dialog.ModalityType.DOCUMENT_MODAL);
                    JPanel panel = new JPanel();
                    panel.setLayout(new BorderLayout());
                    panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                    panel.add(new JLabel("Saving.... Please Wait", SwingConstants.CENTER));
                    dialog.add(panel);
                    dialog.setTitle("Saving Game...");
                    dialog.pack();
                    dialog.setLocationRelativeTo(gui);                    
                    dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                    
                    SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>(){
                       @Override
                       protected Void doInBackground() throws Exception 
                       {
                            board.saveGame(gui.getTimePassed(), gui.getMines());                
                            return null;
                       }
                       
                       @Override
                       protected void done(){
                           dialog.dispose();                           
                       }                       
                    };
                            
                    worker.execute();
                    dialog.setVisible(true);
                                                            
                    System.exit(0);
                    break;
                
                
                case JOptionPane.NO_OPTION:
                    score.incGamesPlayed();
                    score.save();
                    System.exit(0);
                    break;
                    
                case JOptionPane.CANCEL_OPTION: break;
            }
        }
        else
            System.exit(0);
    }
    
    

    @Override
    public void actionPerformed(ActionEvent e) {        
        JMenuItem menuItem = (JMenuItem) e.getSource();

        if (menuItem.getName().equals("New Game"))
        {
            if (playing)
            {
                ImageIcon question = new ImageIcon(getClass().getResource("/resources/question.png"));      

                Object[] options = {"Quit and Start a New Game","Restart","Keep Playing"};
                
                int startNew = JOptionPane.showOptionDialog(null, "What do you want to do with the game in progress?", 
                                "New Game", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, question, options, options[2]);

                switch(startNew) 
                {
                    case JOptionPane.YES_OPTION:      
                        
                        
                        newGame();
                        score.incGamesPlayed();
                        score.save();
                        break;

                    case JOptionPane.NO_OPTION: 
                        score.incGamesPlayed();   
                        score.save();
                        restartGame();
                        break;
                    
                    case JOptionPane.CANCEL_OPTION: break;
                }
            }
        }
        
        else if (menuItem.getName().equals("Exit"))
        {
            windowClosing(null);
        }
        
        
        else
        {
            showScore();
        }        
    }
    
    
    
        
    
    @Override
    public void mouseClicked(MouseEvent e)
    {
        
        if(!playing)
        {
            gui.startTimer();
            playing = true;
        }
        
        if (playing)
        {
            
            JButton button = (JButton)e.getSource();

            
            String[] co = button.getName().split(",");

            int x = Integer.parseInt(co[0]);
            int y = Integer.parseInt(co[1]);

            
            boolean isMine = board.getCells()[x][y].getMine();
            int neighbours = board.getCells()[x][y].getSurroundingMines();

            
            if (SwingUtilities.isLeftMouseButton(e)) 
            {
                if (!board.getCells()[x][y].getContent().equals("F"))
                {
                    button.setIcon(null);

                    
                    if(isMine) 
                    {  
                        
                        button.setIcon(gui.getIconRedMine());
                        button.setBackground(Color.red);
                        board.getCells()[x][y].setContent("M");

                        gameLost();
                    }
                    else 
                    {
                        
                        board.getCells()[x][y].setContent(Integer.toString(neighbours));
                        button.setText(Integer.toString(neighbours));
                        gui.setTextColor(button);

                        if( neighbours == 0 ) 
                        {
                            
                            button.setBackground(Color.lightGray);
                            button.setText("");
                            findZeroes(x, y);
                        } 
                        else 
                        {
                            button.setBackground(Color.lightGray);
                        }
                    }
                }
            }
            
            else if (SwingUtilities.isRightMouseButton(e)) 
            {
                if(board.getCells()[x][y].getContent().equals("F")) 
                {   
                    board.getCells()[x][y].setContent("");
                    button.setText("");
                    button.setBackground(new Color(0,110,140));

                    

                    button.setIcon(gui.getIconTile());
                    gui.incMines();
                }
                else if (board.getCells()[x][y].getContent().equals("")) 
                {
                    board.getCells()[x][y].setContent("F");
                    button.setBackground(Color.blue);	

                    button.setIcon(gui.getIconFlag());
                    gui.decMines();
                }
            }

            checkGame();
        }
    }

    


    
    
    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }    

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }
}
