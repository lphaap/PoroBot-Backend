package tracker;

import java.util.ArrayList;
import java.util.List;

import com.merakianalytics.orianna.types.core.spectator.Player;

import app.App;
import database.Account;
import database.DBHandler;
import database.User;

//To keep tracked of all users under tracking
public class UserTracker implements Runnable{
	
	ArrayList<CombinedUser> trackedUsers = new ArrayList<CombinedUser>();
	ArrayList<CombinedUser> activeGames = new ArrayList<CombinedUser>();
	
	ReportHandler rh;
	
	boolean run = true;
	
	public UserTracker(ReportHandler rh) {
		this.rh = rh;
	}
	
	@Override
	public void run() {
		while(run) {
			try{Thread.sleep(10000);}
			catch(Exception e) {App.log(e.getMessage());}
			monitorUser();
		}
		
	}
	
	public void addUser(CombinedUser user) {
		this.trackedUsers.add(user);
		user.setTrackerUtility(new TrackerUtility(user.getUserData(), this));
		new Thread(user.getTrackerUtility()).start();
	}
	
	//Logs user off from tracker if timed out and handles Game ends and starts
	private void monitorUser() {
		ArrayList<CombinedUser> rmTracked = new ArrayList<CombinedUser>();
		for(CombinedUser u : trackedUsers) {//Find timed out users and new active games
			if(u.getTrackerUtility().getTimeout()) {
				rmTracked.add(u);
			}
			else if(u.getTrackerUtility().isInGame() && !activeGames.contains(u)) {
				activeGames.add(u);
				App.log("Active Game: " + u.getUserData().getUsername() + " " + u.getUserData().getId());
			}
		}
		for(CombinedUser u : rmTracked) {//Remove timedOut Users
			trackedUsers.remove(u);
			u.getMsgUtility().sendMessage("You have been logged out.");
			App.log("User " + u.getUserData().getId() +" is now logged out.");
		}
		
		List<CombinedUser> rmGames = new ArrayList<CombinedUser>();
		for(CombinedUser u : activeGames) {//Find finished games from activeGames
			if(!u.getTrackerUtility().isInGame()) {
				rmGames.add(u);
			}
		}
		for(CombinedUser u : rmGames) {//Remove finished games from ActiveGames
			activeGames.remove(u);
			this.handleGameEnd(u);
		}
		
	}
	
	public void handleGameEnd(CombinedUser u) {
		App.log("Game ended handling user: " + u.getUserData().getUsername() + " " + u.getUserData().getId());
		List<Player> players = u.getTrackerUtility().getMatchParticipants();
		Account def = new DBHandler().findDefaultAccount(u.getUserData().getId());
		String send = "Would you like to report Anyone?" + "\r\n";
		List<String> reports = new ArrayList<String>();
		int number = 1;
		for(Player p : players) {
			if(!p.getSummoner().getName().equalsIgnoreCase(def.getAccountName())) {
				send = send + number + ". " + p.getChampion().getName() + " | ";
				reports.add(p.getSummoner().getName());
				number++;
			}
		}
		send = send + "\r\n" + "Report player with their above number, Example: !griefer 134 or !toxic 12";
		rh.addReportable(new Reportable(reports, u.getUserData().getId()));
		u.getTrackerUtility().resetMatch();
		u.getMsgUtility().sendMessageAtUser(send);
		
	}
	
	//Returns false if user has timed out -> User is not tracker and will be logged out
	public boolean isUserTracked(String id) {
		boolean found = false;
		loop: for(CombinedUser u : trackedUsers) {
			if(u.getUserData().getId().equals(id)) {found = true; break loop;}
		}
		return found;
	}
	
	public void killThread() {
		this.run = false;
	}
	
	public CombinedUser findUser(String id) {
		for(CombinedUser u : trackedUsers) {
			if(u.getUserData().getId().equals(id)) {
				if(u.isTrackable()) {
					u.getTrackerUtility().resetTimer();
				}
				return u;
			}
		}
		return null;
	}



}
