package minesweeper;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javafx.util.Pair;


public class Board{
    private int numberOfMines;	
    private Cell cells[][];
    private int rows;
    private int cols;

    public Board(int numberOfMines, int r, int c){
        this.rows = r;
        this.cols = c;
        this.numberOfMines = numberOfMines;

        cells = new Cell[rows][cols];

        createEmptyCells();         

        setMines();

        setSurroundingMinesNumber();
    }
    public void createEmptyCells(){
        for (int x = 0; x < cols; x++){
            for (int y = 0; y < rows; y++){
                cells[x][y] = new Cell();
            }
        }
    }
    public void setMines(){
        int x,y;
        boolean hasMine;
        int currentMines = 0;                
        while (currentMines != numberOfMines){
            x = (int)Math.floor(Math.random() * cols);
            y = (int)Math.floor(Math.random() * rows);
            hasMine = cells[x][y].getMine();
            if(!hasMine){		
                cells[x][y].setMine(true);
                currentMines++;	
            }			
        }
    }
    public void setSurroundingMinesNumber(){	
        for(int x = 0 ; x < cols ; x++){
            for(int y = 0 ; y < rows ; y++){
                cells[x][y].setSurroundingMines(calculateNeighbours(x,y));                        
            }
        }
    }
    public int calculateNeighbours(int xCo, int yCo){
        int neighbours = 0;
        for(int x=makeValidCoordinateX(xCo - 1); x<=makeValidCoordinateX(xCo + 1); x++){
            for(int y=makeValidCoordinateY(yCo - 1); y<=makeValidCoordinateY(yCo + 1); y++){
          
                if(x != xCo || y != yCo)
                    if(cells[x][y].getMine()) 
                        neighbours++;
            }
        }
        return neighbours;
    }
    public int makeValidCoordinateX(int i){
        if (i < 0)
            i = 0;
        else if (i > cols-1)
            i = cols-1;

        return i;
    }	
    public int makeValidCoordinateY(int i){
        if (i < 0)
            i = 0;
        else if (i > rows-1)
            i = rows-1;

        return i;
    }
    public void setNumberOfMines(int numberOfMines){
        this.numberOfMines = numberOfMines;
    }

    public int getNumberOfMines(){
        return numberOfMines;
    }

    public Cell[][] getCells(){
        return cells;
    }
    
    public int getRows(){
        return rows;
    }
    
    public int getCols(){
        return cols;
    }
    public void resetBoard(){
        for(int x = 0 ; x < cols ; x++){
            for(int y = 0 ; y < rows ; y++){
                cells[x][y].setContent("");                        
            }
        }
    }
     public boolean checkSave()
    {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        
        boolean saveExists = false;

        try {
            String dbURL = Game.dbPath; 
            
            connection = DriverManager.getConnection(dbURL); 
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM GAME_STATE");
            
            while(resultSet.next()) 
            {
                saveExists = true;
            }
            
            resultSet.close();
            statement.close();
                       
            
            connection.close();            
            
            return saveExists;
        }
        catch(SQLException sqlex)
        {
            sqlex.printStackTrace();
            return false;
        }        
    }
     public Pair loadSaveGame()
    {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            String dbURL = Game.dbPath; 
            
            connection = DriverManager.getConnection(dbURL); 
            
         
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM CELL");

            for(int x = 0 ; x < cols ; x++) 
            {
                for(int y = 0 ; y < rows ; y++) 
                {                                        
                    resultSet.next();
                    
                    cells[x][y].setContent(resultSet.getString("CONTENT"));
                    cells[x][y].setMine(resultSet.getBoolean("MINE"));
                    cells[x][y].setSurroundingMines(resultSet.getInt("SURROUNDING_MINES"));                    
                }
            }
            
            statement.close();
            resultSet.close();
       
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM GAME_STATE");

            resultSet.next();
                        
            Pair p = new Pair(resultSet.getInt("TIMER"),resultSet.getInt("MINES"));
           
            deleteSavedGame();
          
            resultSet.close();
            statement.close();
          
            connection.close();

            return p;
        }
        catch(SQLException sqlex)
        {
            sqlex.printStackTrace();
            return null;
        }                
    }
     public void deleteSavedGame()
    {
        Connection connection = null;
        PreparedStatement statement = null;
        
        try {
            String dbURL = Game.dbPath; 
            
            connection = DriverManager.getConnection(dbURL); 

            
   
            String template = "DELETE FROM GAME_STATE"; 
            statement = connection.prepareStatement(template);
            statement.executeUpdate();
            
        
            template = "DELETE FROM CELL"; 
            statement = connection.prepareStatement(template);
            statement.executeUpdate();
            
            statement.close();
    
            connection.close();            
        }
        catch(SQLException sqlex)
        {
            sqlex.printStackTrace();
        }                
    }
    
           

    public void saveGame(int timer, int mines)
    {
        Connection connection = null;
        PreparedStatement statement = null;
        
        try {
            String dbURL = Game.dbPath; 
            
            connection = DriverManager.getConnection(dbURL); 

            
            String template = "INSERT INTO CELL (CONTENT, MINE, SURROUNDING_MINES) values (?,?,?)";
            statement = connection.prepareStatement(template);

            for(int x = 0 ; x < cols ; x++) 
            {
                for(int y = 0 ; y < rows ; y++) 
                {
                    statement.setString(1, cells[x][y].getContent());
                    statement.setBoolean(2, cells[x][y].getMine());
                    statement.setInt(3, (int)cells[x][y].getSurroundingMines());                    

                    statement.executeUpdate();
                }
            }
          
            template = "INSERT INTO GAME_STATE (TIMER,MINES) values (?,?)";
            statement = connection.prepareStatement(template);
            
            statement.setInt(1, timer);
            statement.setInt(2, mines);

            statement.executeUpdate();
            
    
            
            statement.close();
          
            connection.close();            
        }
        catch(SQLException sqlex)
        {
            sqlex.printStackTrace();
        }
        
    }
    
    
    
}
