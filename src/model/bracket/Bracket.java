package model.bracket;

import model.team.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A class that represents the a bracket to keep track of the games and progress of teams. 
 * The teams in the bracket must descend from the TeamInterface. The bracket is primarily created
 * to keep the progress of the March Madness Tournament. At the moment it has only been tested
 * and works with 4 Regions and 16 Teams Per Region
 * 
 * @author Clay Mackenthun
 * 
 *	ADD: List that keeps track of all of the teams to cut down on time used to return a list of teams
 */
public class Bracket 
{
	private int numRegions;//Must be even number
	private int teamsPerRegion;//Must be power of two
	private int beginIndex;//Beginning of subarray of all teams
	private int endIndex;//End of subarray of all teams
	private HalfGame[] bracket;
	private List<TeamInterface> teams;
	private Region[] regions;
	
	/**
	 * Creates a new bracket. Represents a bracket full of teams that advances teams based on
	 * score differences. A score of zero is considered a score that hasn't been entered. Does not
	 * accept negative scores.
	 * 
	 * @param numRegions The number of equally divided sections of the bracket. Must be an even number
	 * @param teamsPerRegion Currently only supports 16 teams per region. Should work with any power of two in the future
	 * 
	 * @see TeamInterface
	 * @see Team
	 */
	public Bracket(int numRegions, int teamsPerRegion)
	{
		if(numRegions % 2 != 0 || !((teamsPerRegion & (teamsPerRegion-1)) == 0))
			throw new IllegalArgumentException();
		
		this.numRegions = numRegions;
		this.teamsPerRegion = teamsPerRegion;
		
		//One is added so the integer division creates the bracket effect
		bracket = new HalfGame[arrayLength(numRegions*teamsPerRegion)+1];
		
		//List of each team with no duplicates
		teams = new ArrayList<TeamInterface>();
		
		//The indexes of the subarray for all teams (aka the first round of games)
		this.beginIndex = bracket.length-(numRegions*teamsPerRegion);
		this.endIndex = bracket.length-1;
		
		regions = Region.values();
	}

	/**
	 * Helper method that returns the size of the bracket array by
	 * taking the initial number of teams and dividing in half and adding
	 * to the total to simulate the amount of spaces that will be needed.
	 * 
	 * @param number the teams in each hypothetical "round"
	 * 
	 * @return size the number of teams for each round including champion
	 */
	private int arrayLength(int number) 
	{
		if(number == 1)
			return 1;
		return number+arrayLength(number/2);
	}
	
	/**
	 * Adds a team to the bracket. Seed must be between 1 and the number of teams 
	 * in the region (inclusive). If there is already a team in it's designated place
	 * the method will not add this team. The edit team method allows a team to replace another.
	 * 
	 * @param team The team to be entered. Cannot be null or contain a out-of-range seed value
	 * 
	 * @return errorCode 
	 * 	0: Added successfully.
	 * 	1: Team already exists in this location.
	 * 	2: Team with this name already exists in the bracket
	 * 
	 * @throws IllegalArgumentException if seed value is out of range
	 */
	public int addTeam(TeamInterface team)
	{
		if(team.getSeed() > teamsPerRegion || team.getSeed() < 1) {
                    throw new IllegalArgumentException();
                }
		
		if(findTeam(team.getName()) != null)//There is already a team with this name
			return 2;
		
		if(bracket[hashCode(team)] == null)
		{
			bracket[hashCode(team)] = new HalfGame(team);
			teams.add(team);
			return 0;
		}
		return 1;
	}
	
	/**
	 * Adds a score to the team with a matching name. The score will be added to the farthest
	 * matchup for the team. If there are no games remaining the score will not be added. The score
	 * will also be added to the list of scores contained in the TeamInterface object.
	 * 
	 * @param teamName The string that will be used to search for the team receiving the score
	 * @param score The integer that will be added to the team. Negative numbers will cause an exception. 0 will do nothing
	 * 
	 * @return errorCode 0: Added successfully; 1: Team has already lost; 2: TeamInterface object is not in bracket
	 * 
	 * @throws IllegalArgumentException if team cannot be found or scores is negative
	 */
	public int addScore(String teamName, int score)
	{
		if(teamName == null || teamName.trim().equals("") || score < 0)
			throw new IllegalArgumentException();
		return addScore(findTeam(teamName),score);
		
	}
	
	/**
	 * Adds a score to the team passed. The score will be added to the farthest
	 * matchup for the team. If there are no games remaining the score will not be added. The score
	 * will also be added to the list of scores contained in the TeamInterface object.
	 * 
	 * @param team The team that the score should be added to
	 * @param score The score that will be added to the team. Will return error code if TeamInterface object is not in bracket.
	 * 
	 * @return errorCode 0: Added successfully; 1: Team has already lost; 2: TeamInterface object is not in bracket
	 * 
	 * @throws IllegalArgumentException if team is null or scores is negative
	 */
	public int addScore(TeamInterface team, int score)
	{
		if(team == null || score < 0)
			throw new IllegalArgumentException();
		
		if(!team.is_alive())
			return 1;//Team has already lost
		
		int startingIndex = hashCode(team);
		if(bracket[startingIndex].getTeam() != team)
			return 2;//team Object passed is not in bracket
		
		//Calculates the starting position of the team. Then finds its farthest position
		int firstIndex = getCurrentIndex(startingIndex);
		
		if(bracket[firstIndex] == null)
			bracket[firstIndex] = new HalfGame(team);//If this position is empty (used when initializing bracket)
		
		bracket[firstIndex].setScore(score);
		
		finishGame(firstIndex);
		return 0;
	}
	
	/**
	 * Removes the provided team from the bracket if found. All scores associated
	 * with the team will also be removed.
	 * 
	 * @param team The team that should be removed
	 * @return The team removed if found, else null
	 */
	public TeamInterface removeTeam1(TeamInterface team) {
		if(team == null || !bracket[hashCode(team)].equals(team))
			return null;
		
		TeamInterface temp = team;
		int index = hashCode(team);
		while(temp != null && temp.getName().equals(team.getName())) {
			bracket[index] = null;
			index /= 2;
			if(bracket[index] != null)
				temp = bracket[index].getTeam();
		}
		return team;
	}
	
	/**
	 * Private method that completes a game if both scores have been entered 
	 * between two teams that are playing. Will not advance a winner if one of
	 * the scores is 0 or opponent doesn't exist)
	 * 
	 * @param teamIndex Index of team in matchup
	 */
	private void finishGame(int teamIndex)
	{
            if(teamIndex == 1)
                    return;
            int opponentIndex = getOpponent(teamIndex);
            HalfGame team = bracket[teamIndex];
            HalfGame opponent = bracket[opponentIndex];
            if(team.getScore() == 0 || (opponent == null || opponent.getScore() == 0))
                    return;
            if(team.getScore() > opponent.getScore()) { //Team won
                    team.won();
                    opponent.lost();

                    int nextIndex = teamIndex/2;
                    if(bracket[nextIndex] == null || !bracket[nextIndex].getTeam().equals(team.getTeam())) {
                            bracket[nextIndex] = new HalfGame(team.getTeam());
                    }
            } else if(opponent.getScore() > team.getScore()) { //Opponent won
                    opponent.won();
                    team.lost();

                    int nextIndex = opponentIndex/2;
                    if(bracket[nextIndex] == null || !bracket[nextIndex].getTeam().equals(opponent.getTeam())) {
                            bracket[nextIndex] = new HalfGame(opponent.getTeam());
                    }
            } else											//Tie
                    bracket[opponentIndex/2] = null;
	}
	
	/**
	 * Returns true if all of the first round teams are filled. False if any of
	 * the spaces are empty. A check to see if all teams have been entered and tournament is ready to start.
	 * 
	 * @return initialized True if all teams are set, False otherwise
	 */
	public boolean initialized()
	{
		for(int i = beginIndex;i < bracket.length; i++)
			if(bracket[i] == null)
				return false;
			return true;
	}
        
        public boolean hasBegun() {
            for(int i = beginIndex;i < bracket.length; i++) {
		if(bracket[i] == null)
                    return false;
                else if(bracket[i].getScore() != 0)
                    return true;
            }
            return false;
            
        }
	
	/**
	 * Given two teams it will return an number representing how many games team1
	 * must win before it will play team2. Order matters if one of the teams has played more than the other.
	 * 
	 * Bug: If teams are not found error will happen
	 * @param team1 The team that the method will calculate for. (Games before team1 plays team2)
	 * @param team2 The number returned will be how long until team1 plays this team
	 * 
	 * @return numGames The number of games for team1 before team1 and team2 play each other. Returns -1 if one of the teams has been eliminated
	 * 
	 * @throws IllegalArgumentException if either parameter is null or either object is not in the bracket.
	 */
	public int gamesBeforeMatchup(TeamInterface team1, TeamInterface team2)
	{
		if(team1 == null || team2 == null)
			throw new IllegalArgumentException();
		
		int index1 = getCurrentIndex(hashCode(team1));
		int index2 = getCurrentIndex(hashCode(team2));

			if(index1 < 0 || index2 < 0 ||
					bracket[index1] == null || bracket[index2] == null ||
					!bracket[index1].getTeam().equals(team1) || 
					!bracket[index2].getTeam().equals(team2))
			throw new IllegalArgumentException();
		
		
		//One of the teams has been eliminated
		if(!bracket[index1].getTeam().is_alive() || !bracket[index2].getTeam().is_alive())
			return -1;
		
		
		return gamesBeforeMatchup(index1, index2);
	}
	
	/**
	 * Recursively calculates how many games before the teams at each index play each other.
	 * Order matters if games are not in same round and are calculated relative to index1.
	 * 
	 * Idea: Change from recursive to a list based that would calculate all future possiblities of team 2 then see how
	 * long until team 1 would get there? Might be slower. Could also change call so both would be on same round regardless
	 * 
	 * @param index1
	 * @param index2
	 * 
	 * @return numGames Number of games before the indexes are one away from each other and are a matchup in the bracket
	 */
	private int gamesBeforeMatchup(int index1, int index2)
	{
		if(index1 < 0 || index2 < 0 || index1 >bracket.length-1 || index2 > bracket.length-1)
			return -1;
		List<Integer> rounds1 = new LinkedList<Integer>();
		List<Integer> rounds2 = new LinkedList<Integer>();
		
		while(index2 > 0 || index1 > 0) {
			if(index2 > 0)
				rounds2.add(index2);
			if(index1 > 0)
				rounds1.add(index1);
			index2 /= 2;
			index1 /= 2;
		}
		Iterator<Integer> itr1 = rounds1.iterator();
		int count = 0;
		while(itr1.hasNext()) {
			Iterator<Integer> itr2 = rounds2.iterator();
			int currIndex1 = itr1.next();
			while(itr2.hasNext()) {
				int currIndex2 = itr2.next();
				if(currIndex2 == currIndex1) {
					return count-1;
				}
			}
			count++;
		}
		return -1;
		
	}
	
	/**
	 * Returns a List of the scores of the team in order of oldest to newest
	 * 
	 * @param team	The team the scores are coming from
	 * 
	 * @return scores A List of scores in order of oldest score to newest score
	 */
	public List<Integer> getScores(TeamInterface team) {
		return getScores(team,this.numRegions*this.teamsPerRegion);
	}
	
	/**
	 * Returns a list of the scores of the team passed. The list size will not exceed the
	 * limit passed, but it may be less if there are less scores entered than the limit passed. 
	 * Throws an IllegalArgumentException if the limit is negative.
	 * 
	 * @param team The team the scores are coming from
	 * @param limit The maximum size of the list of scores
	 * @return scores A List of scores in order oldest to newest
	 */
	public List<Integer> getScores(TeamInterface team, int limit) {
		if(team == null || 
			!bracket[hashCode(team)].getTeam().equals(team) ||
			 limit < 0)
			throw new IllegalArgumentException();
		
		List<Integer> scores = new ArrayList<Integer>();
		int index = hashCode(team);
		HalfGame curr = bracket[index];

		while(limit != 0 &&//The limit passed has not been reached
				index != 1 &&//It's not the champion position (score isn't logical)
				curr != null && //This position actually exists
				curr.getTeam().equals(team) && //We are still viewing scores for the team passed
				curr.getScore() > 0) { //negative (invalid)

			scores.add(bracket[index].getScore());
			index /= 2;
			curr = bracket[index];
			limit--;
		}
		return scores;
	}
	
	/**
	 * 
	 * @param seed
	 * @param region
	 * @return
	 */
	public TeamInterface getTeam(int seed, Region region)
	{
		int index = hashCode(new Team("temp", seed, region));
		if(index < 0 || index >= bracket.length || bracket[index] != null)
			return bracket[index].getTeam();
		return null;
	}
	
	/**
	 * 
	 * @param team
	 * @return
	 */
	public TeamInterface removeTeam(TeamInterface team)
	{
		if(team == null)
			throw new IllegalArgumentException();
		
		int index = teamIndex(team);
		if(index < 0)
			return null;
		
		removeProgress(team);
		
		bracket[index] = null;
		teams.remove(team);
		
		return team;
	}
	
	/**
	 * A method used to edit the scores of a particular team. All of the progress will be reset and the scores
	 * will be re-added one by one. Once the first 0 is found, all scores after will be disregarded. The scores passed
	 * should contain every score of the team from first round to current round (in that order).
	 * 
	 * @param team The team that will have its scores edited.
	 * @param scores All scores of the team from first round to current round
	 * 
	 * @throws NullPointerException Thrown if team or scores are null
	 * 
	 * Bugs: What if first round hasn't been initialized??
	 * Future: Change to remove scores and only have an edit method in the database
	 */
	public void editScores(TeamInterface team, List<Integer> scores)
	{
		if(team == null || scores == null)
			throw new IllegalArgumentException();
		int index = this.hashCode(team);
                if(index < 1 || index > bracket.length || 
                        !bracket[index].getTeam().equals(team))
                    throw new IllegalArgumentException("Misplaced teams");
		//Puts the team back to the beginning of the tournament, resets its wins and status, and "revives" the opponents it faced
		removeProgress(team);
		//Goes through each score and adds it to the team using the addScore() method
		Iterator<Integer> itr = scores.iterator();
                boolean clearOut = false;
		while(bracket[index] != null && index > 0) {
                    HalfGame opp = bracket[getOpponent(index)];
                    if(opp != null && index != 1) {
                        opp.setStatus(false);
                        opp.getTeam().setStatus(true);
                    }
                    if(bracket[index].getTeam().equals(team)) {
                        bracket[index].setStatus(true);
                        team.setStatus(true);
                    }
                    if(itr.hasNext()) {
                        int score = itr.next();
                        bracket[index].setScore(score);
                    }
                    else if(bracket[index].getTeam().equals(team)) {
                        bracket[index].setScore(0);
                    }
                    if(!clearOut)
                        this.finishGame(index);
                    //System.out.println(bracket[index]+" "+bracket[index].getTeam().is_alive()+"\n"+opp+" "+opp.getTeam().is_alive());
                    if(clearOut) {
                        TeamInterface clearedTeam = bracket[index].getTeam();
                        if(clearedTeam != null)
                            clearedTeam.setWins(clearedTeam.getWins()-1);
                        if(team != null)
                            
                        bracket[index] = null;
                    }
                    if(!clearOut)
                        clearOut = bracket[index].getTeam().equals(team) && 
                                bracket[index].getScore() == 0;
                    index /= 2;
		}
	}
	
	private void removeProgress(TeamInterface team)
	{
		int index = hashCode(team);
		if(index < 0)
			return;
		//Reset team's status
		team.setWins(0);
		team.setStatus(true);

		if(bracket[index] == null)
			return;
		
		//Resets teams results  in first round game of bracket
		bracket[index].setScore(0);
		bracket[index].setStatus(false);
		
		//"Revives" the opponent in first round so score can be calculated
		int opponentIndex = getOpponent(index);
		if(bracket[opponentIndex] != null) {
			if(bracket[opponentIndex].is_winner()) {
				TeamInterface opponent = bracket[opponentIndex].getTeam();
				opponent.setWins(opponent.getWins()-1);
			}
			bracket[opponentIndex].getTeam().setStatus(true);
			bracket[opponentIndex].setStatus(false);
		}
		
		//Goes through each game that this team is in and sets it to null and "revives" the opponent 
		index /= 2;
		HalfGame curr = bracket[index];
		while(curr != null && curr.getTeam().equals(team) && index > 0) {
                    opponentIndex = getOpponent(index);
                    if(bracket[opponentIndex] != null) {
                        TeamInterface opponent = bracket[opponentIndex].getTeam();
                        if(opponent != null) {
                            if(bracket[opponentIndex].is_winner()) //Opponent won matchup
                                    opponent.setWins(opponent.getWins()-1);

                            bracket[opponentIndex].setStatus(false);//Game is not over so no winner
                            opponent.setStatus(true);//"revive" opponent so score can be calculated
                        }

                    }
                    //Erase the team from this game and then move to next round
                    bracket[index] = null;
                    index /= 2;
                    curr = bracket[index];
		}		
	}
	
	/**
	 * Calculates how many wins a team could possibly still attain for the team
	 * that matches the string passed
	 * 
	 * @param name The name of the team that the wins will be calculated for
	 * 
	 * @return numWins The number of wins the team could still have. -1 will be returned if the team is null or not found in the bracket
	 */
	public int possibleWins(String name)
	{
		return possibleWins(teamIndex(name));
	}
	
	/**
	 * Calculates how many wins a team could possibly still attain
	 * 
	 * @param team The team that the wins will be calculated for
	 * 
	 * @return numWins The number of wins the team could still have. -1 will be returned if the team is null or not found in the bracket
	 */
	public int possibleWins(TeamInterface team)
	{
		if(team == null)
			return -1;
		
		int index = getCurrentGame(hashCode(team));
		if(index < 0 || index >= bracket.length)
			return -1;
		
		return possibleWins(index);
	}
	
	/**
	 * Helper method that recursively calculates the wins a team could possibly 
	 * attain
	 * 
	 * @param index Current position in bracket
	 * 
	 * @return numWins The running total of the number of wins
	 */
	private int possibleWins(int index) {
		if(bracket[index] != null && !bracket[index].getTeam().is_alive())
			return 0;
		if(index == 1)
			return 0;
		
		return 1 + possibleWins(index/2);
	}
	
	/**
	 * Returns the index of the team that matches the parameter string.
	 * Returns -1 if not found
	 * 
	 * @param name The name of the team that should be searched for
	 * 
	 * @return index Index of the team's first round position
	 */
	private int teamIndex(String name) {
		for(int i = beginIndex; i < bracket.length; i++) {
			if(!(bracket[i] == null) && bracket[i].getTeam().getName().equals(name)) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Returns the index of the team that is passed. Only returns the index if the team is in
	 * the place assigned to it by the hashCode method. 
	 * 
	 * @param team The team whose index we are concerned about 
	 * 
	 * @return teamIndex The index of the team passed
	 */
	private int teamIndex(TeamInterface team) {
		int teamIndex = hashCode(team);
		if(bracket[teamIndex].getTeam().equals(team))
			return teamIndex;
		return teamIndex(team.getName());
	}
	
	/**
	 * Calculates where the team's first round index will be. Use's the teams seed and region to assign
	 * the team to it's starting position
	 * Future: Need to make more general. Only works with region size 16
	 * 
	 * @param team The team that will have its index calculated
	 * 
	 * @return index The index of where this team should be placed in the bracket
	 */
	private int hashCode(TeamInterface team) {
		int seed = team.getSeed();
		int regionNumber = 0;
		for(int i = 0; i < regions.length; i++) {
			if(regions[i].equals(team.getRegion())) {
				regionNumber = i;
			}
		}
		int ordering;
		switch(seed)
		{
		case 1: ordering = 0;
				break;
		case 16: ordering = 1;
				break;
		case 8: ordering = 2;
				break;
		case 9: ordering = 3;
				break;
		case 5: ordering = 4;
				break;
		case 12: ordering = 5;
				break;
		case 4: ordering = 6;
				break;
		case 13: ordering = 7;
				break;
		case 6: ordering = 8;
				break;
		case 11: ordering = 9;
				break;
		case 3: ordering = 10;
				break;
		case 14: ordering = 11;
				break;
		case 7: ordering = 12;
				break;
		case 10: ordering = 13;
				break;
		case 2: ordering = 14;
				break;
		case 15: ordering = 15;
				break;
		default: ordering = -1;
		}
		
		return beginIndex + (regionNumber*teamsPerRegion)+ordering;
	}
	
	/**
	 * Takes the index of a team and finds its farthest position in the bracket.
	 * If the team is alive it will return the index of the team's current position.
	 * If the team was eliminated it will return the farthest position the team reached
	 * 
	 * @param index The starting index of the team
	 * 
	 * @return index The farthest index of the team. -1 if the index is out of bounds 
	 */
	//GOES TO THE FURHTEST INSTANCE OF THE TEAM (DOESN'T CARE IF GAME HAS BEEN COMPLETED
	private int getCurrentIndex(int index)
	{	
		if(index < 0 || index >= bracket.length)//Index is out of bounds
			return -1;

		if(bracket[index] == null || bracket[index].getScore() == 0)//No score has been recorded for this team
			return index;
		
		if(bracket[getOpponent(index)] != null && //There is an opponent
				bracket[getOpponent(index)].getScore() != 0 && //The opponent's score has been entered
				!bracket[index].is_winner())//And the opponent beat this team
			return index;
		
		return getCurrentIndex(index/2);
			
	}
	
	//DOES NOT MOVE PAST GAME IF GAME HASN'T BEEN DECIDED
	private int getCurrentGame(int index)
	{
		if(index < 0 || index >= bracket.length)//Index is out of bounds
			return -1;
		
		if(index == 1)
			return 1;

		if(bracket[index/2] == null || !bracket[index/2].getTeam().equals(bracket[index].getTeam()))
			return index;
			
		
		return getCurrentGame(index/2);
	}
	
	/**
	 * Takes the given index and finds the opponent of that team according to 
	 * their place in the bracket
	 * 
	 * @param firstIndex The index of the team whose opponent's index will be returned
	 * 
	 * @return opponentIndex index of the opponent of the team found at the index passed
	 */
	private int getOpponent(int firstIndex) {
		if(firstIndex == 1)
			return 1;
		if(firstIndex/2 == (firstIndex+1)/2)
			return firstIndex+1;
		else
			return firstIndex-1;
	}
	
	/**
	 * Returns a list of all of the teams in the bracket
	 * Future: organize list by seeds and/or regions
	 * 
	 * @return teamList A List of all of the teams in the bracket
	 */
	public List<TeamInterface> getTeams() {
		return teams;
	}
	
	public Region[] getRegions() {
		return this.regions;
	}
	
	public void setRegions(Region[] regions) {
		this.regions = regions;
	}
	
	public List<TeamInterface> getOpponents(TeamInterface team)
	{
		if(team == null)
			return null;
		
		int index = teamIndex(team);
		if(index < 0)
			return null;
		
		List<TeamInterface> opponents = new LinkedList<TeamInterface>();
		HalfGame curr = bracket[index];
		while(curr != null && curr.getTeam().equals(team) && index > 1) {
			try {
				TeamInterface opponent = bracket[getOpponent(index)].getTeam();
				if(opponent != null)
					opponents.add(opponent);
			} catch(NullPointerException e){}
			
			index /= 2;
			curr = bracket[index];
		}
		return opponents;
	}
	
	public TeamInterface getChampion()
	{
		HalfGame champion = bracket[1];
		if(champion == null || champion.getTeam() == null)
			return null;
		return champion.getTeam();
	}
	
	/**
	 * Searches the bracket for the team specified by the string name
	 * Returns null if not found. The search is not case-sensitive
	 * 
	 * @param name The bracket will return a team object that whose name matches the name
	 * 
	 * @return TeamInterface The team object whose name matches the parameter. Null if no match is found
	 */
	public TeamInterface findTeam(String name)
	{
		if(name == null || name.length() < 1)
			return null;
		name = name.trim().toLowerCase();
		for(int i = beginIndex; i < endIndex+1; i++)
		{
			if(bracket[i] != null)
			{
				String lowerName = bracket[i].getTeam().getName().toLowerCase();
				if(lowerName.equals(name))
					return bracket[i].getTeam();
			}
		}
		return null;
	}
	
	/**
	 * Returns a string with the style:
	 * 
	 * #SEED TEAMNAME: SCORE	#SEED TEAMNAME: SCORE
	 * #SEED TEAMNAME: SCORE	#SEED TEAMNAME: SCORE
	 * ...
	 * #SEED TEAMNAME: SCORE	#SEED TEAMNAME: SCORE
	 * 		(Blank line to indicate a new round of games)
	 * #SEED TEAMNAME: SCORE	#SEED TEAMNAME: SCORE
	 * #SEED TEAMNAME: SCORE	TBD (if team has not yet been determined or entered)
	 * 
	 */
	public String toString()
	{
		int endRound = beginIndex;
		int roundTotal = this.teamsPerRegion*this.numRegions;
		String bar = "===============================================================";
		String string = "";
		String line = "";
		boolean emptyRound = true;
		for(int i = endIndex; i > 1; i-=2) {
			
			if(bracket[i] != null) {
				line += String.format("%3s %-20s %-5d",
						"#"+bracket[i].getTeam().getSeed(),bracket[i].getTeam().getName()+": ",bracket[i].getScore());
				emptyRound = false;
			} else
				line += String.format("%-30s","TBD");
			
			if(bracket[i-1] != null) {
				line += String.format("%3s %-20s %-5d%n",
						"#"+bracket[i-1].getTeam().getSeed(),bracket[i-1].getTeam().getName()+": ",bracket[i-1].getScore());
				emptyRound = false;
			} else
				line += String.format("%-30s%n","TBD");

			if(endRound == i-1) {
				if(emptyRound)
					break;
				string += String.format("%s%n%s%n%s%n",bar,"Round of "+roundTotal+":",bar)+line;
				emptyRound = true;
				endRound /= 2;
				roundTotal /= 2;
				line = "";
			}
		}
		if(bracket[1] != null)
			string += String.format("Champion: %3s %-27s%n%s%n",
					"#"+bracket[1].getTeam().getSeed(),bracket[1].getTeam().getName(),bar);
		
		return string;
	}
}
