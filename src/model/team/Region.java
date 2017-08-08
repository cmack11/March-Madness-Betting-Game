package model.team;

public enum Region 
{
	WEST(),
	MIDWEST(),
	EAST(),
	SOUTH();
	
	//private int regionNumber;
	
	Region()
	{
		//this.regionNumber = regionNumber;
	}
	
	/**
	public int getRegionNumber()
	{
		return this.regionNumber;
	}
	**/
	
	public static Region determineRegion(String region)
	{
		region = region.trim().toLowerCase();
		switch(region)
		{
		case "midwest":
			return MIDWEST;
		case "west":
			return WEST;
		case "south":
			return SOUTH;
		case "east":
			return EAST;
		default:
			return null;
		}
	}
	
}
