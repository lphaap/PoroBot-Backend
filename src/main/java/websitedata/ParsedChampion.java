package websitedata;

public class ParsedChampion {
	private String name;
	private String role;
	private int games;
	private double wr;
	
	public ParsedChampion(String name, String role, String games, String wr) {
		this.name=name;
		this.role=role;
		this.games = Integer.parseInt(games.replace(" ", "").replace("\n", ""));
		this.wr = Double.parseDouble(wr.replace(" ", "").
											  replace("\n", "").
											  replace("%",""));
	}

	public double getWr() {
		return wr;
	}

	public int getGames() {
		return games;
	}

	public String getRole() {
		return role;
	}

	public String getName() {
		return name;
	}
}

