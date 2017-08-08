/**
 * A an object used in the Bracket class to hold the information of half of a game in the tournament.
 * It holds the team and score that the team achieved during the game. Depending on the score of its
 * matching HalfGame object, one side will be marked the winner and the other will be the loser. This 
 * is signified by the boolean winner. Each object also updates the TeamInterface object's wins and losses
 * 
 * @author claymackenthun
 *
 */
package model.bracket;

import model.team.TeamInterface;

class HalfGame 
{
	private TeamInterface team;
	private int score;
	private boolean winner;
	
	public HalfGame(TeamInterface t) {
		this(t,0);
	}
	
	public HalfGame(TeamInterface team, int score) {
		if(team == null || score < 0)
			throw new IllegalArgumentException();
		this.team = team;
		this.score = score;
		winner = false;
	}
	
	public TeamInterface getTeam() {
		return team;
	}
	
	public int getScore() {
		return score;
	}
	
	public void setScore(int score) {
		if(score < 0)
			throw new IllegalArgumentException();
		this.score = score;
	}
	
	public void setTeam(TeamInterface team) {
		if(team == null)
			throw new IllegalArgumentException();
		this.team = team;
	}
	
	/**
	 * Signifies if the team in this object won the game
	 * 
	 * @return True if winner, False otherwise
	 */
	public boolean is_winner() {
		return winner;
	}
	
	public void setStatus(boolean status) {
		winner = false;
	}
	
	/**
	 * Marks this object the lower and sets the team's status to eliminated
	 */
	public void lost() {
		winner = false;
		team.loses();
	}
	
	/**
	 * Marks this object the winner and updates the team's wins if the team is alive
	 */
	public void won() {
		winner = true;
		if(team.is_alive())
			team.wins();
		else
			team.setWins(team.getWins()+1);
	}
	/**
	 * Future: use the length of the largest team name plus length of seed, spaces, and two digits score
	 */
	public String toString() {
		return "#"+this.team.getSeed()+" "+this.team.getName()+": "+this.score;
	}
}
