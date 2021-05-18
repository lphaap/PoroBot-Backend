package discord;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.AEADBadTagException;

import app.App;
import database.Account;
import database.DBHandler;
import database.ReportReason;
import database.Server;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import tracker.CombinedUser;
import tracker.ReportHandler;
import tracker.UserTracker;
import websitedata.HTMLParser;
import websitedata.LinkHandler;
import websitedata.ParsedChampion;
import websitedata.ParsedPlayer;


public class MessageProcessor extends ListenerAdapter{
	private DBHandler db;
	private LinkHandler links = new LinkHandler();
	private HTMLParser parser = new HTMLParser();
	private ReportHandler rh = new ReportHandler();
	private UserTracker tracker = new UserTracker(rh);
	private final String ownerID = "267695837897949194";
	
	public MessageProcessor() {
		Thread t = new Thread(tracker);
		t.start();
		App.log("TRACKER INIT");
		db = new DBHandler();
		App.log("DB INIT");
	}
	
	private void tester(MessageReceivedEvent event) {
		String msg = event.getMessage().getContentRaw();
		String msgLowerCase = msg.toLowerCase();
		if(msg.startsWith("!") && !event.getAuthor().isBot()) {
		if(db.userExists("267695962074382336")) { //User is registered
			CombinedUser user = new CombinedUser(db.findUserByID("267695962074382336"));
			user.setMsgUtility(event.getChannel());
			App.log(""+tracker.isUserTracked(user.getUserData().getId()));
			if(msgLowerCase.startsWith("!register")) { //User registered but calls !register
				user.getMsgUtility().sendMessage("This account is already registered!");
				user.getMsgUtility().sendMessage("Continue with \"!login\"");
			}
			
			else if(tracker.isUserTracked(user.getUserData().getId())){ //User is logged in
				
				CombinedUser realUser = tracker.findUser(user.getUserData().getId());
				
				if(msgLowerCase.startsWith("!login")) { //User calls login while logged in
					realUser.getMsgUtility().sendMessage("You are already logged in!");
				}
				else if(msgLowerCase.startsWith("!logout")) {
					
					if(realUser != null) {
						realUser.getTrackerUtility().killTracker();
					}
					realUser.getMsgUtility().sendMessage("You are being logged out...");
				}
				else { //User is logged in and calls a command -> Forward to handleMsg()
					this.handleMsg(msg, realUser);
				}
				
			}
			
			else if(msgLowerCase.startsWith("!login")) { //User not logged in and calls login
					this.tracker.addUser(user);
					user.getMsgUtility().sendMessage("Login Successful.");
					
					if(!user.getUserData().getUsername().equals(event.getAuthor().getName())) {
						db.updateUsername(user.getUserData().getId(), event.getAuthor().getName());
					}
					user.getMsgUtility().sendMessage("Welcome back " + event.getAuthor().getName() + "! (ﾉ◕ヮ◕)ﾉ*:・ﾟ✧");
					
					if(!db.defaultAccountExists(user.getUserData().getId())) {
						user.getMsgUtility().sendMessageAtUser("Pssst.. you haven't setup an account yet ヽ༼ ຈل͜ຈ༼ ▀̿̿Ĺ̯̿̿▀̿ ̿༽Ɵ͆ل͜Ɵ͆ ༽ﾉ"
															+ "\r\n" + "Add an account with \"!default eune-AccountName\""
															+ "\r\n" + "or euw account with \"!default euw-AccountName\"");
					}
					
			}
		}}
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		new Thread(() -> handleLogin(event)).start();
	}
	
	public void handleLogin(MessageReceivedEvent event) {
		String msg = event.getMessage().getContentRaw();
		String msgLowerCase = msg.toLowerCase();
		//tester(event);
			
		//Check that bot is using dms and not talking to itself/another bot
		if(msg.startsWith("!") && !event.getAuthor().isBot()) { //Message starts with command
			
			App.log("Command "+ msgLowerCase +" from user " + event.getAuthor().getName() + " " + event.getAuthor().getId());
			
			if(!event.isFromType(ChannelType.PRIVATE)) { //Message not from dms
				event.getChannel().sendMessage("\r\n" + event.getAuthor().getAsMention() + "\r\n" + "Please Dm me instead!").queue();
				return;
			}
			
			if(msgLowerCase.equals("!kill 0129") && event.getAuthor().getId().equals(ownerID)) { //Kill program
				App.log("KILLING SERVER");
				event.getChannel().sendMessage("\r\n" + "KILLING SERVER").queue();;
				System.exit(0);
				return;
			}
			
			if(msgLowerCase.startsWith("!info")) {
				String send = "\r\n" + "Hello there! (✿◠‿◠)";
				send = send + "\r\n" + "Im a bot that can help you with que dodging and getting you links to different stat websites!";
				send = send + "\r\n" + "I use my own report database to inform you about reported griefers and give you useful data for your queue.";
				send = send + "\r\n" + "I also have links for all the common data websites blitz, opgg etc. with just 1 command";
				send = send + "\r\n" + "You just have to tell me your League Username and I'll do the rest!";
				send = send + "\r\n";
				send = send + "\r\n" + "TLDR: Start with !setup command to setup your account!";
				event.getChannel().sendMessage(send).queue();
				return;
			}
			
			if(msgLowerCase.startsWith("!setup")) {
				String send = "\r\n" + "Follow these steps to setup your account! (◕ᴥ◕ʋ)"+ "\r\n";
				send = send + "\r\n" + "1. Dm me !register to save your discord account"+ "\r\n";
				send = send + "\r\n" + "2. Dm me !login to login to the system (uses your unique Discord ID)" + "\r\n";
				send = send + "\r\n" + "3. Next setup your account with (Notice the server at the start of command!):";
				send = send + "\r\n" + "    !default eune-AccountName - Replace \"AccountName\" with your League Username";
				send = send + "\r\n" + "    !default euw-AccountName - Replace \"AccountName\" with your League Username" + "\r\n";
				send = send + "\r\n" + "EXTRA: Be sure to try !help for all commands and learn how to use the !que command";
				event.getChannel().sendMessage(send).queue();
				return;
			}
			
			if(db.userExists(event.getAuthor().getId())) { //User is registered
				CombinedUser user = new CombinedUser(db.findUserByID(event.getAuthor().getId()));
				user.setMsgUtility(event.getChannel());
			
				if(msgLowerCase.startsWith("!register")) { //User registered but calls !register
					user.getMsgUtility().sendMessage("This account is already registered!");
					user.getMsgUtility().sendMessage("Continue with \"!login\"");
				}
				
				else if(tracker.isUserTracked(user.getUserData().getId())){ //User is logged in
					
					CombinedUser realUser = tracker.findUser(user.getUserData().getId());
					
					if(msgLowerCase.startsWith("!login")) { //User calls login while logged in
					//TODO: Crashes here with kallu ID
						realUser.getMsgUtility().sendMessage("You are already logged in!");
					}
					else if(msgLowerCase.startsWith("!logout")) {
						
						if(realUser != null) {
							realUser.getTrackerUtility().killTracker();
						}
						realUser.getMsgUtility().sendMessage("You are being logged out...");
					}
					else { //User is logged in and calls a command -> Forward to handleMsg()
						this.handleMsg(msg, realUser);
					}
					
				}
				
				else if(msgLowerCase.startsWith("!login")) { //User not logged in and calls login
						this.tracker.addUser(user);
						user.getMsgUtility().sendMessage("Login Successful.");
						
						if(!user.getUserData().getUsername().equals(event.getAuthor().getName())) {
							db.updateUsername(user.getUserData().getId(), event.getAuthor().getName());
						}
						user.getMsgUtility().sendMessage("Welcome back " + event.getAuthor().getName() + "! (ﾉ◕ヮ◕)ﾉ*:・ﾟ✧");
						
						if(!db.defaultAccountExists(user.getUserData().getId())) {
							user.getMsgUtility().sendMessageAtUser("Pssst.. you haven't setup an account yet ヽ༼ ຈل͜ຈ༼ ▀̿̿Ĺ̯̿̿▀̿ ̿༽Ɵ͆ل͜Ɵ͆ ༽ﾉ"
																+ "\r\n" + "Add an account with \"!default eune-AccountName\""
																+ "\r\n" + "or euw account with \"!default euw-AccountName\"");
						}
						
				}
				
				else { //User not logged in and calls a command
					user.getMsgUtility().sendMessage("You are not logged in. (҂◡_◡) ᕤ");
					user.getMsgUtility().sendMessage("Start with \"!login\"");
				}
				
			}
			else if(msgLowerCase.startsWith("!register")) { //User not registered but calls !register
				db.newUser(event.getAuthor().getName(), event.getAuthor().getId());
				event.getChannel().sendMessage("\r\n" + "User " + event.getAuthor().getName() + " registered.").queue();
				event.getChannel().sendMessage("\r\n" + "Thank you for joining! ( ဖ‿ဖ)人(စ‿စ )").queue();
				event.getChannel().sendMessage("\r\n" + "\r\n" + "You can continue with \"!login\"").queue();
			}
			else { //User not registered tries to call command
				event.getChannel().sendMessage("\r\n" + "Sorry! This account is not registered. ( ͡° ͜ʖ ͡°)").queue();
				event.getChannel().sendMessage("\r\n" + "Start with \"!register\".").queue();
				event.getChannel().sendMessage("\r\n" + "If you need help with account setup try \"!setup\"").queue();
				event.getChannel().sendMessage("\r\n" + "Or If you want to know more about me features start with \"!info\"").queue();
			}
			
		}//If start*/

	}//Method end
	
	public void handleMsg(String msg, CombinedUser user){
		String msgLowerCase = msg.toLowerCase();
		
		if(msgLowerCase.startsWith("!help")) {
			String help = "\r\n" + "!login - Login to the system" +
						  "\r\n" + "!logout - Logout of the system" + 
						  "\r\n" + "!swap - Swap between user's saved accounts" +
						  "\r\n" + "!register - Register a new user to the system" +
						  "\r\n" + "!reports PlayerName - Checks if given user is reported to system (Uses default account server)" +
						  "\r\n" + "!toxic 1234 - Reports players 1,2,3,4 for being Toxic, ONLY AFTER GAME" + 
						  "\r\n" + "!griefer 1234 - Reports players 1,2,3,4 for Griefing, ONLY AFTER GAME" + 
						  "\r\n" + "!opgg - OP.GG of your default Account" +
						  "\r\n" + "!blitz - Blitz of your default Account" + 
						  "\r\n" + "!graphs - League of graphs of your default Account" +
						  "\r\n" + "!game or !porofessor - Porofessor active game of your default Account" +
						  "\r\n" + "!accounts - To see your saved accounts" +
						  "\r\n" + "!default - To see your default account" + 
						  "\r\n" + "!default eune-AccountName - Change or add default account" +
						  "\r\n" + "!default euw-AccountName - Change or add default account" +
						  "\r\n" + "!eune profile ProfileName - Graphs and OP.GG of given Profile" +
						  "\r\n" + "!euw profile ProfileName - Graphs and OP.GG of given Profile" +
						  "\r\n" + "!eune game ProfileName - Porofessor active game of given Profile" +
						  "\r\n" + "!euw game ProfileName - Porofessor active game of given Profile" +
						  //"\r\n" + "" +
						  "\r\n" + "༼ つ ◕_◕ ༽つ GL&HF" ; 
			
			user.getMsgUtility().sendMessage(help);
		}
		
		//Check if given user is reported
		else if(msgLowerCase.startsWith("!reports")) {
			if(!this.accountCreationMsg(user)) {return;} //Check if user has saved at least 1 account
			
			Account c = db.findDefaultAccount(user.getUserData().getId());
			
			String[] split = msgLowerCase.split(" ");
			String name = "";
			if(split.length > 2) {
				name = split[1] + " " + split[2];
			}
			else {name = split[1];} 
			
			String reps = "Player " + c.getServer()+"-"+name+" is Reported for:";
			boolean tester = false;
			if(db.findReport(name, c.getServer(), ReportReason.GRIEFER) != null) {
				reps = reps + " Griefing";
				tester = true;
			}
			if(db.findReport(name, c.getServer(), ReportReason.TOXIC) != null) {
				if(tester) {
					reps = reps + " & Toxicity";
				}
				else {reps = reps + " Toxicity"; tester = true;}
			}
			if(tester) {
				user.getMsgUtility().sendMessageAtUser(reps);
			}
			else {user.getMsgUtility().sendMessageAtUser("No reports found for " +c.getServer()+"-"+ name);}
			
		}
		
		//Parse que data and send dodge info
		else if(msgLowerCase.startsWith("!que")) {
			if(!this.accountCreationMsg(user)) {return;} //Check if user has saved at least 1 account
			
			Account c = db.findDefaultAccount(user.getUserData().getId());
			
			ArrayList<String> names = this.parseLobbyString(msg, c.getAccountName());
			
			List<ParsedPlayer> playerData = this.parser.rankedDataQueue(names, c.getServer());
			
			String send = "";
			for(ParsedPlayer p : playerData) {
				send = send + "\r\n" + p.getName() + ": "
							+ p.getMainRole() + " | "
							+ p.getGames() 
							+ " Games | " + p.getWr() + "%";
				for(ParsedChampion champ : p.getChamps()) {
					send = send + "\r\n" + "  -   " + champ.getName() + ": "
							+ champ.getRole() + " | "
							+ champ.getGames() 
							+ " Games | " + champ.getWr() + "%";
				}
				if(p.isWrWarning()) {
					send = send + "\r\n" + "WARNING! Negative WR: " + p.getWr() + "%";
				}
				if(p.isOtpWarning()) {
					send = send + "\r\n" + "WARNING! OTP: " + p.getFirstChamp().getName();
				}
				if(p.isToxicWarning()) {
					send = send + "\r\n" + "WARNING! Reported for TOXICITY";
				}
				if(p.isGrieferWarning()) {
					send = send + "\r\n" + "WARNING! Reported for GRIEFING";
				}
				
				send = send + "\r\n";
			}
		
			user.getMsgUtility().sendMessageAtUser(send);
			user.getMsgUtility().sendMessageAtUser(db.createLobbyJson(playerData));
		}
		
		//Change/add a default eune account
		else if(msgLowerCase.startsWith("!default eune-")) {
			String name = msg.split("-")[1];
			Account a = null;
			if(db.accountExists(user.getUserData().getId(), name, Server.EUNE)) {
				a = db.findSpecificAccount(user.getUserData().getId(), name, Server.EUNE);
				db.updateDefaultAccount(user.getUserData().getId(), a.getId());
			}
			else {
				db.newAccount(name, user.getUserData().getId(), Server.EUNE);
				a = db.findSpecificAccount(user.getUserData().getId(), name, Server.EUNE);
				db.updateDefaultAccount(user.getUserData().getId(), a.getId());
			}
			
			user.getMsgUtility().sendMessage("Your default account is now: " + a.getAccountName() + " - " + a.getServer());
		}
		
		//Swap current default account to the next on the list
		else if(msgLowerCase.startsWith("!swap")) {
			if(!this.accountCreationMsg(user)) {return;} //Check if user has saved at least 1 account
			
			ArrayList<Account> accounts = (ArrayList<Account>) db.findAccountsByUserID(user.getUserData().getId());
			int len = accounts.size()-1;
			int next = 0;
			Account def = db.findDefaultAccount(user.getUserData().getId());

			for(Account acc : accounts) {
				if(acc.getId().equals(def.getId())) {
					next = (accounts.indexOf(acc)+1);
					break;
				}
			}
			if(next > len) { next = 0;}
			
			db.updateDefaultAccount(user.getUserData().getId(), accounts.get(next).getId());
		
			user.getMsgUtility().sendMessage("Your default account is now: " + accounts.get(next).getAccountName()
																		+ " - " + accounts.get(next).getServer());
		}
		
		//Change/add a default euw account
		else if(msgLowerCase.startsWith("!default euw-")) {
			String name = msg.split("-")[1];
			Account a = null;
			if(db.accountExists(user.getUserData().getId(), name, Server.EUW)) {
				a = db.findSpecificAccount(user.getUserData().getId(), name, Server.EUW);
				db.updateDefaultAccount(user.getUserData().getId(), a.getId());
			}
			else {
				db.newAccount(name, user.getUserData().getId(), Server.EUW);
				a = db.findSpecificAccount(user.getUserData().getId(), name, Server.EUW);
				db.updateDefaultAccount(user.getUserData().getId(), a.getId());
			}
			
			user.getMsgUtility().sendMessage("Your default account is now: " + a.getAccountName() + " - " + a.getServer());
		}
		
		//Send default account for logged in user
		else if(msgLowerCase.equals("!default")) {
			if(!this.accountCreationMsg(user)) {return;} //Check if user has saved at least 1 account
			
			Account a = db.findDefaultAccount(user.getUserData().getId());
			user.getMsgUtility().sendMessageAtUser("Your default account is: " + a.getAccountName() + " - " + a.getServer());
		}
		
		//Send default account for logged in user
		else if(msgLowerCase.equals("!accounts")) {
			if(!this.accountCreationMsg(user)) {return;} //Check if user has saved at least 1 account
			
			List<Account> a = db.findAccountsByUserID(user.getUserData().getId());
			int number = 1;
			user.getMsgUtility().sendMessageAtUser("You have saved these accounts to the system:");
			for(Account acc : a) {
				user.getMsgUtility().sendMessage("" + number + ". "+ acc.getAccountName() + " - " + acc.getServer());
				number++;
			}
		}
		
		//Reports the after game griefers to system
		else if(msgLowerCase.startsWith("!griefer")) {
			if(!this.accountCreationMsg(user)) {return;} //Check if user has saved at least 1 account
			
			String[] split = msgLowerCase.split(" ");
			List<Integer> toReport = new ArrayList<Integer>();
			for(char c : split[1].toCharArray()) {
				try {
					int add = Integer.parseInt(Character.toString(c));
					toReport.add(add);
				}
				catch(Exception e) {continue;}
			}
			if(!toReport.isEmpty()) {
				String re = rh.reportGriefer(toReport, user.getUserData().getId());
				if(re.equals("")) {
					user.getMsgUtility().sendMessage("Sorry, didn't find reportable game, play one first ( ´◔ ω◔`) ノシ");
				}
				else {
					user.getMsgUtility().sendMessageAtUser(re + "\r\n" + "(⌐■_■)︻╦╤─ (╥﹏╥)");
				}
			}
			else {
				user.getMsgUtility().sendMessage("༼ つ ╹ ╹ ༽つ Sorry, couldn't understand your report, be sure to follow the examples! ");
			}
		}
		
		//Reports the after game griefers to system
		else if(msgLowerCase.startsWith("!toxic")) {
			if(!this.accountCreationMsg(user)) {return;} //Check if user has saved at least 1 account
			
			String[] split = msgLowerCase.split(" ");
			List<Integer> toReport = new ArrayList<Integer>();
			for(char c : split[1].toCharArray()) {
				try {
					int add = Integer.parseInt(Character.toString(c));
					toReport.add(add);
				}
				catch(Exception e) {continue;}
			}
			if(!toReport.isEmpty()) {
				String re = rh.reportToxic(toReport, user.getUserData().getId());
				if(re.equals("")) {
					user.getMsgUtility().sendMessage("Sorry, didn't find reportable game, play one first ( ´◔ ω◔`) ノシ");
				}
				else {
					user.getMsgUtility().sendMessageAtUser(re + "\r\n" + "(⌐■_■)︻╦╤─ (╥﹏╥)");
				}
			}
			else {
				user.getMsgUtility().sendMessage("༼ つ ╹ ╹ ༽つ Sorry, couldn't understand your report, be sure to follow the examples!");
			}
		}
		
		//Opgg for logged in users default acc
		else if(msgLowerCase.startsWith("!opgg")) {
			if(!this.accountCreationMsg(user)) {return;} //Check if user has saved at least 1 account
			
			user.getMsgUtility().sendMessageAtUser(links.getOPGG(db.findDefaultAccount(user.getUserData().getId())));
		}
		
		//Blitz for logged in users default acc
		else if(msgLowerCase.startsWith("!blitz")) {
			if(!this.accountCreationMsg(user)) {return;} //Check if user has saved at least 1 account
			
			user.getMsgUtility().sendMessageAtUser(links.getBlitz(db.findDefaultAccount(user.getUserData().getId())));
		}
		
		//Porofessor for logged in users default acc
		else if(msgLowerCase.startsWith("!game") || msg.toLowerCase().startsWith("!porofessor")) {
			if(!this.accountCreationMsg(user)) {return;} //Check if user has saved at least 1 account
			
			user.getMsgUtility().sendMessageAtUser(links.getPorofessor(db.findDefaultAccount(user.getUserData().getId())));
		}
		
		//League of Graphs for logged in users default acc
		else if(msgLowerCase.startsWith("!graphs")) {
			if(!this.accountCreationMsg(user)) {return;} //Check if user has saved at least 1 account
			
			user.getMsgUtility().sendMessageAtUser(links.getLeagueOfGraphs(db.findDefaultAccount(user.getUserData().getId())));
		}
		
		//Eune profile without login
		else if(msgLowerCase.startsWith("!eune profile ")) {
			String name = msgLowerCase.substring(14, msgLowerCase.length());
			user.getMsgUtility().sendMessageAtUser(links.getLeagueOfGraphs(name, Server.EUNE));
			user.getMsgUtility().sendMessage(links.getOPGG(name, Server.EUNE));
		}
		
		//Eune game without login
		else if(msgLowerCase.startsWith("!eune game ")) {
			String name = msgLowerCase.substring(11, msgLowerCase.length());
			user.getMsgUtility().sendMessageAtUser(links.getPorofessor(name, Server.EUNE));
		}
		
		//West profile without login
		else if(msgLowerCase.startsWith("!euw profile ")) {
			String name = msgLowerCase.substring(13, msgLowerCase.length());
			user.getMsgUtility().sendMessageAtUser(links.getLeagueOfGraphs(name, Server.EUW));
			user.getMsgUtility().sendMessage(links.getOPGG(name, Server.EUW));
		}
		
		//West game without login
		else if(msgLowerCase.startsWith("!euw game ")) {
			String name = msgLowerCase.substring(10, msgLowerCase.length());
			user.getMsgUtility().sendMessageAtUser(links.getPorofessor(name, Server.EUW));
		}
		else {
			user.getMsgUtility().sendMessage("Sorry, I dont know what that means ლ(ၴ෴ၴ)ლ");
			user.getMsgUtility().sendMessage("Try \"!help\" for all commands");
		}
	}
	
	public ArrayList<String> parseLobbyString(String raw, String username){
		String[] split = raw.split("\n");
		ArrayList<String> names = new ArrayList<String>();
		
		for(String s : split) {
			if(s.contains(" joined the lobby")) {
				s = s.replace(" joined the lobby", "");
			}
			if(s.contains("!que ")) {
				s = s.replace("!que ", "");
			}
			if(!s.contains("left the lobby") && !names.contains(s) && !s.equalsIgnoreCase(username)) {
				names.add(s);
			}
		}
		
		return names;
	}
	
	//Check if user has saved at least 1 account
	public boolean accountCreationMsg(CombinedUser user) {
		if(!db.defaultAccountExists(user.getUserData().getId())) {
			user.getMsgUtility().sendMessageAtUser("This command only works after account setup ヽ༼ ຈل͜ຈ༼ ▀̿̿Ĺ̯̿̿▀̿ ̿༽Ɵ͆ل͜Ɵ͆ ༽ﾉ"
					+ "\r\n" + "Add an account with \"!default eune-AccountName\""
					+ "\r\n" + "Or euw account with \"!default euw-AccountName\"");
			return false;
		}
		else {return true;}
	}
	

    
	
}
