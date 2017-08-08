package model.prizes;

public enum PrizeType {

	CHOSEN_WINNER(0, 
			"U-Pick-It",
			"The player(s) who correctly select the\n"
			+ "team that won the NCAA tournament.","Predicted Championship Score."),
	CINDERELLA_TEAM(1,
			"Cinderella",
			"The player(s) with the furthest advancing\n"
			+ "team ranked 9-16","None. Split Prize."),
	FIRST_OVERTIME_LOSS(2,
			"Hard-Luck Loser",
			"The player who owns the first team to lose\n"
			+ "in overtime","N/A"),
	FIRST_ROUND_BLOWOUT(3,
			"Just Glad To Be Here",
			"The player(s) who own the team that lost by\n"
			+ "the largest margin in the first round","None. Split Prize."),
	WON_FIRST_GAME(4,
			"Won First Game",
			"The players whose team(s) have won at least\n"
			+ "one game","N/A"),
	
	FINAL_FOUR_TEAM(5, 
			"Final Four Team",
			"The players whose team(s) made it to the\n"
			+ "final four","N/A"),
	CHAMPION_TEAM(6, 
			"Championship Team",
			"The player who owns the team that won the\n"
			+ "NCAA tournament","N/A"),
	
	PICK_FOUR_CHAMPION(7, 
			"Pick Four Contest Champion",
			"The player(s) whose pick four teams scored the\n"
			+ "most points","None. Split Prize."),
	PICK_FOUR_RUNNERUP(8, 
			"Pick Four Contest Runner-Up",
			"The player(s) whose pick four teams scored the\n"
			+ "second most points","None. Split Prize.");
	
	private static final int NUM_PRIZES = 9;
	private int prizeNum;
	private String name;
	private String description;
        private String tiebreaker;
	
	PrizeType(int prizeNum, String name, String description, String tiebreaker) {
		this.prizeNum = prizeNum;
		this.name = name;
		this.description = description;
                this.tiebreaker = tiebreaker;
	}
	
	public int getPrizeNum() {
		return this.prizeNum;
	}
	
	public static int getNumPrizes() {
		return NUM_PRIZES;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getDescription() {
		return this.description;
	}
        
        public String getTiebreaker() {
            return this.tiebreaker;
        }
	
	public static PrizeType findPrizeType(String prizeType) {
		prizeType = prizeType.trim().toLowerCase();
		switch(prizeType)
		{
		case "chosen winner":
			return CHOSEN_WINNER;
		case "cinderella team":
			return CINDERELLA_TEAM;
		case "first overtime loss":
			return FIRST_OVERTIME_LOSS;
		case "first round blowout":
			return FIRST_ROUND_BLOWOUT;
		case "won first game":
			return WON_FIRST_GAME;
		case "final four team":
			return FINAL_FOUR_TEAM;
		case "champion team":
			return CHAMPION_TEAM;
		case "pick four winner":
			return CINDERELLA_TEAM;
		case "pick four runnerup":
			return PICK_FOUR_RUNNERUP;
		default:
			return null;
		}
	}
        
        @Override
        public String toString() {
            return this.getName();
        }
	
}
