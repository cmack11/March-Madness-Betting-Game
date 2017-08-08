package model.player;

import model.team.TeamInterface;
import java.util.List;
import java.util.LinkedList;


public class Player implements PlayerInterface, Comparable<PlayerInterface>
{
	private String name;
	private boolean paid;
	private int predictedScore;
	private double balance;
	private int teamsPurchased;
	private List<TeamInterface> teams;
	private List<TeamInterface> pickFourTeams;
	private TeamInterface chosenWinner;
	
	public Player(String name,int teamsPurchased) {
		this(name,teamsPurchased,null,null,null);
	}
	public Player(String name,int teamsPurchased,List<TeamInterface> teams,
			List<TeamInterface> pickFourTeams, TeamInterface chosenWinner)
	{
		if(name == null || teamsPurchased < 1)
			throw new IllegalArgumentException();
		
		this.name = name.trim();
		this.paid = false;
		this.teamsPurchased = teamsPurchased;
		this.predictedScore = 0;
		
		if(teams == null)
			this.teams  = new LinkedList<TeamInterface>();
		else
			this.teams = teams;
		
		if(pickFourTeams == null)
			this.pickFourTeams = new LinkedList<TeamInterface>();
		else
			this.pickFourTeams = pickFourTeams;
		
		this.chosenWinner = chosenWinner;
	}
	
	public String getName() 
	{
		return name;
	}
	
	public void setName(String name) {
		if(name != null) {
			name = name.trim();
			if(name.length() > 0)
				this.name = name;
		}
	}
	
	public int getTeamsPurchased()
	{
		return teamsPurchased;
	}

	public int getPickFourPoints()
	{
		int total = 0;
		for(TeamInterface t: pickFourTeams)
		{

			total+=(t.getSeed()*t.getWins());
		}
		return total;
	}
	public boolean addPickFourTeam(TeamInterface t)
	{
		if(t == null)
			throw new IllegalArgumentException();
		if(pickFourTeams.size() > 4)
			return false;
		for(TeamInterface team: pickFourTeams)
			if(team.getName().equals(t.getName()))
				return false;
		return pickFourTeams.add(t);
	}
	
	
	public boolean addTeam(TeamInterface t)
	{
		if(t == null)
			throw new IllegalArgumentException();
		if(teams.size() >= teamsPurchased)
			return false;
		for(TeamInterface team: teams)
			if(team.getName().equals(t.getName()))
				return false;
		return teams.add(t);
	}
	
	public List<TeamInterface> getPickFourTeams() 
	{
		return pickFourTeams;
	}
	
	public void setPickFourTeams(List<TeamInterface> pick4teams) {
		if(pick4teams != null)
			this.pickFourTeams = pick4teams;
	}

	public List<TeamInterface> getTeams() 
	{
		return teams;
	}

	public TeamInterface getChosenWinner()
	{
		return chosenWinner;
	}
	
	public String toString()
	{
		return name;
	}
	
	public boolean addChosenTeam(TeamInterface t) 
	{
		//if(chosenWinner != null)
			//return false;
		
		chosenWinner = t;
		return true;
	}
	
	public int getNumSpots() 
	{
		return teamsPurchased;
	}
	
	public void setNumSpots(int numSpots) {
		if(numSpots > 0)
			this.teamsPurchased = numSpots;
	}
	
	public int numTeamsAlive()
	{
		int total = 0;
		for(TeamInterface team: teams)
			if(team.is_alive())
				total++;
		
		return total;
	}
	
	public boolean hasPaid() {
		return paid;
	}
	
	public void setPaid(boolean paid) {
		this.paid = paid;
	}

	public void paid() {
		paid = true;
	}
	
	public int getPredictedScore() {
		return predictedScore;
	}
	
	public void setPredictedScore(int score) {
		if(score >= 0)
			this.predictedScore = score;
	}
	
	public double getBalance() {
		return balance;
	}
	
	public void setBalance(double balance) {
		this.balance = balance;
	}
	
	@Override
	public int compareTo(PlayerInterface o) 
	{
		if(o == null)
			throw new NullPointerException();
		return this.name.compareTo(o.getName());
	}

}
