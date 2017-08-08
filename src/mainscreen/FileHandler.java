/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mainscreen;

import model.team.TeamInterface;
import model.team.Region;
import model.player.PlayerInterface;
import model.prizes.PrizeType;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.Preferences;
import model.MarchMadnessDB;

public class FileHandler  {

    private File file;
    private  String otLoserName;
    private MainScreen screen;
    private  BufferedReader reader;
    private  PrintWriter writer;
    private Preferences pref;
    

    public FileHandler(MainScreen screen, File file) {
        this.screen = screen;
        pref = Preferences.userNodeForPackage(this.getClass());
        if(file != null)
            this.file = file;
        else
            this.file = new File(pref.get("DEFAULT_PATH",""));
        
    }
    
    public boolean setFile(File file) {
        if(file == null)
            return false;
        this.file = file;
        return true;
    }
    
    public File getFile() {
        return this.file;
    }

    public boolean initialize() throws FileNotFoundException,IOException {
        try {
            //boolean success = readConfigFile() && readTeamFile() && readPlayerFile();

            reader = new BufferedReader(new FileReader(file));
            pref.put("DEFAULT_PATH", file.getAbsolutePath());
            String line = reader.readLine();
            while (line != null) {
                switch (line) {
                    case "#Config:":
                        readConfigFile();
                        break;
                    case "#Teams:":
                        readTeamFile();
                        break;
                    case "#Players:":
                        readPlayerFile();
                        break;
                    default:
                        break;
                }
                line = reader.readLine();
            }
            reader.close();

            screen.db.setOTloser(screen.db.findTeam(otLoserName));
            return true;
        }  finally {
            
        }
    }

    private boolean readTeamFile() throws FileNotFoundException, IOException {
            //scanner = new Scanner(new File(teamFile));
        //reader = new BufferedReader(new FileReader(teamFile));

        String line = reader.readLine();
        String region;
        while (line != null && !line.contains("}") && line.contains("Region:")) {
            region = line.substring(line.indexOf(":") + 1, line.length()).trim();
            line = reader.readLine();

            while (line != null && !line.contains("Region:")) {
                int seed;
                String name = "";
                List<Integer> scores = new LinkedList<Integer>();
                String[] pieces = line.split(" ");
                seed = Integer.parseInt(pieces[0]);

                for (int i = 1; i < pieces.length - 1; i++) {
                    name += pieces[i];
                    if (i + 1 < pieces.length - 1) {
                        name += " ";
                    }
                }
                if (pieces[pieces.length - 1].length() > 2) {
                    pieces[pieces.length - 1] = pieces[pieces.length - 1].substring(1, pieces[pieces.length - 1].length() - 1);//remove the end brackets
                    pieces = pieces[pieces.length - 1].split(",");//fill pieces with scores
                    for (int i = 0; i < pieces.length; i++) {
                        try {
                            scores.add(Integer.parseInt(pieces[i]));
                        } catch (NumberFormatException e) {
                        }
                    }
                }
                try {
                    if (screen.db.addTeam(name, seed, region, scores) != 0) {
                        System.out.println("ERROR: " + name);
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }

                line = reader.readLine();
                if (line.contains("}")) {
                    return true;
                }
            }
        }

        return true;
    }

    private boolean readPlayerFile() throws FileNotFoundException, IOException {
            //scanner = new Scanner(new File(playerFile));
        //reader = new BufferedReader(new FileReader(playerFile));

        String line = reader.readLine();
        while (!line.contains("}") && !line.contains("Chosen Team")) {
            if (!line.contains("Player:")) {
                line = reader.readLine();
            }
            String name = "";
            PlayerInterface player;
            int spotsPurchased = 0;
            int tiebreakerScore = 0;
            boolean paid = false;
            while (line != null && line.contains("Player")) {
                line = line.substring(line.indexOf(":") + 1, line.length()).trim(); //<Name> <Spots Purhcased>
                String[] pieces = line.split(" ");
                short offset;
                if (pieces[pieces.length - 1].equals("paid")) {
                    offset = 3;
                    paid = true;
                } else {
                    offset = 2;
                    paid = false;
                }
                try {
                    spotsPurchased = Integer.parseInt(pieces[pieces.length - offset]);
                    tiebreakerScore = Integer.parseInt(pieces[pieces.length - offset + 1]);
                } catch (NumberFormatException e) {
                    spotsPurchased = 0;
                    tiebreakerScore = 0;
                }
                /**
                 * Reconstructs name
                 */
                name = "";
                for (int i = 0; i < pieces.length - offset; i++) {
                    name += pieces[i];
                    if (i + 1 < pieces.length - offset) {
                        name += " ";
                    }
                }
                /**
                 * Attempts to add player to database
                 */
                try {
                    if (screen.db.addPlayer(name, spotsPurchased) != 0) {

                    }
                } catch (IllegalArgumentException e) {

                }

                line = reader.readLine();//NEXT PLAYER OR "#CHOSEN TEAM"
                if (line.contains("}")) {
                    return true;
                }
            }
            /**
             * Find player to add data to
             */
            player = screen.db.findPlayer(name);
            if (line != null && player != null && !line.contains("}")) {
                /**
                 * Set payment status
                 */
                if (paid) {
                    player.paid();
                }
                /**
                 * Set tiebreaker score from above
                 */
                player.setPredictedScore(tiebreakerScore);
                /**
                 * Reads in player's chosen team
                 */
                if (line.contains("Chosen Team:")) {
                    line = reader.readLine();
                    if (line != null && !line.contains("Owned Teams:")) {
                        try {
                            screen.db.addChosenWinner(player, screen.db.findTeam(line));
                        } catch (IllegalArgumentException e2) {
                        }

                        line = reader.readLine();
                    }
                }
                while (line != null && line.contains("Owned Teams:")) {
                    line = reader.readLine();
                    while (line != null && !line.contains("Pick Four Teams:")) {
                        try {
                            screen.db.addOwnedTeam(player, screen.db.findTeam(line));
                        } catch (IllegalArgumentException e2) {
                        }

                        line = reader.readLine();
                    }
                }
                while (line != null && line.contains("Pick Four Teams:")) {
                    line = reader.readLine();
                    while (line != null && !line.contains("Player") && !line.contains("}")) {
                        try {
                            screen.db.addPickFourTeam(player, screen.db.findTeam(line));
                        } catch (IllegalArgumentException e2) {
                        }
                        line = reader.readLine();
                        if (line.contains("}")) {
                            return true;
                        }
                    }
                }
            }
        }
        //reader.close();
        return true;
    }

    private boolean readConfigFile() throws FileNotFoundException, IOException {
            //scanner = new Scanner(new File(configFile));
        //reader = new BufferedReader(new FileReader(configFile));

        String line = reader.readLine();
        while (!line.contains("}")) {
            if (line.contains("#Pricing:")) {
                line = reader.readLine();
                while (line != null && !line.contains("#")) {
                    if (line.contains("Spots Pricing:")) {
                        List<Double> pricing = new LinkedList<Double>();
                        line = line.substring(line.indexOf(":") + 1).trim();
                        line = line.replace("[", "").replace("]", "");
                        String[] pieces = line.split(",");
                        for (int i = 0; i < pieces.length; i++) {
                            try {
                                pricing.add(Double.parseDouble(pieces[i].trim()));
                            } catch (NumberFormatException numExcep) {
                                return false;
                            }
                        }
                        screen.db.setPricing(pricing);
                        line = reader.readLine();
                    }
                }
            }
            if (line != null && line.contains("#Prizes:")) {
                line = reader.readLine();
                double prize;
                List<Double> pickFourPrizes = new LinkedList<Double>();
                while (line != null && !line.contains("#")) {
                    if (line.contains("Blowout Loss Prize:")) {
                        line = line.substring(line.indexOf(":") + 1).trim();
                        try {
                            prize = Double.parseDouble(line);
                            screen.db.setPrizeMoney(PrizeType.FIRST_ROUND_BLOWOUT, prize);
                        } catch (NumberFormatException numExcep) {
                        }
                    } else if (line.contains("Cinderella Prize:")) {
                        line = line.substring(line.indexOf(":") + 1).trim();
                        try {
                            prize = Double.parseDouble(line);
                            screen.db.setPrizeMoney(PrizeType.CINDERELLA_TEAM, prize);
                        } catch (NumberFormatException numExcep) {
                        }
                    } else if (line.contains("First Overtime Loss Prize:")) {
                        line = line.substring(line.indexOf(":") + 1).trim();
                        try {
                            prize = Double.parseDouble(line);
                            screen.db.setPrizeMoney(PrizeType.FIRST_OVERTIME_LOSS, prize);
                        } catch (NumberFormatException numExcep) {
                        }
                    } else if (line.contains("Chosen Winner Prize:")) {
                        line = line.substring(line.indexOf(":") + 1).trim();
                        try {
                            prize = Double.parseDouble(line);
                            screen.db.setPrizeMoney(PrizeType.CHOSEN_WINNER, prize);
                        } catch (NumberFormatException numExcep) {
                        }
                    } else if (line.contains("Won First Game Prize:")) {
                        line = line.substring(line.indexOf(":") + 1).trim();
                        try {
                            prize = Double.parseDouble(line);
                            screen.db.setPrizeMoney(PrizeType.WON_FIRST_GAME, prize);
                        } catch (NumberFormatException numExcep) {
                        }
                    } else if (line.contains("Final Four Team:")) {
                        line = line.substring(line.indexOf(":") + 1).trim();
                        try {
                            prize = Double.parseDouble(line);
                            screen.db.setPrizeMoney(PrizeType.FINAL_FOUR_TEAM, prize);
                        } catch (NumberFormatException numExcep) {
                        }
                    } else if (line.contains("Champion Team:")) {
                        line = line.substring(line.indexOf(":") + 1).trim();
                        try {
                            prize = Double.parseDouble(line);
                            screen.db.setPrizeMoney(PrizeType.CHAMPION_TEAM, prize);
                        } catch (NumberFormatException numExcep) {
                        }
                    } else if (line.contains("Pick Four Prize:")) {
                        line = line.substring(line.indexOf(":") + 1).replace("[", "").replace("]", "").trim();
                        String[] pieces = line.split(",");
                        for (int i = 0; i < pieces.length; i++) {
                            try {
                                pickFourPrizes.add(Double.parseDouble(pieces[i]));
                            } catch (NumberFormatException numExcep) {
                            }
                        }
                        screen.db.setPrizeMoney(PrizeType.PICK_FOUR_CHAMPION, pickFourPrizes.remove(0));
                        screen.db.setPrizeMoney(PrizeType.PICK_FOUR_RUNNERUP, pickFourPrizes.remove(0));
                    }

                    line = reader.readLine();
                }
            }
            if (line.contains("#Region Matchups:")) {
                line = reader.readLine();
                Region[] regionMatchups = new Region[screen.db.getRegionMatchups().length];
                int index = 0;
                while (line != null && !line.contains("#")) {
                    String[] pieces = line.split("vs");
                    int i = 0;
                    while (index < regionMatchups.length && i < pieces.length) {
                        regionMatchups[index] = Region.determineRegion(pieces[i]);
                        i++;
                        index++;
                    }
                    line = reader.readLine();
                }
                screen.db.setRegionMatchups(regionMatchups);
            }
            if (line != null && line.contains("#Other:") && !line.contains("}")) {
                line = reader.readLine();
                int size;
                while (line != null && !line.contains("#")) {
                    if (line.contains("Leaderboard Size:")) {
                        line = line.substring(line.indexOf(":") + 1).trim();
                        try {
                            size = Integer.parseInt(line);
                            screen.db.setLeaderboardSize(size);
                        } catch (NumberFormatException numExcep) {
                        }
                    } else if (line.contains("First Overtime Loss Team:")) {
                        otLoserName = line.substring(line.indexOf(":") + 1, line.length()).trim();
                    } else if (line.contains("Title:")) {
                        screen.setMainTitle(line.substring(line.indexOf(":") + 1, line.length()).trim());
                    }
                    else if (line.contains("Subtitle:")) {
                        screen.setMainSubtitle(line.substring(line.indexOf(":") + 1, line.length()).trim());
                    }
                    line = reader.readLine();
                    if (line.contains("}")) {
                        return true;
                    }
                }
            }
            line = reader.readLine();
            if (line.contains("}")) {
                return true;
            }
        }

        //reader.close();
        return true;
    }

    /**
     * Not fully tested?
     *
     * @return
     */
    public boolean saveToFile() throws FileNotFoundException {
        /**
         * Write out team information to team file
         */
        if(screen.db == null) 
            return false;
        if(file == null || !file.exists())
            throw new FileNotFoundException();
        this.screen.db = screen.db;
        OutputStream os = new FileOutputStream(file);
        pref.put("DEFAULT_PATH", file.getAbsolutePath());
        writer = new PrintWriter(os);
         
              //catch (URISyntaxException e2) {e2.printStackTrace(); return false;}
        //Config
        writer.println("#Config:");
        writer.println("#Pricing:");
        writer.println("Spots Pricing: " + screen.db.getPricing());
        writer.println("#Prizes:");
        writer.println("Blowout Loss Prize: " + screen.db.getPrizeMoney(PrizeType.FIRST_ROUND_BLOWOUT));
        writer.println("Cinderella Prize: " + screen.db.getPrizeMoney(PrizeType.CINDERELLA_TEAM));
        writer.println("First Overtime Loss Prize: " + screen.db.getPrizeMoney(PrizeType.FIRST_OVERTIME_LOSS));
        writer.println("Pick Four Prize: [" + screen.db.getPrizeMoney(PrizeType.PICK_FOUR_CHAMPION) + ", "
                + screen.db.getPrizeMoney(PrizeType.PICK_FOUR_RUNNERUP) + "]");
        writer.println("Chosen Winner Prize: " + screen.db.getPrizeMoney(PrizeType.CHOSEN_WINNER));
        writer.println("Won First Game Prize: " + screen.db.getPrizeMoney(PrizeType.WON_FIRST_GAME));
        writer.println("Final Four Team: " + screen.db.getPrizeMoney(PrizeType.FINAL_FOUR_TEAM));
        writer.println("Champion Team: " + screen.db.getPrizeMoney(PrizeType.CHAMPION_TEAM));
        writer.println("#Region Matchups:");
        Region[] regionMatchups = screen.db.getRegionMatchups();
        for (int i = 0; i < regionMatchups.length; i++) {
            writer.print(regionMatchups[i] + " vs ");
            i++;
            if (i < regionMatchups.length) {
                writer.print(regionMatchups[i]);
            }
            writer.println();
        }
        writer.println("#Other:");
        writer.println("Leaderboard Size: " + screen.db.getLeaderboardSize());
        String teamName = ((screen.db.getOTloser() != null) ? screen.db.getOTloser().getName() : "");
        writer.println("First Overtime Loss Team: " + teamName);
        writer.println("Title: "+screen.getMainTitle());
        writer.println("Subtitle: "+screen.getMainSubtitle());
        writer.println("}");
        //Teams
        writer.println("#Teams:");
        Iterator<TeamInterface> itr = screen.db.getTeams().iterator();
        Region r = null;
        while (itr.hasNext()) {
            TeamInterface team = itr.next();
            if (r == null || r != team.getRegion()) {
                r = team.getRegion();
                writer.println("#Region: " + team.getRegion());
            }
            writer.print(team.getSeed() + " " + team.getName() + " ");
            try {
                String scores = screen.db.getScores(team).toString().replaceAll(" ", "");
                writer.println(scores);
            } catch (Exception e) {
                writer.println("[]");
            }
        }
        writer.println("}");
        //Players
        writer.println("#Players:");
        Iterator<PlayerInterface> itr2 = screen.db.getPlayers().iterator();
        Iterator<TeamInterface> teams;
        while (itr2.hasNext()) {
            PlayerInterface player = itr2.next();
            String paidStatus = (player.hasPaid() ? "paid" : "");
            writer.println("#Player: " + player.getName() + " " + player.getNumSpots() + " " + player.getPredictedScore() + " " + paidStatus);
            writer.println("\t#Chosen Team:");
            if (player.getChosenWinner() != null) {
                writer.println("\t\t" + player.getChosenWinner().getName());
            }
            writer.println("\t#Owned Teams:");
            teams = player.getTeams().iterator();
            while (teams.hasNext()) {
                writer.println("\t\t" + teams.next().getName());
            }
            writer.println("\t#Pick Four Teams:");
            teams = player.getPickFourTeams().iterator();
            while (teams.hasNext()) {
                writer.println("\t\t" + teams.next().getName());
            }
            writer.flush();
        }
        writer.println("}");
        writer.close();
        
        return true;
    }
    
    public boolean fillOutputFile(File file) throws FileNotFoundException {
        if(screen.db == null) 
            return false;
        if(file == null || !file.exists())
            throw new FileNotFoundException();
        MarchMadnessDB db = screen.db;
        db.update();
        OutputStream os = new FileOutputStream(file);
        writer = new PrintWriter(os);
        writer.println("PLAYERS:");
        for(PlayerInterface player: db.getPlayers()) {
            writer.println(db.getPlayerInfo(player));      
        }
        
        writer.println("--------------------------------------------------------"
                + "----------------------------------------");
        writer.println("PICK FOUR LEADERBOARD");
        writer.println("--------------------------------------------------------"
                + "----------------------------------------");
        writer.println(screen.getPickFourLeaderboard());
        writer.println("--------------------------------------------------------"
                + "----------------------------------------");
        writer.println("OWNED TEAM LEADERBOARD");
        writer.println("--------------------------------------------------------"
                + "----------------------------------------");
        writer.println(screen.getPlayerLeaderboard());
        writer.println("--------------------------------------------------------"
                + "----------------------------------------");
        writer.println("U-PICK-IT");
        writer.println("--------------------------------------------------------"
                + "----------------------------------------");
        writer.println(screen.getChosenWinnerLeaderboard());
        writer.println("--------------------------------------------------------"
                + "----------------------------------------");
        
        writer.println("CONTESTS:");
        writer.println("--------------------------------------------------------"
                + "----------------------------------------");
        for(PrizeType prize: PrizeType.values()) {
            writer.print(" -"+prize.getName());
            if(prize.equals(PrizeType.WON_FIRST_GAME))
                writer.print(" (each instance is $"+db.getPrizeMoney(prize)+")");
            writer.println(": "+db.getPrizeWinner(prize)+"\n");
        }
        writer.println("--------------------------------------------------------"
                + "----------------------------------------");
        writer.close();
        return true;
    }

}
