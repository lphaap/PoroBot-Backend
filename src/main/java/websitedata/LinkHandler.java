package websitedata;

import database.Account;
import database.Server;
import database.User;

public class LinkHandler {
	
	public String getPorofessor(Account a) {
		StringBuffer name = new StringBuffer(a.getAccountName().toLowerCase());
		while(name.indexOf(" ") != -1) {
			name.deleteCharAt(name.indexOf(" "));
		}
		if(a.getServer() == Server.EUNE) {
			return "https://porofessor.gg/live/eune/"+name;
		}
		else {
			return "https://porofessor.gg/live/euw/"+name;
		}
	}
	
	public String getPorofessor(String accountName, Server server) {
		StringBuffer name = new StringBuffer(accountName.toLowerCase());
		while(name.indexOf(" ") != -1) {
			name.deleteCharAt(name.indexOf(" "));
		}
		if(server == Server.EUNE) {
			return "https://porofessor.gg/live/eune/"+name;
		}
		else {
			return "https://porofessor.gg/live/euw/"+name;
		}
	}
	
	public String getBlitz(Account a) {
		StringBuffer name = new StringBuffer(a.getAccountName().toLowerCase());
		while(name.indexOf(" ") != -1) {
			name.replace(name.indexOf(" "), name.indexOf(" ")+1, "%20");
		}
		if(a.getServer() == Server.EUNE) {
			return "https://blitz.gg/lol/profile/eun1/"+name;
		}
		else {
			return "https://blitz.gg/lol/profile/euw1/"+name;
		}
	}
	
	public String getBlitz(String accountName, Server server) {
		StringBuffer name = new StringBuffer(accountName.toLowerCase());
		while(name.indexOf(" ") != -1) {
			name.replace(name.indexOf(" "), name.indexOf(" ")+1, "%20");
		}
		if(server == Server.EUNE) {
			return "https://blitz.gg/lol/profile/eun1/"+name;
		}
		else {
			return "https://blitz.gg/lol/profile/euw1/"+name;
		}
	}
	
	public String getOPGG(Account a) {
		StringBuffer name = new StringBuffer(a.getAccountName().toLowerCase());
		while(name.indexOf(" ") != -1) {
			name.deleteCharAt(name.indexOf(" "));
		}
		if(a.getServer() == Server.EUNE) {
			return "https://eune.op.gg/summoner/userName="+name;
		}
		else {
			return "https://euw.op.gg/summoner/userName="+name;
		}
	}
	
	public String getOPGG(String accountName, Server server) {
		StringBuffer name = new StringBuffer(accountName.toLowerCase());
		while(name.indexOf(" ") != -1) {
			name.deleteCharAt(name.indexOf(" "));
		}
		if(server == Server.EUNE) {
			return "https://eune.op.gg/summoner/userName="+name;
		}
		else {
			return "https://euw.op.gg/summoner/userName="+name;
		}
	}
	
	public String getLeagueOfGraphs(Account a) {
		StringBuffer name = new StringBuffer(a.getAccountName().toLowerCase());
		while(name.indexOf(" ") != -1) {
			name.replace(name.indexOf(" "), name.indexOf(" ")+1, "+");
		}
		if(a.getServer() == Server.EUNE) {
			return "https://www.leagueofgraphs.com/summoner/eune/"+name;
		}
		else {
			return "https://www.leagueofgraphs.com/summoner/euw/"+name;
		}
	}
	
	public String getLeagueOfGraphs(String accountName, Server server) {
		StringBuffer name = new StringBuffer(accountName.toLowerCase());
		while(name.indexOf(" ") != -1) {
			name.replace(name.indexOf(" "), name.indexOf(" ")+1, "+");
		}
		if(server == Server.EUNE) {
			return "https://www.leagueofgraphs.com/summoner/eune/"+name;
		}
		else {
			return "https://www.leagueofgraphs.com/summoner/euw/"+name;
		}
	}
}
