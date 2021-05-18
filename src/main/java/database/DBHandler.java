package database;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import app.App;
import io.jsondb.JsonDBTemplate;
import io.jsondb.crypto.Default1Cipher;
import io.jsondb.crypto.ICipher;
import io.jsondb.query.Update;
import websitedata.ParsedChampion;
import websitedata.ParsedPlayer;

public class DBHandler {
	private String location = "G:/Databases/PoroDB";
			//"G:/Databases/PoroDB";
			//"C:/Users/lassi/DBTester";
	
	private JsonDBTemplate db;
	
	public DBHandler() {
		if(App.cmdSetup) { //If app is guven database as cmd parameter
			this.location = App.databasePath;
		}
		
		String baseScanPackage = "database";

		ICipher cipher = null;
		try {
			cipher = new Default1Cipher("1r8+24pibarAWgS85/Heeg==");
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.db = new JsonDBTemplate(location, baseScanPackage, cipher);
		
	}
	
	//Add new user to DB, WARNING ID must relate to discord id
	public void newUser(String name, String ID) {
		this.update();
		
		User u = new User();
		u.setId(ID);
		u.setUsername(name);
		u.setDefaultAccountId(null);
		db.insert(u);
		
		System.out.println("New user added with id: " + ID);
	}
	
	//Find user from DB with discord ID
	public User findUserByID(String ID) {
		this.update();
		
		return db.findById(ID, "users");
	}
	
	public boolean userExists(String discordID) {
		this.update();
		
		if(db.findById(discordID, User.class) == null) {
			return false;
		}
		else {
			
			return true;
		}
	}
	
	//Add new account to DB, WARNING userID must relate to discord id
	public void newAccount(String accountName, String userID, Server server) {
		this.update();
		
		Account ac = new Account();
		ac.setServer(server);
		ac.setAccountName(accountName);
		ac.setUserId(userID);
		long idL = (db.findAll(Account.class).size()+1);
		ac.setId("" + idL);

		db.upsert(ac);
		
		System.out.println("New account added for user: " + userID);
	}
	
	//Add new Report to DB, WARNING reporterID must relate to discord id
	public void newReport(String accountName, String reporterID, Server server, ReportReason reason) {
		this.update();
		
		if(this.findReport(accountName.toLowerCase(), server, reason) == null) {
			Report r = new Report();
			r.setAccountName(accountName.toLowerCase());
			r.setServer(server);
			r.setReason(reason);
			r.setReporterId(reporterID);
			long idL = (db.findAll(Report.class).size()+1);
			r.setId("" + idL);
	
			db.upsert(r);
			
			System.out.println("New Report added: " + r.getAccountName() + 
							   " - " + r.getReason() + " - " + r.getReporterId());
			}
		else {
			App.log("Report exists");
		}
	}
	
	//Find user accounts from DB with discord ID
	public List<Account> findAccountsByUserID(String ID) {
		this.update();
		
		return db.find(String.format("/.[userId='%s']", ID), Account.class);
	}
	
	//Find Reports for given info
	public Report findReport(String name, Server server, ReportReason reason) {
		this.update();
		
		List<Report> found = db.find(String.format("/.[accountName='%s']",name.toLowerCase()), Report.class);
		if(found == null) {return null;}
		else {
			for(Report r : found) {
				if(r.getServer() == server && r.getReason() == reason) {
					return r;
				}
			}
		}
		return null;
	}
	
	//Find user default account with userID, returns null if not found
	public Account findDefaultAccount(String userId) {
		this.update();
		
		User u = db.findById(userId, User.class);
		if(u.getDefaultAccountId() == null) {
			return null;
		}
		else {
			return db.findById(u.getDefaultAccountId(), Account.class);
		}
	}
	
	//Find account with user ID, accountName and server
	public Account findSpecificAccount(String userId, String accName, Server server) {
		this.update();
		
		List<Account> found = this.findAccountsByUserID(userId);
		Account correct = null;
		for(Account a : found) {
			if(a.getAccountName().equals(accName) && a.getServer() == server) {
				correct = a;
				break;
			}
		}
		return correct;
	}
	
	public boolean accountExists(String userId, String accName, Server server) {
		this.update();
		
		if(this.findSpecificAccount(userId, accName, server) == null) {
			return false;
		}
		else {return true;}
	}
	
	//Find account with accounts id
	public Account findAccountById(String id) {
		this.update();
	
		return db.findById(id, Account.class);
	}
	
	//Checks if default account exists for userID
	public boolean defaultAccountExists(String userId) {
		this.update();
		
		if(this.findDefaultAccount(userId) == null) {
			return false;
		}
		else { return true; }
	}
	
	//Update default accountId of userId to database
	public void updateDefaultAccount(String userId, String accountId) {
		this.update();
		
		db.findAndModify(String.format("/.[id='%s']", userId), 
						 Update.update("defaultAccountId", 
						 accountId), User.class);
		App.log("User " + userId + " default-account updated.");
	}
	
	//Update default accountId of userId to database
	public void updateUsername(String userId, String newName) {
		this.update();
		
		db.findAndModify(String.format("/.[id='%s']", userId), 
						 Update.update("username", 
						 newName), User.class);
		App.log("User " + userId + " username updated.");
	}
	
	//Saves lobby to db and returns link to created game id
	public String createLobbyJson(List<ParsedPlayer> players) {
		this.update();
		
		long gameid = createGameId();
		
		Delete d = new Delete();
		d.setTime(LocalDateTime.now());
		d.setId(gameid);
		db.upsert(d);
		
		for(ParsedPlayer p : players) {
			App.log(""+p.getName());
			Player save = new Player();
			save.setGameId(gameid);
			save.setId(db.findAll(Player.class).size()+1);
			save.setName(p.getName());
			save.setRole(p.getMainRole());
			save.setGames(p.getGames());
			save.setWr(p.getWr());
			save.setGriefer(p.isGrieferWarning());
			save.setToxic(p.isToxicWarning());
			save.setOtp(p.isOtpWarning());
			save.setNegativeWr(p.isWrWarning());
			
			for(ParsedChampion c : p.getChamps()) {
				PlayerChampion saveChamp = new PlayerChampion();
				saveChamp.setChamp(c.getName());
				saveChamp.setGames(c.getGames());
				saveChamp.setWr(c.getWr());
				if(save.getC1() == null) {
					save.setC1(saveChamp);
				}
				else if(save.getC2() == null) {
					save.setC2(saveChamp);
				}
				else {
					save.setC3(saveChamp);
				}
			}
			db.upsert(save);
		}
		
		return "games/"+gameid;
	}
	
	//Find players with given gameID
	public List<Player> findPlayers(long gameId) {
		this.update();
		
		return db.find(String.format("/.[gameId='%s']", gameId), Player.class);
	}
	
	public long createGameId() {
		this.update();
		
		long time = System.currentTimeMillis();
		long re = time%100000000;
		if(db.findById(re, Delete.class) != null) {
			return (time%1000000000-1);
		}
		return re;
	}
	
	//Find all deletes
	public List<Delete> findAllDeletes(){
		this.update();
		
		return db.findAll(Delete.class);
	}
	
	public void removeAllDeletesByGameId(long gameId) {
		this.update();
		
		this.db.findAllAndRemove(String.format("/.[id='%s']", gameId), Delete.class);
	}
	
	public void removeAllPlayersByGameId(long gameId) {
		this.update();
		
		this.db.findAllAndRemove(String.format("/.[gameId='%s']", gameId), Player.class);
	}
	
	//DELETE ON LAUNCH
	public JsonDBTemplate getRawDB() {
		return this.db;
	}
	
	private void update() {
		this.db.reLoadDB();
	}
	
	
	

	
	
	

}
