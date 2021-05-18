package database;

import java.io.Serializable;

public class PlayerChampion implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String champ;
	private double wr;
	private int games;
	
	public String getChamp() {
		return champ;
	}
	public void setChamp(String champ) {
		this.champ = champ;
	}
	
	public double getWr() {
		return wr;
	}
	public void setWr(double wr) {
		this.wr = wr;
	}
	
	public int getGames() {
		return games;
	}
	public void setGames(int games) {
		this.games = games;
	}
}
