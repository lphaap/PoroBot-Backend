package tracker;

import java.util.List;

import com.merakianalytics.orianna.Orianna;
import com.merakianalytics.orianna.types.common.Region;
import com.merakianalytics.orianna.types.core.searchable.SearchableList;
import com.merakianalytics.orianna.types.core.spectator.CurrentMatch;
import com.merakianalytics.orianna.types.core.spectator.Player;
import com.merakianalytics.orianna.types.core.summoner.Summoner;

import app.App;
import database.Account;
import database.DBHandler;
import database.Server;
import database.User;

//To create objects of users being tracked
public class TrackerUtility implements Runnable {
	private User user;
	private UserTracker handler;
	private DBHandler db = new DBHandler();
	int timer = 60*60;
	private boolean timeout = false;
	private boolean inGame = false;
	private List<Player> currentMatchPlayers;
	
	public TrackerUtility(User u, UserTracker handler) {
		this.handler = handler;
		this.user = u;
	}	
	
	@Override
	public void run() {
		App.log("User " + user.getId() +" is now logged to tracker.");
		while(!timeout) {
			try {
				Thread.sleep(10000);
				
				timer -= 10;
				
				if(timer < 0) {
					this.timeout=true;
				}
				
				//TODO: Might improve performance if we stop this check every 10s and only check this ones
				if(db.defaultAccountExists(user.getId())) {
					monitorGameActivity();
				}
				
			} catch (InterruptedException e) {}
			
		}
	}

	public void monitorGameActivity() {
		Account def = db.findDefaultAccount(user.getId());
		if(def.getServer()==Server.EUNE) {
			if(Orianna.summonerNamed(def.getAccountName()).
			   withRegion(Region.EUROPE_NORTH_EAST).get().isInGame()) {
				
				this.inGame = true;
				this.resetTimer();
				this.setMatchParticipants();
			}
			else {
				if(this.inGame) {
					this.inGame = false;
				}
			}
		}
		else {
			if(Orianna.summonerNamed(def.getAccountName()).
					   withRegion(Region.EUROPE_WEST).get().isInGame()) {
				
				this.inGame = true;
				this.resetTimer();
				this.setMatchParticipants();
			}
			else {
				if(this.inGame) {
					this.inGame = false;
				}
			}
		}
	}
	
	public void resetMatch() {
		this.currentMatchPlayers = null;
	}
	
	//Gets current match participants from OriannaAPI, re == null if !isInGame();
	public List<Player> getMatchParticipants() {
		if(this.currentMatchPlayers == null) {
			this.setMatchParticipants();
		}
		return currentMatchPlayers;
	}
	
	private void setMatchParticipants() {
		if(this.inGame && this.currentMatchPlayers == null) {
			Account def = new DBHandler().findDefaultAccount(this.user.getId());
			
			Summoner caller = null;
			if(def.getServer() == Server.EUNE) {
				caller = Orianna.summonerNamed(def.getAccountName()).withRegion(Region.EUROPE_NORTH_EAST).get();
			}
			else {
				caller = Orianna.summonerNamed(def.getAccountName()).withRegion(Region.EUROPE_WEST).get();
			}
			if(caller == null) {this.currentMatchPlayers = null;}
			
			CurrentMatch match = caller.getCurrentMatch();
			SearchableList<Player> blue = match.getBlueTeam().getParticipants();
			SearchableList<Player> red = match.getRedTeam().getParticipants();
			SearchableList<Player> home = null;
			
			for(Player p : blue) {
				if(p.getSummoner().getAccountId().equals(caller.getAccountId())) {
					home = blue;
					break;
				}
			}
			if(home == null) {
				home = red;
			}
			
			this.currentMatchPlayers = home;
		
		}
	}
	
	public void resetTimer() {this.timer = 60*60;}
	
	public boolean getTimeout() {return this.timeout;}
	
	public boolean isInGame() {return this.inGame;}
	
	public void killTracker() {this.timeout = true;}
	

}
