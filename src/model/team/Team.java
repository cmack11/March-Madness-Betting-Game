package model.team;

public class Team implements TeamInterface, Comparable<TeamInterface>
{
    /*The full name of the team, can be any size*/
    private String name;
    /*The name of the team that is either the first letter of each word or the first four letters of one word*/
    private String shortName;
    /*The team's seed or rank for a tournament*/
    private int seed;
    /*The region of the tournament that the team is in*/
    private Region region;
    /*The amount of wins the team has*/
    private int wins;
    /*Whether the team is still in the tournament or if they have been eliminated*/
    private boolean alive;

    /**
     * Constructs a team that is alive and has zero wins
     * @param name
     * @param seed
     * @param region 
     */
    public Team(String name, int seed, Region region) {
            this(name,seed,region,true,0);
    }

    /**
     * Constructs a team with zero wins
     * @param name
     * @param seed
     * @param region
     * @param alive 
     */
    public Team(String name, int seed, Region region, boolean alive) {
            this(name,seed,region,alive,0);
    }

    /**
     * Constructs a team with the given non-null and non-negative characteristics
     * @param name 
     * @param seed 
     * @param region
     * @param alive - true if team has not be eliminated
     * @param wins - number of times team has won
     */
    public Team(String name, int seed, Region region, boolean alive, int wins)
    {
        if(name == null || seed < 1 || region == null || wins < 0)
            throw new IllegalArgumentException();
        this.name = name.trim();
        if(name.length() < 1)
            throw new IllegalArgumentException();
        this.shortName = determineShortName(name);
        this.seed = seed;
        this.region = region;
        this.alive = alive;
        this.wins = wins;
    }

    public String getName() {
            return name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setName(String name) {
            if(name == null || name.trim().length() < 1)
                    throw new IllegalArgumentException();
            this.name = name;
    }

    public Region getRegion() {
            return region;
    }

    public int getSeed() {
            return seed;
    }

    public int getWins() {
            return wins;
    }

    public boolean is_alive() {
            return alive;
    }

    public void loses() {
            alive = false;
    }

    public void wins() {
            if(!alive)
                    throw new IllegalArgumentException();
            wins++;
    }

    public String toString() {
            return "#"+seed+" "+name;
    }

    @Override
    public int compareTo(TeamInterface o) {
            if(o == null)
                    throw new NullPointerException();
            return this.getName().compareTo(o.getName());
    }

    public void setWins(int wins) {
            this.wins = wins;
    }

    public void setStatus(boolean status) {
            this.alive = status;
    }

    private String determineShortName(String name) {
        if(name == null)
            return "";
        String shortName = "";
        name = name.trim();
        String[] split = name.split(" ");
        for(int i = 0; split.length > 1 && i < split.length; i++) {
            shortName += split[i].substring(0,1).toUpperCase();
        }
        if(shortName.length() == 0) {
            shortName += split[0].substring(0,Math.min(4, split[0].length())).toUpperCase();
        }
        return shortName;
    }
	
	
}
