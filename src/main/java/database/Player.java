package database;

import java.io.Serializable;
import java.util.List;

import io.jsondb.annotation.Document;
import io.jsondb.annotation.Id;

@Document(collection = "players", schemaVersion= "1.0")
public class Player implements Serializable{
	
	//Saves info about game players to be passed onto web controller
	//No encryption is needed since saved info is not confidential in anyway
	
	@Id
	private long id; 
	private long gameId; //Same as game id, used by web-controller
	private String name;
	private String role;
	private int games;
	private double wr;
	private PlayerChampion c1;
	private PlayerChampion c2;
	private PlayerChampion c3;
	private boolean griefer;
	private boolean toxic;
	private boolean negativeWr;
	private boolean otp;
	
	  
	  
	private static final long serialVersionUID = 1L;
	
	public long getId() { return id; }
	public void setId(long id) { this.id = id; }
	
	public long getGameId() { return gameId; }
	public void setGameId(long gameId) { this.gameId = gameId; }
	
	public PlayerChampion getC1() { return c1; }
	public void setC1(PlayerChampion c1) { this.c1 = c1; }
	
	public PlayerChampion getC2() { return c2; }
	public void setC2(PlayerChampion c2) { this.c2 = c2; }
	
	public PlayerChampion getC3() { return c3; }
	public void setC3(PlayerChampion c3) { this.c3 = c3; }
	
	public String getRole() { return role; }
	public void setRole(String role) { this.role = role;}
	
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	
	public int getGames() {return games;}
	public void setGames(int games) {this.games = games;}
	
	public double getWr() {return wr;}
	public void setWr(double wr) {this.wr = wr;}
	
	//Warnings
	public boolean isGriefer() { return griefer; }
	public void setGriefer(boolean griefer) { this.griefer = griefer; }
	public boolean isToxic() { return toxic; }
	public void setToxic(boolean toxic) { this.toxic = toxic; }
	public boolean isNegativeWr() { return negativeWr; }
	public void setNegativeWr(boolean negativeWr) { this.negativeWr = negativeWr; }
	public boolean isOtp() { return otp; }
	public void setOtp(boolean otp) {this.otp = otp;}

	

	
  
  
  
}
