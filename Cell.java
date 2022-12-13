package minesweeper;


public class Cell 
{
    private boolean mine;

  
    private String content;

    private int surroundingMines;

    

    public Cell()
    {
        mine = false;
        content = "";
        surroundingMines = 0;
    }

    public boolean getMine()
    {
        return mine;
    }

    public void setMine(boolean mine)
    {
        this.mine = mine;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public int getSurroundingMines()
    {
        return surroundingMines;
    }

    public void setSurroundingMines(int surroundingMines)
    {
        this.surroundingMines = surroundingMines;
    }

}
