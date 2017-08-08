package model.player;

import model.team.TeamInterface;
import java.util.List;

public interface PlayerInterface extends Comparable<PlayerInterface>
{
	public String getName();
	
	public void setName(String name);
	
	public List<TeamInterface> getPickFourTeams();
	
	public List<TeamInterface> getTeams();
	
	public TeamInterface getChosenWinner();
	
	public boolean addTeam(TeamInterface t);
	
	public boolean addPickFourTeam(TeamInterface t);

	public int getPickFourPoints();

	public boolean addChosenTeam(TeamInterface t);
	
	public void setPickFourTeams(List<TeamInterface> pick4teams);

	public int getNumSpots();
	
	public void setNumSpots(int numSpots);
	
	public int numTeamsAlive();
	
	public boolean hasPaid();

	public void setPaid(boolean paid);
	
	public void paid();
	
	public void setPredictedScore(int score);
	
	public int getPredictedScore();
	
	public double getBalance();
	
	public void setBalance(double balance);
}
