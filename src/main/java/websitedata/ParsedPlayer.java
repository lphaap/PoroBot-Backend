package websitedata;

import java.util.List;

import database.DBHandler;
import database.Player;
import database.ReportReason;
import database.Server;

public class ParsedPlayer {
	private String name;
	private String mainRole;
	private int games;
	private double wr;
	private List<ParsedChampion> champs;
	private boolean wrWarning = false;
	private boolean otpWarning = false;
	private boolean grieferWarning = false;
	private boolean toxicWarning = false;
	private Server server;
	
	public ParsedPlayer(String name, String mainRole, String games, String wr, List<ParsedChampion> champs,Server server) {
		this.name=name;
		this.mainRole=mainRole;
		this.games = Integer.parseInt(games.replace(" ", "").replace("\n", ""));
		this.wr = Double.parseDouble(wr.replace(" ", "").
											  replace("\n", "").
											  replace("%",""));
		this.champs = champs;
		
		if(this.wr < 50.0) {
			wrWarning = true;
		}
		if((this.games/2) < champs.get(0).getGames()) {
			otpWarning = true;
		}
		
		DBHandler db = new DBHandler();
		if(db.findReport(this.name, server, ReportReason.TOXIC) != null) {
			this.toxicWarning = true;
		}
		if(db.findReport(this.name, server, ReportReason.GRIEFER) != null) {
			this.grieferWarning = true;
		}
	}

	public double getWr() {
		return wr;
	}

	public int getGames() {
		return games;
	}

	public String getMainRole() {
		return mainRole;
	}

	public String getName() {
		return name;
	}
	
	//Expects list to be in most played order
	public List<ParsedChampion> getChamps(){
		return this.champs;
	}
	
	public ParsedChampion getFirstChamp() {
		return this.champs.get(0);
	}
	public ParsedChampion getSecondChamp() {
		return this.champs.get(1);
	}
	public ParsedChampion getThirdChamp() {
		return this.champs.get(2);
	}

	public boolean isOtpWarning() {
		return otpWarning;
	}

	public boolean isWrWarning() {
		return wrWarning;
	}
	
	public boolean isToxicWarning() {
		return toxicWarning;
	}

	public boolean isGrieferWarning() {
		return grieferWarning;
	}
	

}
