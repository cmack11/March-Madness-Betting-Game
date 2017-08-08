package model.prizes;

import model.team.TeamInterface;
import model.player.PlayerInterface;
import java.util.LinkedList;
import java.util.List;

public class Contest {

	/**The amount of money that will be given to the players that won the contest*/
	private double prizeMoney;
        private boolean splitPrize;
	/**The player or players that owned the winning team(s) and will be receiving the prize money*/
	private List<PlayerInterface> players;
	/**The team or teams that won the contest*/
	private List<TeamInterface> teams;
	
        
	public Contest(double prizeMoney, boolean splitPrize) {
		this.prizeMoney = prizeMoney;
                this.splitPrize = splitPrize;
		players = new LinkedList<PlayerInterface>();
		teams = new LinkedList<TeamInterface>();
	}
	
	public int instancesOf(PlayerInterface player) {
		int total = 0;
		for(PlayerInterface winner: players)
			if(player.equals(winner))
				total++;
		return total;
	}

	public double getPrizeMoney() {
		return prizeMoney;
	}

	public void setPrizeMoney(double prizeMoney) {
		if(prizeMoney > 0)
			this.prizeMoney = prizeMoney;
	}
        
        public boolean splitPrize() {
            return this.splitPrize;
        }

	public List<PlayerInterface> getPlayers() {
		return players;
	}

	public void setPlayers(List<PlayerInterface> players) {
		if(players != null)
			this.players = players;
	}

	public List<TeamInterface> getTeams() {
		return teams;
	}

	public void setTeams(List<TeamInterface> teams) {
		if(teams != null)
			this.teams = teams;
	}
        
        public String toString() {
            return players +" "+teams;
        }
	
	
	
}

