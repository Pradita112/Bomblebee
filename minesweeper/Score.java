package minesweeper;

import static java.lang.Math.ceil;
import java.sql.Connection;
import java.util.ArrayList;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Comparator;


public class Score{
    ArrayList<Time> bestTimes;
    
    int gamesPlayed;
    int gamesWon;
       
    int longestWinningStreak;
    int longestLosingStreak;
    
    int currentStreak;

    int currentWinningStreak;
    int currentLosingStreak;
    
    public Score(){
        gamesPlayed = gamesWon = currentStreak = longestLosingStreak = longestWinningStreak = currentWinningStreak = currentLosingStreak = 0;
        bestTimes = new ArrayList();
    }
      
    public int getGamesPlayed(){
        return gamesPlayed;        
    }
    
    public int getGamesWon(){        
        return gamesWon;
    }
    public int getWinPercentage(){
        double gP = gamesPlayed;
        double gW = gamesWon;
        double percentage = ceil((gW/gP) * 100);
        return (int)percentage;
    }
    public int getLongestWinningStreak(){
        return longestWinningStreak;
    }
    
    public int getLongestLosingStreak()
    {
        return longestLosingStreak;
    }
    
    public int getCurrentStreak()
    {
        return currentStreak;
    }
    
    public int getCurrentLosingStreak()
    {
        return currentLosingStreak;
    }

    public int getCurrentWinningStreak(){
        return currentWinningStreak;
    }
    
    public void incGamesWon()
    {
        gamesWon++;
    }
    
    public void incGamesPlayed()
    {
        gamesPlayed++;
    }
    
    public void incCurrentStreak()
    {
        currentStreak++;
    }
    
    public void incCurrentLosingStreak()
    {
        currentLosingStreak++;
        
        if (longestLosingStreak < currentLosingStreak)
        {
            longestLosingStreak = currentLosingStreak;
        }                
    }

    public void incCurrentWinningStreak()
    {
        currentWinningStreak++;
        
        if (longestWinningStreak < currentWinningStreak)
        {
            longestWinningStreak = currentWinningStreak;
        }                
    }
    
    
    public void decCurrentStreak()
    {        
        currentStreak--;
    }    
    
    
    public void resetScore()
    {
        gamesPlayed = gamesWon = currentStreak = longestLosingStreak = longestWinningStreak = currentWinningStreak = currentLosingStreak = 0;
    }
    
    
    
    public ArrayList<Time> getBestTimes()
    {
        return bestTimes;
    }
        
    
    public void addTime(int time, Date date)
    {
        bestTimes.add(new Time(time,date));
        Collections.sort(bestTimes,new TimeComparator()); 
        
        if(bestTimes.size() > 5)
            bestTimes.remove(bestTimes.size()-1);
    }
    public class TimeComparator implements Comparator<Time>
    {
        @Override
        public int compare(Time a, Time b) {
            if (a.getTimeValue() > b.getTimeValue())
                return 1;
            else if (a.getTimeValue() < b.getTimeValue())
                return -1;
            else
                return 0;
        }                        
    }
    public class Time{
        Date date;
        int time;
        
        public Time(int t, Date d)
        {
            time = t;
            date = d;
        }
        
        public Date getDateValue()
        {
            return date;
        }
        
        public int getTimeValue()
        {
            return time;
        }        
    }    
}
