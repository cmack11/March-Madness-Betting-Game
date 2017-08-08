package model.team;

public interface TeamInterface extends Comparable<TeamInterface> {	
	public String getName();
	
	public Region getRegion();
	
	public int getSeed();
	
	public int getWins();
	
	public boolean is_alive();
	
	public void loses();
	
	public void wins();
	
	public String toString();
	
	public void setName(String name);
        
        public String getShortName();

	public void setWins(int wins);

	public void setStatus(boolean status);
}
