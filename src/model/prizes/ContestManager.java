package model.prizes;

import model.player.PlayerInterface;
import model.team.TeamInterface;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import model.bracket.Bracket;

public class ContestManager {

    private Contest[] contests;
    private Bracket bracket;

    private final double CHOSEN_WINNER_PRIZE = 20;
    private final double CINDERELLA_TEAM_PRIZE = 10;
    private final double FIRST_OVERTIME_LOSS_PRIZE = 10;
    private final double FIRST_ROUND_BLOWOUT_LOSS_PRIZE = 20;
    private final double WON_FIRST_GAME_PRIZE = 5;
    private final double FINAL_FOUR_TEAM_PRIZE = 20;
    private final double CHAMPION_TEAM_PRIZE = 80;
    private final double PICK_FOUR_CHAMPION_PRIZE = 30;
    private final double PICK_FOUR_RUNNERUP_PRIZE = 10;
    private final boolean SPLIT_PRIZE = true;

    private TeamInterface championTeam;
    private TeamInterface otLoser;
    private List<TeamInterface> finalFourTeams;
    private List<TeamInterface> firstRoundBlowout;

    private int mostCinderellaWins = 0;
    private int topPickFourScore = 0;
    private int secondPickFourScore = 0;
    private final int MIN_CINDERELLA_SEED = 9;

    public ContestManager(Bracket b) {
        bracket = b;

        contests = new Contest[PrizeType.getNumPrizes()];

        contests[PrizeType.CHOSEN_WINNER.getPrizeNum()]
                = new Contest(CHOSEN_WINNER_PRIZE,SPLIT_PRIZE);
        contests[PrizeType.CINDERELLA_TEAM.getPrizeNum()]
                = new Contest(CINDERELLA_TEAM_PRIZE,SPLIT_PRIZE);
        contests[PrizeType.FIRST_OVERTIME_LOSS.getPrizeNum()]
                = new Contest(FIRST_OVERTIME_LOSS_PRIZE,!SPLIT_PRIZE);
        contests[PrizeType.FIRST_ROUND_BLOWOUT.getPrizeNum()]
                = new Contest(FIRST_ROUND_BLOWOUT_LOSS_PRIZE,SPLIT_PRIZE);
        contests[PrizeType.WON_FIRST_GAME.getPrizeNum()]
                = new Contest(WON_FIRST_GAME_PRIZE,!SPLIT_PRIZE);
        contests[PrizeType.FINAL_FOUR_TEAM.getPrizeNum()]
                = new Contest(FINAL_FOUR_TEAM_PRIZE,!SPLIT_PRIZE);
        contests[PrizeType.CHAMPION_TEAM.getPrizeNum()]
                = new Contest(CHAMPION_TEAM_PRIZE,!SPLIT_PRIZE);
        contests[PrizeType.PICK_FOUR_CHAMPION.getPrizeNum()]
                = new Contest(PICK_FOUR_CHAMPION_PRIZE,SPLIT_PRIZE);
        contests[PrizeType.PICK_FOUR_RUNNERUP.getPrizeNum()]
                = new Contest(PICK_FOUR_RUNNERUP_PRIZE,SPLIT_PRIZE);
    }

    public void setOTLoserTeam(TeamInterface loser) {
        otLoser = loser;
    }

    public TeamInterface getOTLoserTeam() {
        return otLoser;
    }

    public boolean setPrizeMoney(PrizeType prizeType, double prizeMoney) {
        if (prizeType == null) {
            return false;
        }
        contests[prizeType.getPrizeNum()].setPrizeMoney(prizeMoney);
        return true;
    }

    public double getPrizeMoney(PrizeType prizeType) {
        if (prizeType == null) {
            return 0;
        }
        return contests[prizeType.getPrizeNum()].getPrizeMoney();
    }

    public List<PlayerInterface> getWinningPlayers(PrizeType prizeType) {
        if (prizeType == null) {
            return null;
        }
        return contests[prizeType.getPrizeNum()].getPlayers();
    }

    public List<TeamInterface> getWinningTeams(PrizeType prizeType) {
        if (prizeType == null) {
            return null;
        }
        return contests[prizeType.getPrizeNum()].getTeams();
    }

    //Check if first round is done (for some) and if all scores are entered (others) save time and don't declare winners early
    public void update(List<Double> pricing, List<PlayerInterface> players) {
        if (bracket == null) {
            return;
        }

        for (Contest contest : contests) {
            contest.setPlayers(new LinkedList<>());
            contest.setTeams(new LinkedList<>());
        }

        finalFourTeams = new LinkedList<>();
        Iterator<TeamInterface> itr = bracket.getTeams().iterator();
        while (itr.hasNext()) {
            TeamInterface temp = itr.next();
            if (temp.getWins() >= 4) {
                finalFourTeams.add(temp);
            }
        }
        championTeam = bracket.getChampion();
        firstRoundBlowout = new LinkedList<>();
        getFirstRoundBlowout();

        topPickFourScore = 0;
        secondPickFourScore = 0;
        for (PlayerInterface player : players) {
            if (!player.hasPaid()) {
                player.setBalance(0 - pricing.get(player.getNumSpots()));
            } else {
                player.setBalance(0);
            }

            for (TeamInterface ownedTeam : player.getTeams()) {
                checkFinalFourTeam(player, ownedTeam);

                checkChampionTeam(player, ownedTeam);

                checkOTLoser(player, ownedTeam);

                checkFirstRoundBlowout(player, ownedTeam);

                checkWonFirstGame(player, ownedTeam);

                checkCinderellaTeam(player, ownedTeam);
            }
            checkChosenWinner(player);

            checkPickFourContest(player);

        }

        if (contests[PrizeType.CHOSEN_WINNER.getPrizeNum()].getPlayers().size() > 1) { //Tiebreaker
            chosenWinnerTiebreaker();
        }
        List<PlayerInterface> p4Winners = contests[PrizeType.PICK_FOUR_CHAMPION.getPrizeNum()].getPlayers();
        if (!p4Winners.isEmpty() && p4Winners.size() == players.size()
                && p4Winners.get(0).getPickFourPoints() == 0) {
            contests[PrizeType.PICK_FOUR_CHAMPION.getPrizeNum()].setPlayers(new LinkedList<>());
        }
        List<PlayerInterface> p4RunnerUps = contests[PrizeType.PICK_FOUR_RUNNERUP.getPrizeNum()].getPlayers();
        if (!p4RunnerUps.isEmpty() && p4RunnerUps.size()+p4Winners.size() == players.size()
                && p4RunnerUps.get(0).getPickFourPoints() == 0) {
            contests[PrizeType.PICK_FOUR_RUNNERUP.getPrizeNum()].setPlayers(new LinkedList<>());
        }

        updateBalances();
    }

    private void checkChosenWinner(PlayerInterface player) {
        if (championTeam != null
                && player.getChosenWinner() != null
                && player.getChosenWinner().equals(championTeam)) {
            Contest contest = contests[PrizeType.CHOSEN_WINNER.getPrizeNum()];
            contest.getTeams().add(championTeam);
            contest.getPlayers().add(player);
        }
    }

    private void checkWonFirstGame(PlayerInterface player, TeamInterface team) {
        if (team.getWins() > 0) {
            Contest contest = contests[PrizeType.WON_FIRST_GAME.getPrizeNum()];
            contest.getTeams().add(team);
            contest.getPlayers().add(player);
        }
    }

    private void checkChampionTeam(PlayerInterface player, TeamInterface team) {
        if (championTeam != null && team.equals(championTeam)) {
            Contest contest = contests[PrizeType.CHAMPION_TEAM.getPrizeNum()];
            contest.getTeams().add(team);
            contest.getPlayers().add(player);
        }
    }

    private void checkOTLoser(PlayerInterface player, TeamInterface team) {
        if (otLoser != null && otLoser.equals(team)) {
            Contest contest = contests[PrizeType.FIRST_OVERTIME_LOSS.getPrizeNum()];
            contest.getTeams().add(team);
            contest.getPlayers().add(player);
        }
    }

    private void checkFirstRoundBlowout(PlayerInterface player, TeamInterface team) {
        for (TeamInterface blowout : firstRoundBlowout) {
            if (blowout.equals(team)) {
                Contest contest = contests[PrizeType.FIRST_ROUND_BLOWOUT.getPrizeNum()];
                contest.getTeams().add(team);
                contest.getPlayers().add(player);
            }
        }
    }

    private void checkFinalFourTeam(PlayerInterface player, TeamInterface team) {
        Iterator<TeamInterface> ffItr = finalFourTeams.iterator();
        Contest contest = contests[PrizeType.FINAL_FOUR_TEAM.getPrizeNum()];
        while (ffItr.hasNext()) {
            TeamInterface finalFourTeam = ffItr.next();
            if (team.equals(finalFourTeam)) {
                ffItr.remove();
                contest.getTeams().add(team);
                contest.getPlayers().add(player);
            }
        }
    }

    private void checkCinderellaTeam(PlayerInterface player, TeamInterface team) {
        if (team.getSeed() < MIN_CINDERELLA_SEED) {
            return;
        }
        Contest contest = contests[PrizeType.CINDERELLA_TEAM.getPrizeNum()];
        if ((team.getWins() > mostCinderellaWins) || //team has advanced further
                (mostCinderellaWins != 0 && team.getWins() == mostCinderellaWins && //teams have advanced same distance
                (contest.getTeams().isEmpty() || team.getSeed() > contest.getTeams().get(0).getSeed()))) { //but team wins tiebreaker
            contest.setTeams(new LinkedList<>());
            contest.setPlayers(new LinkedList<>());
            contest.getPlayers().add(player);
            contest.getTeams().add(team);
            mostCinderellaWins = team.getWins();
        } else if (mostCinderellaWins != 0 && team.getWins() == mostCinderellaWins
                && team.getSeed() == contest.getTeams().get(0).getSeed()) {
            contest.getPlayers().add(player);
            contest.getTeams().add(team);
        }
    }

    private void checkPickFourContest(PlayerInterface player) {
        int score = player.getPickFourPoints();
        if (score > topPickFourScore) {
            Contest top = contests[PrizeType.PICK_FOUR_CHAMPION.getPrizeNum()];
            Contest second = contests[PrizeType.PICK_FOUR_RUNNERUP.getPrizeNum()];
            second.setPlayers(top.getPlayers());
            top.setPlayers(new LinkedList<>());
            top.getPlayers().add(player);
            secondPickFourScore = topPickFourScore;
            topPickFourScore = score;
        } else if (score == topPickFourScore) {
            Contest top = contests[PrizeType.PICK_FOUR_CHAMPION.getPrizeNum()];
            top.getPlayers().add(player);
        } else if (score > secondPickFourScore) {
            Contest second = contests[PrizeType.PICK_FOUR_RUNNERUP.getPrizeNum()];
            second.setPlayers(new LinkedList<>());
            second.getPlayers().add(player);
            secondPickFourScore = score;
        } else if (score == secondPickFourScore) {
            Contest second = contests[PrizeType.PICK_FOUR_RUNNERUP.getPrizeNum()];
            second.getPlayers().add(player);
        }
    }

    public void getFirstRoundBlowout() {
        if (!bracket.initialized()) {
            return;
        }

        List<TeamInterface> teams = bracket.getTeams();
        firstRoundBlowout = new LinkedList<>();
        TeamInterface opponent;
        int maxDifference = 0;
        for (TeamInterface team : teams) {
            if (team.getWins() > 0) {
                opponent = bracket.getOpponents(team).get(0);

                int teamScore = bracket.getScores(team, 1).get(0);
                int opponentScore = bracket.getScores(opponent, 1).get(0);

                int difference = teamScore - opponentScore;
                if (difference > maxDifference) {
                    maxDifference = difference;
                    firstRoundBlowout = new LinkedList<>();
                    firstRoundBlowout.add(opponent);
                } else if (difference == maxDifference) {
                    firstRoundBlowout.add(opponent);
                }
            }
        }
    }

    private void chosenWinnerTiebreaker() {
        int totalScore;
        List<Integer> scores = bracket.getScores(championTeam);//Champions championship game score
        totalScore = scores.get(scores.size() - 1);
        List<TeamInterface> opps = bracket.getOpponents(championTeam);//Opponent's championship game score
        scores = bracket.getScores(opps.get(opps.size() - 1));
        totalScore += scores.get(scores.size() - 1);
        int smallestDif = Integer.MAX_VALUE;
        for (PlayerInterface player : contests[PrizeType.CHOSEN_WINNER.getPrizeNum()].getPlayers()) {
            int tempDif = Math.abs(player.getPredictedScore() - totalScore);
            if (tempDif < smallestDif) {
                smallestDif = tempDif;
            }
        }
        Iterator<PlayerInterface> tempItr = contests[PrizeType.CHOSEN_WINNER.getPrizeNum()].getPlayers().iterator();
        while (tempItr.hasNext()) {
            PlayerInterface player = tempItr.next();
            int tempDif = Math.abs(player.getPredictedScore() - totalScore);
            if (tempDif != smallestDif) {
                tempItr.remove();
            }
        }
    }

    private void updateBalances() {
        double earnings = 0;
        for (Contest contest : contests) {
            if (contest.equals(contests[PrizeType.PICK_FOUR_CHAMPION.getPrizeNum()])
                    && contest.getPlayers().size() > 1) {
                earnings = (contest.getPrizeMoney()
                        + contests[PrizeType.PICK_FOUR_RUNNERUP.getPrizeNum()].getPrizeMoney())
                        / contest.getPlayers().size();

                contests[PrizeType.PICK_FOUR_RUNNERUP.getPrizeNum()].setPlayers(new LinkedList<>());
                contests[PrizeType.PICK_FOUR_RUNNERUP.getPrizeNum()].setTeams(new LinkedList<>());
            } else if (contest.splitPrize()) {
                earnings = contest.getPrizeMoney() / contest.getPlayers().size();
            }  else {
                earnings = contest.getPrizeMoney();
            }
            //System.out.println(contest);
            for (PlayerInterface player : contest.getPlayers()) {
                //System.out.println(player+" "+player.getBalance()+" + "+earnings);
                player.setBalance(player.getBalance() + earnings);
            }

        }
    }

}
