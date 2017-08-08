package model;

import model.player.*;
import model.team.*;
import model.prizes.*;
import model.bracket.Bracket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * The controlling class of the entire March Madness data. It keeps track of the
 * teams through the Bracket class and also keeps a list of all of the players.
 * The database calculates all of the scores and prizes for the various games in
 * the March Madness Board.
 *
 * Future: List some of games and prizes and rules
 *
 * @author claymackenthun
 *
 * @see Bracket.java
 * @see java.util.List
 *
 */
public class MarchMadnessDB {

    private Bracket bracket;
    private ContestManager manager;
    private List<PlayerInterface> players;
    private final int STARTING_SPOTS;
    
    private final int NUM_REGIONS = 4;
    private final int TEAMS_PER_REGION = 16;
    private TeamInterface firstOTloser;
    
    private List<Double> pricing;
    
    private Contest blowoutLoss;
    private Contest cinderellaTeam;
    private Contest firstOvertimeLoss;
    private Contest pickFourWinner;
    private Contest pickFourRunnerUp;
    private Contest chosenWinner;
    private Contest winFirstGame;
    private Contest finalFourTeam;
    private Contest championTeam;
    
    private int leaderboardSize = 1;
    
	//private int moneyPool;// ToDo
    /**
     * Creates a new database with the given cost per team. This signifies the
     * price to purchase a single team
     *
     */
    public MarchMadnessDB() {
        bracket = new Bracket(NUM_REGIONS, TEAMS_PER_REGION);
        players = new LinkedList<PlayerInterface>();
        STARTING_SPOTS = NUM_REGIONS * TEAMS_PER_REGION;

        // Defaults
        pricing = new LinkedList<Double>();
        pricing.add(0.0);//0 teams costs 0 dollars
        pricing.add(10.0);
        pricing.add(15.0);
        pricing.add(20.0);
        pricing.add(25.0);
        pricing.add(30.0);

        manager = new ContestManager(bracket);
        
    }

    /**
     * Converts information in the string and adds it to a team in the database
     * String style must be: SEED, TEAM NAME, REGION (each separated by commas)
     *
     * @param line A String with the style <SEED>, <TEAM>, <REGION>
     *
     * @return errorCode 0: If added successfully. 1: Team already exists in
     * location. 2: A Team with this name already exists in the bracket 3:
     * Illegal information entered.
     */
    public int addTeam(String line) {
        if (line != null && line.length() > 0) {
            line = line.trim();
            String name = "";
            String region;
            int seed;
            
            String[] pieces = line.split(","); // Separate each word

            try {
                seed = Integer.parseInt(pieces[0]);
            } catch (NumberFormatException e) {
                return 4;
            } // Turn first word to seed number

            region = pieces[pieces.length - 1].trim(); // Last word should be
            // region

            for (int i = 1; i < pieces.length - 1; i++) // Take middle pieces
            // and turn into name
            {
                name += pieces[i];
            }
            name = name.trim();
            if (name.equals("")) {
                return 3;
            }
            return addTeam(name, seed, region); // Send off to be added
        }
        return 3;
    }

    /**
     * Adds a team to the database if the information is valid. The name must
     * not match any other team already in the database, the seed must be in
     * between 1 and 16 (inclusive), and the region must be Midwest, West, East,
     * or South.
     *
     * @param name The name of the team to be added
     * @param seed Number between 1 and 16 (inclusive)
     * @param region Midwest, West, East, or South
     *
     * @return errorCode 0: If added successfully. 1: Team already exists in
     * location. 2: A Team with this name already exists in the bracket 3:
     * Illegal information entered.
     */
    public int addTeam(String name, int seed, String region) {
        List<Integer> scores = new LinkedList<Integer>();
        return addTeam(name, seed, region, scores);
    }

    /**
     * Add a new team to the bracket object. The region must be midwest, east,
     * south, or west. Seed must be between 1 & 16 (inclusive). Name must not be
     * null, empty, or matching the name of another team in the database.
     *
     * @param name The name of the team to be added
     * @param seed Number between 1 and 16 (inclusive)
     * @param regionstr Midwest, West, East, or South
     *
     * @return errorCode 0: If added successfully. 1: Team already exists in
     * location. 2: A Team with this name already exists in the bracket 3:
     * Illegal information entered.
     */
    public int addTeam(String name, int seed, String regionstr, List<Integer> scores) {
        Region region = Region.determineRegion(regionstr);
        return addTeam(name, seed, region, scores);
    }

    /**
     * Add a new team to the bracket object. The region must be midwest, east,
     * south, or west. Seed must be between 1 & 16 (inclusive). Name must not be
     * null, empty, or matching the name of another team in the database.
     *
     * @param name The name of the team to be added
     * @param seed Number between 1 and 16 (inclusive)
     * @param region Midwest, West, East, or South (non-NULL)
     *
     * @return errorCode 0: If added successfully. 1: Team already exists in
     * location. 2: A Team with this name already exists in the bracket 3:
     * Illegal information entered.
     */
    public int addTeam(String name, int seed, Region region) {
        return addTeam(name, seed, region, new LinkedList<Integer>());
    }
    
    private int addTeam(String name, int seed, Region region, List<Integer> scores) {
        if (seed < 1 || seed > 16) {
            return 3;
        }
        if (region == null) {
            return 2;
        }
        TeamInterface team = new Team(name.trim(), seed, region);
        int returnCode = bracket.addTeam(team);
        if (returnCode == 0) {// Add was successful, so add scores
            Iterator<Integer> itr = scores.iterator();
            while (itr.hasNext()) {
                this.addScore(team, itr.next());
            }
        }
        return returnCode;
    }

    /**
     *
     * @param team
     * @return
     */
    public int addTeam(TeamInterface team) {
        return bracket.addTeam(team);
    }

    /**
     * Adds a specified team to a specified player's collection of owned teams.
     * The amount of teams owned for a player is specified by the amount of
     * spots they purchased. No two players should own the same team. (SHOULD
     * THIS METHOD CHECK TO SEE IF THAT RULE IS UPHELD??)
     *
     * @throws IllegalArgumentExcption Thrown if Player or Team are not found.
     *
     * @param playerName The name of the player that the team will be added to
     * @param teamName The name of the team that will be added to the player
     *
     * @return boolean True if team was added successfully, False if team does
     * not have anymore spaces available
     */
    public boolean addOwnedTeam(String playerName, String teamName) {
        PlayerInterface player = findPlayer(playerName);
        TeamInterface team = findTeam(teamName);
        
        return addOwnedTeam(player, team);
    }
    
    public boolean addOwnedTeam(PlayerInterface player, TeamInterface team) {
        if (player == null || team == null) {
            throw new IllegalArgumentException();
        }
        return player.addTeam(team);
    }

    /**
     * Adds 1-4 Team(s) to a specified player. The method determines the teams
     * and player if the String passed is formatted correctly. The format should
     * be <Player>, <Team>, <Team>... The amount of teams must be between 1-4
     * (inclusive) and each item should be separated by a comma With no commas
     * before or after line passed
     *
     * @param line The string containing the player and team(s) with the format
     * <Player>, <Team>, <Team>...
     *
     * @return errorCode 0 (if Team(s) were added successfully.) 1 (if no commas
     * were present.) 2 (if too many fields were entered.) 3 (if Player is not
     * found.) 4 (if a team is not found.) 5 (if the team will have more than
     * four teams if these teams were added)
     */
    public int addPickFourTeam(String line)// Line: <Player>, <Team>, <Team>...
    // (as many teams as entered)
    {
        if (!line.contains(",")) {
            return 1;
        }
        String[] pieces = line.trim().split(",");
        if (pieces.length > 5) {
            return 2;
        }
        PlayerInterface player = findPlayer(pieces[0]);
        if (player == null) {
            return 3;
        }
        List<TeamInterface> temp = new LinkedList<TeamInterface>();
        for (int i = 1; i < pieces.length; i++) {
            TeamInterface team = findTeam(pieces[i]);
            if (team == null) {
                return 4;
            }
            temp.add(team);
        }
        Iterator<TeamInterface> itr = temp.iterator();
        if (player.getPickFourTeams().size() + temp.size() > 4) {
            return 5;
        }
        while (itr.hasNext()) {
            player.addPickFourTeam(itr.next());
        }
        return 0;
    }

    /**
     * Adds a Team to a specified player's Pick Four Teams. A player may not
     * have more than 4 Teams and can not have duplicates. Two different players
     * are permitted to have the same team in their Pick Four Collection. True
     * will be returned if the team was added. False if the player does not have
     * any more openings in its Pick Four Teams or a duplicate is trying to be
     * added. An exception is thrown if either the Player or the Team could not
     * be found.
     *
     * @throws IllegalArgumentException Thrown if either the Player or the Team
     * could not be found.
     *
     * @param playerName The name of the player receiving the Pick Four Team
     * @param teamName The name of the team that will be added to the Player's
     * Pick Four Team
     *
     * @return True if added successfully, False if Player's Pick Four
     * collection is full or a duplicate is attempting to be added
     */
    public boolean addPickFourTeam(String playerName, String teamName) {
        PlayerInterface player = findPlayer(playerName);
        TeamInterface team = findTeam(teamName);
        
        return addPickFourTeam(player, team);
    }
    
    public boolean addPickFourTeam(PlayerInterface player, TeamInterface team) {
        if (player == null || team == null) {
            throw new IllegalArgumentException();
        }
        
        return player.addPickFourTeam(team);
    }
    
    public int addPlayer(String line)// Line: <Player> <SpotsPurchased>
    {
        if (line == null || line.trim().length() < 1) {
            return 1;
        }
        String[] pieces = line.split(" ");
        String player = "";
        for (int i = 0; i < pieces.length - 1; i++) {
            player += pieces[i] + " ";
        }
        player = player.trim();
        int spotsPurchased;
        try {
            spotsPurchased = Integer.parseInt(pieces[pieces.length - 1]);
        } catch (NumberFormatException e) {
            return 2;
        }
        return addPlayer(player, spotsPurchased);
    }

    /**
     * Adds a given player to the database if there are enough spots remaining
     * Will not add player if there is another player with a the same name.
     * returns true if player was successfully added, else returns false
     *
     * @param String name
     * @param int spotsPurchased
     * @return boolean
     */
    public int addPlayer(String name, int spotsPurchased) {
        if (name == null || name.trim().length() == 0 || spotsPurchased < 1) {
            throw new IllegalArgumentException();
        }
        if (findPlayer(name) != null) {
            return 5;
        }
        int spotsRemaining = this.spotsRemaining();
        if (spotsRemaining - spotsPurchased >= 0) {
            if (spotsPurchased >= pricing.size()) {
                return 4;
            }
            name = name.trim();
            players.add(new Player(name, spotsPurchased));
            return 0;
        }
        return 3;
    }
    
    public int addScore(String line)// Line: <Team> <Score>
    {
        if (line == null || line.trim().length() < 1) {
            return 1;
        }
        String[] pieces = line.split(" ");
        String team = "";
        for (int i = 0; i < pieces.length - 1; i++) {
            team += pieces[i] + " ";
        }
        team = team.trim();
        int score;
        try {
            score = Integer.parseInt(pieces[pieces.length - 1]);
        } catch (NumberFormatException e) {
            return 2;
        }
        return addScore(team, score);
    }

    /**
     * Adds score with the given teamName and score. Bracket object handles the
     * verifying and adding it to the most current game for the team
     *
     * @param teamName
     * @param score
     * @return
     */
    public int addScore(String teamName, int score) {
        TeamInterface team = findTeam(teamName);
        return addScore(team, score);
    }
    
    public int addScore(TeamInterface team, int score) {
        if (team == null || score < 0) {
            return 3;
        }
        int returnCode = bracket.addScore(team, score);
        return returnCode;
    }
    
    public List<Integer> getScores(TeamInterface team) {
        if (team == null || bracket.findTeam(team.getName()) == null) {
            throw new IllegalArgumentException();
        }
        
        return bracket.getScores(team);
    }
    
    public int addChosenWinner(String line)// <Player> , <Team>
    {
        if (!line.contains(",")) {
            return 1;
        }
        String[] pieces = line.split(",");
        if (pieces.length != 2) {
            return 2;
        }
        
        PlayerInterface p = findPlayer(pieces[0]);
        TeamInterface t = findTeam(pieces[1]);
        
        return addChosenWinner(p, t);
    }

    // Don't add team if there is already one? Make them use the edit command?
    public int addChosenWinner(PlayerInterface p, TeamInterface t) {
        if (p == null || t == null) {
            return 3;
        }
        if (p.addChosenTeam(t)) {
            return 0;
        }
        return 4;
    }
    
    public void moveTeam(TeamInterface team, int seed, Region region) {
        if (team == null || seed < 1 || seed > 16 || region == null) {
            throw new IllegalArgumentException();
        }

        // Remove the team at the seed and region if there is one.
        TeamInterface teamToDelete = bracket.getTeam(seed, region);
        if (teamToDelete != null) {
            bracket.removeTeam(teamToDelete);
        }

        // Get the team's scores
        List<Integer> scores = bracket.getScores(team);

        // Remove the team from the old bracket place
        bracket.removeTeam(team);

        // Create a new team with the new information and add it and its score
        TeamInterface newTeam = new Team(team.getName(), seed, region);
        bracket.addTeam(newTeam);
        for (int score : scores) {
            bracket.addScore(newTeam, score);
        }
    }

    /**
     * @param team
     * @param scores
     */
    public void editScores(TeamInterface team, List<Integer> scores) {
        Iterator<Integer> itr = scores.iterator();
        while (itr.hasNext()) {
            int score = itr.next();
            if (score == 0) {
                itr.remove();
                while (itr.hasNext()) {
                    itr.next();
                    itr.remove();
                }
            }
        }
        bracket.editScores(team, scores);
    }

    /**
     * Needs to check and see if teams have been assigned
     *
     * @param name
     * @return
     */
    public PlayerInterface removePlayer(String name) {
        if (name == null) {
            throw new NullPointerException();
        }
        name = name.trim();
        if (name.length() < 1) {
            return null;
        }
        
        Iterator<PlayerInterface> itr = players.iterator();
        int i = 0;
        while (itr.hasNext()) {
            PlayerInterface player = itr.next();
            if (itr.next().getName().equalsIgnoreCase(name)) {
                PlayerInterface p = players.remove(i);
                return p;
            }
            i++;
        }
        return null;
    }

    /**
     *
     * @param name
     * @return
     */
    public TeamInterface removeTeam(String name) {
        if (name == null) {
            throw new IllegalArgumentException();
        }
        
        name = name.trim();
        if (name.length() < 1) {
            throw new IllegalArgumentException();
        }
        
        TeamInterface team = findTeam(name);
        return removeTeam(team);
    }
    
    public TeamInterface removeTeam(TeamInterface team) {
        if (team == null) {
            return null;
        }
		// Also remove this team from any player lists. (Chosen winner, pick
        // four, owned team)
        TeamInterface removed = bracket.removeTeam(team);
        if (removed == null) {
            return null;
        }
        
        for (PlayerInterface player : players) {
            if (player.getChosenWinner() != null && player.getChosenWinner().equals(removed)) {
                player.addChosenTeam(null);
            }
            
            player.getPickFourTeams().remove(removed);
            
            player.getTeams().remove(removed);
        }
        return removed;
    }
    
    public String getPlayerInfo(PlayerInterface p) {
        if (p == null) {
            return "";
        }
        String menuBar = "======================================================"
                + "==========================================";
        String info = "";
        /**
         * Prints out one bar, player's name, ad balance
         */
        info += String.format("%n%s%n"
                + "Player: %s | $% .2f | Tiebreaker Score: %d | ", menuBar, p.toString(), p.getBalance(), p.getPredictedScore());

        /**
         * Determines if player has chosen winner, prints, adds another bar"
         */
        String chosenWinner;
        if (p.getChosenWinner() != null) {
            if (p.getChosenWinner().is_alive()) {
                chosenWinner = p.getChosenWinner().toString() + " (Alive)";
            } else {
                chosenWinner = p.getChosenWinner().toString() + " (Eliminated)";
            }
        } else {
            chosenWinner = "None Selected";
        }
        info += String.format("Chosen Winner: %s%n%s%n", chosenWinner, menuBar);

        /**
         * Pick-four points and potential points remaining (or none if bracket
         * isn't ready
         */
        String points = ((this.bracketInitialized())
                ? ("(" + p.getPickFourPoints() + "/" + (this.getPotentialPickFourPoints(p) + p.getPickFourPoints()) + ")") : "(0/0)");

        /**
         * Prints a title and then the respective teams in two team rows
         */
        String[] titles = {"Owned Teams:", "Pick-Four: " + points};
        @SuppressWarnings("unchecked")
        Iterator<TeamInterface>[] itrs = (Iterator<TeamInterface>[]) new Iterator[2];
        itrs[0] = p.getTeams().iterator();
        itrs[1] = p.getPickFourTeams().iterator();

        for (int i = 0; i < titles.length && i < itrs.length; i++) {
            info += String.format("%s%n   ", titles[i]);
            int count = 0;
            Iterator<TeamInterface> itr = itrs[i];
            while (itr.hasNext()) {
                TeamInterface t = itr.next();
                String status = ((t.is_alive()) ? " (Alive)" : " (Eliminated)");
                info += String.format("%-20s%-15s", t, status);
                count++;
                if (itr.hasNext()) {
                    info += String.format((count % 2 == 0) ? "%n   " : " | ");
                }
            }
            info += String.format("%n");
        }

        /**
         * Final bar to close out section of information
         */
        info += String.format("%s%n", menuBar);
        return info;
    }
    
    public String getPlayerInfo(PlayerInterface p,int remove) {
        if (p == null) {
            return null;
        }
        String playerInfo = "Name: " + p.getName() + "\n";
        playerInfo += "Balance: $" + p.getBalance() + "\n";
        if (p.getChosenWinner() != null) {
            playerInfo += "Chosen Winner: " + p.getChosenWinner() + "\n";
        } else {
            playerInfo += "Chosen Winner: None Selected\n";
        }
        playerInfo += "Teams Owned:\n";
        for (TeamInterface t : p.getTeams()) {
            playerInfo += "\t" + t.getName();
            if (t.is_alive()) {
                playerInfo += " (Alive)\n";
            } else {
                playerInfo += " (Eliminated)\n";
            }
        }
        playerInfo += "Pick Four Teams: ";
        if (bracket.initialized()) {
            int points = p.getPickFourPoints();
            playerInfo += "(" + points + "/"
                    + (getPotentialPickFourPoints(p)+points + " pts)\n");
        }
        for (TeamInterface t : p.getPickFourTeams()) {
            playerInfo += "   " + t.getName();
            if (t.is_alive()) {
                playerInfo += " (Alive)";
            } else {
                playerInfo += " (Eliminated)";
            }
        }
        
        return playerInfo;
    }
    
    public String getTeamInfo(TeamInterface t) {
        if (t == null) {
            return null;
        }
        String teamInfo = "";
        teamInfo += "Name: " + t.getName() + "\n";
        teamInfo += "Games:\n";
        Iterator<TeamInterface> opItr = getOpponents(t).iterator();
        Iterator<Integer> scoreItr = getScores(t).iterator();
        for (int round = 1; scoreItr.hasNext() && opItr.hasNext(); round++) {
            TeamInterface opponent = opItr.next();
            teamInfo += "  -" + t.toString() + ": " + scoreItr.next() + " vs " + opponent.toString() + ": "
                    + bracket.getScores(opponent, round).get(round - 1) + "\n";
        }
        teamInfo += "Alive/Eliminated: ";
        if (t.is_alive()) {
            teamInfo += "Alive\n";
        } else {
            teamInfo += "Eliminated\n";
        }
        teamInfo += "Seed: " + t.getSeed() + "\n";
        teamInfo += "Region: " + t.getRegion() + "\n";
        teamInfo += "Wins: " + t.getWins() + "\n";
        teamInfo += "Potential Games Remaining: " + bracket.possibleWins(t);
        return teamInfo;
    }

    /**
     * Returns how many pick four points a specified player has. If player isn't
     * found an Exception will be thrown
     *
     * @throws IllegalArgumentException
     * @param name
     * @return
     */
    public int getPickFourPoints(String name) {
        PlayerInterface p = findPlayer(name);
        if (p != null) {
            return p.getPickFourPoints();
        }
        throw new IllegalArgumentException();
    }

    /**
     * Calculates the points that certain player could potentially still earn in
     * the pick four contest if the players exists in the database. Throws
     * exception if player isn't found, has no Pick Four Teams, and or player
     * doesn't have four teams.
     *
     * @throws IllegalArgumentException
     * @param name
     * @return int total
     */
    public int getPotentialPickFourPoints(PlayerInterface p) {
        
        if (p == null) {
            throw new IllegalArgumentException();
        }
        
        List<TeamInterface> teams = new ArrayList<TeamInterface>(p.getPickFourTeams());
        if (teams == null || teams.isEmpty()) {
            return 0;
        }
        
        int totalPts = 0;//Keeps track of the total points the player could still recieve 
        /**
         * Bubble sort by highest seed and removes teams that aren't alive
         */
        for (int i = 0; i < teams.size(); i++) {
            TeamInterface highSeed = teams.get(i);
            int highIndex = i;
            for (int j = i + 1; j < teams.size() && highSeed.is_alive(); j++) {
                TeamInterface temp = teams.get(j);
                if (temp.getSeed() > highSeed.getSeed() && temp.is_alive()) {
                    highSeed = temp;
                    highIndex = j;
                }
            }
            TeamInterface removed = teams.remove(highIndex);
            /**
             * If the next team to be moved is alive it will be the highest (or
             * equivalent) seed compared to remaining
             */
            if (highSeed.is_alive()) {
                teams.add(i, removed);
                TeamInterface currTeam = teams.get(i);
                /**
                 * Total wins if current team wins it all
                 */
                int currMax = bracket.possibleWins(currTeam);
                /**
                 * Compares next team to higher (or equivalent) teams that are
                 * already sorted
                 */
                for (int j = 0; j < i; j++) {
                    TeamInterface tempTeam = teams.get(j);
                    /**
                     * The the most wins possible if all previous (higher/same
                     * seeded) teams win as far as they can
                     */
                    if (tempTeam.is_alive()) {
                        int gamesB4Matchup;
                        try {
                            gamesB4Matchup = bracket.gamesBeforeMatchup(currTeam, tempTeam);
                        } catch (Exception e) {
                            gamesB4Matchup = 0;
                        }
                        
                        if (gamesB4Matchup < 0) {
                            gamesB4Matchup = 0;
                        }
                        
                        currMax = Math.min(currMax, currTeam.getWins() + gamesB4Matchup);
                        //System.out.println("\t"+gamesB4Matchup+" games until "+currTeam+" plays "+tempTeam+". current Max:"+(currMax-currTeam.getWins())+" ");
                    }
                }
                totalPts += currTeam.getSeed() * currMax;
            } else { //Team was not alive so it was removed and all teams shift lift one index, so offset increment
                i--;
            }
        }
        
        return totalPts;
        
    }
    
    public List<PlayerInterface> getPrizeWinner(PrizeType prizeType) {
        return manager.getWinningPlayers(prizeType);
    }
    
    public List<TeamInterface> getPrizeWinnerTeam(PrizeType prizeType) {
        return manager.getWinningTeams(prizeType);
    }
    
    public double getPrizeMoney(PrizeType prizeType) {
        return manager.getPrizeMoney(prizeType);
    }
    
    public void setPrizeMoney(PrizeType prizeType, double prizeMoney) {
        manager.setPrizeMoney(prizeType, prizeMoney);
    }
    
    public void update() {
        manager.update(pricing,players);
    }

    /**
     * Returns a list of the players (in descending order) with the highest pick
     * four points. List size depends on the leaderboardSize variable in the
     * MarchMadnessDB class
     *
     * FUTURE: if no games have happened sort the list be potential points
     *
     * @return List<PlayerInterface>
     */
    public List<PlayerInterface> getPickFourLeaderBoard() {
        return this.getPickFourLeaderBoard(this.leaderboardSize);
    }

    /**
     * Needs to be written cleaner and faster
     *
     */
    public List<PlayerInterface> getPickFourLeaderBoard(int leaderboardSize) {
        List<PlayerInterface> leaderboard = new ArrayList<PlayerInterface>();
        
        if (players.isEmpty()) {
            return leaderboard;
        }
        
        Iterator<PlayerInterface> itr = players.iterator();
        while (itr.hasNext()) {
            PlayerInterface player = itr.next();
            
            for (int i = 0; i < leaderboard.size(); i++) {
                PlayerInterface temp = leaderboard.get(i);
                if (player.getPickFourPoints() > temp.getPickFourPoints()) {
                    leaderboard.add(i, player);
                    break;
                } else if (player.getPickFourPoints() == temp.getPickFourPoints()
                        && this.getPotentialPickFourPoints(player) > this.getPotentialPickFourPoints(temp)) {
                    leaderboard.add(i, player);
                    break;
                    
                } else if (i == leaderboard.size() - 1 && (leaderboardSize == -1 || leaderboard.size() <= leaderboardSize)) {
                    leaderboard.add(player);
                    break;
                }
            }
            if (leaderboard.isEmpty()) {
                leaderboard.add(player);
            }
            while (leaderboardSize != -1 && leaderboard.size() > leaderboardSize) {
                leaderboard.remove(leaderboard.size() - 1);
            }
        }
        return leaderboard;
    }
    
    public List<PlayerInterface> getPlayerLeaderboard() {
        List<PlayerInterface> leaderboard = this.getPlayers();
        PlayerInterface[] array = new PlayerInterface[leaderboard.size()];
        leaderboard.toArray(array);
        
        int j; // the number of items sorted so far
        PlayerInterface key; // the item to be inserted
        int i;
        
        for (j = 1; j < array.length; j++) // Start with 1 (not 0)
        {
            key = array[j];
            for (i = j - 1; (i >= 0) && (array[i].numTeamsAlive() < key.numTeamsAlive()); i--) // Smaller
            // values
            // are
            // moving
            // up
            {
                array[i + 1] = array[i];
            }
            array[i + 1] = key; // Put the key in its proper location
        }
        leaderboard = new LinkedList<PlayerInterface>();
        for (int index = 0; index < array.length && (index < this.leaderboardSize || leaderboardSize == -1); index++) {
            leaderboard.add(array[index]);
        }
        
        return leaderboard;
    }
    
    public List<TeamInterface> getOpponents(TeamInterface team) {
        return bracket.getOpponents(team);
    }
    
    public int assignTeams() {
        if (!bracket.initialized() || (this.spotsRemaining() != 0)) {
            return 1;
        }

        // Get rid of them if there are any
        clearAssignedTeams();
        
        Random generator = new Random();
        List<TeamInterface> teams = new LinkedList<TeamInterface>(bracket.getTeams());
        Iterator<PlayerInterface> itr = players.iterator();
        while (itr.hasNext() && teams.size() > 0) {
            PlayerInterface player = itr.next();
            for (int i = 0; i < player.getNumSpots() && teams.size() > 0; i++) {
                int index = generator.nextInt(teams.size());
                player.addTeam(teams.remove(index));
            }
        }
        return 0;
    }
    
    private void clearAssignedTeams() {
        Iterator<PlayerInterface> itr = players.iterator();
        while (itr.hasNext()) {
            itr.next().getTeams().clear();
        }
    }

    /**
     * Sets the team that lost the first overtime game
     *
     * @param loser Team that lost the first overtime game
     */
    public void setOTloser(TeamInterface loser) {
         manager.setOTLoserTeam(loser);
    }

    /**
     * Returns the team that was the first to lose in overtime
     *
     * @return Team that lost first overtime game
     */
    public TeamInterface getOTloser() {
        return manager.getOTLoserTeam();
    }
    
    public List<TeamInterface> getFinalFour() {
        List<TeamInterface> finalFour = new LinkedList<>();
        Iterator<TeamInterface> itr = this.getTeams().iterator();
        while (itr.hasNext()) {
            TeamInterface temp = itr.next();
            System.out.println(temp.getWins());
            if (temp.getWins() >= 4) {
                finalFour.add(temp);
            }
        }
        
        return finalFour;
    }
    
    public Region[] getRegionMatchups() {
        return bracket.getRegions();
    }
    
    public void setRegionMatchups(Region[] newRegionMatchups) {
        Region[] regionMatchups = bracket.getRegions();
        //Edit Region Matchups
        if (newRegionMatchups[0] != null && newRegionMatchups[1] != null && !newRegionMatchups[0].equals(newRegionMatchups[1])) {
            int index = 2;//Region's in spot 3 and 4 need to be determined
            for (int i = 0; i < regionMatchups.length; i++) {
                boolean found = false;
                for (int j = 0; j < newRegionMatchups.length; j++) {
                    if (newRegionMatchups[j] != null && newRegionMatchups[j].equals(regionMatchups[i])) {
                        found = true;
                    }
                }
                if (!found) {
                    newRegionMatchups[index] = regionMatchups[i];
                    index++;
                }
            }
            List<TeamInterface> teams = getTeams();
            List<TeamInterface> teamsCopy = new ArrayList<TeamInterface>(teams.size());
            List<List<Integer>> scoreLists = new LinkedList<List<Integer>>();
            
            List<Integer> zeroes = new LinkedList<>();
            zeroes.add(0);
            for (TeamInterface team : teams) {
                scoreLists.add(getScores(team));
                bracket.editScores(team, zeroes);
                
            }
            
            while (!teams.isEmpty()) {
                teamsCopy.add(bracket.removeTeam(teams.get(0)));
            }

            //db = new MarchMadnessDB();
            bracket.setRegions(newRegionMatchups);
            
            Iterator<TeamInterface> teamItr = teamsCopy.iterator();
            Iterator<List<Integer>> scoreListItr = scoreLists.iterator();
            
            while (teamItr.hasNext() && scoreListItr.hasNext()) {
                TeamInterface team = teamItr.next();
                List<Integer> scores = scoreListItr.next();
                addTeam(team);
                for (int score : scores) {
                    this.addScore(team, score);
                }
            }
        }
        
    }

    /**
     * Finds a team given the specified name. Returns null if the team was not
     * found.
     *
     * @param String name
     * @return Team that matches name, null otherwise
     */
    public TeamInterface findTeam(String name) {
        if (name == null) {
            throw new IllegalArgumentException();
        }
        
        name = name.trim().toLowerCase();
        Iterator<TeamInterface> itr = bracket.getTeams().iterator();
        while (itr.hasNext()) {
            TeamInterface team = itr.next();
            if (team.getName().toLowerCase().equals(name)) {
                return team;
            }
        }
        return null;
    }

    /**
     * Finds a person given the specified name. Returns null if the person was
     * not found.
     *
     * @param String name
     * @return PlayerInterface
     */
    public PlayerInterface findPlayer(String name) {
        if (name == null) {
            throw new IllegalArgumentException();
        }
        name = name.trim().toLowerCase();
        Iterator<PlayerInterface> itr = players.iterator();
        while (itr.hasNext()) {
            PlayerInterface player = itr.next();
            if (player != null && player.getName().toLowerCase().equals(name)) {
                return player;
            }
        }
        return null;
    }
    
    public int findLongestPlayerName() {
        int longest = 0;
        for(PlayerInterface player: players) {
            int temp = player.getName().length();
            if(temp > longest)
                longest = temp;
        }
        return longest;
    }
    
    public int spotsRemaining() {
        int remaining = STARTING_SPOTS;
        for (PlayerInterface player : players) {
            remaining -= player.getNumSpots();
        }
        return remaining;
    }
    
    public TeamInterface getChampionTeam() {
        return bracket.getChampion();
    }
    
    public boolean bracketInitialized() {
        return bracket.initialized();
    }
    
    public boolean tournamentHasBegun() {
        return bracket.hasBegun();
    }
    
    public List<PlayerInterface> getPlayers() {
        Collections.sort(players);
        return players;
    }
    
    public boolean teamsAssigned() {
        if (this.spotsRemaining() != 0) {
            return false;
        }
        for (PlayerInterface player : players) {
            if (player.getTeams().size() != player.getNumSpots()) {
                return false;
            }
        }
        return true;
    }
    
    public TeamInterface getTeam(int seed, Region region) {
        if (seed < 1 || seed > 16 || region == null) {
            return null;
        }
        return bracket.getTeam(seed, region);
    }
    
    public List<TeamInterface> getTeams() {
        List<TeamInterface> teams = bracket.getTeams();
        Collections.sort(teams);
        return teams;
    }
    
    public List<Double> getPricing() {
        return pricing;
    }
    
    public void setPricing(List<Double> pricing) {
        if (pricing != null) {
            this.pricing = pricing;
        }
    }
    
    public PrizeType[] getPrizes() {
        return PrizeType.values();
    }
    
    public int getLeaderboardSize() {
        return leaderboardSize;
    }
    
    public void setLeaderboardSize(int leaderboardSize) {
        if (leaderboardSize > -2) {
            this.leaderboardSize = leaderboardSize;
        }
    }
    
    public String toString() {
        return bracket.toString();
    }
}
