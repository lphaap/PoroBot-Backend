package websitedata;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.*;

import app.App;
import database.Server;

public class HTMLParser {
	
	public HTMLParser() {
		
		if(App.cmdSetup) {
			System.setProperty("webdriver.chrome.driver", App.webdriverPath);
		}
		else {
			System.setProperty("webdriver.chrome.driver","G:/Databases/PoroDB/WebDriver/chromedriver.exe");
		}
	}
	
	
	/**
	 * Returns ParsedPlayer for a list of summonerNames
	 * Parses the result from https://www.leagueofgraphs.com, saving queries from riot api
	 * @.pre summonerName != null && championName != null
	 * Return a list with [0]
	 */
	public List<ParsedPlayer> rankedDataQueue(List<String> names, Server region){
		ArrayList<Thread> parsers = new ArrayList<Thread>();
		ArrayList<ParsedPlayer> players = new ArrayList<ParsedPlayer>();
		
		for(String s : names) {
			Thread t = new Thread(() -> {
				ParsedPlayer p = rankedData(s,region);
				if(p != null) {players.add(p);}
			});
			parsers.add(t);
			t.start();
		}
		
		for(Thread t : parsers) {
			try {t.join();} catch (Exception e) {}
		}
		
		return players;
	}
	
	
	/**
	 * Returns ParsedPlayer for summonerName
	 * Parses the result from https://www.leagueofgraphs.com, saving queries from riot api
	 * @.pre summonerName != null && championName != null
	 * Return a list with [0]
	 */
	public ParsedPlayer rankedData(String summonerName, Server region){
		String server = "";
		if(region == Server.EUNE) {
			server = "eune";
		}
		else if(region == Server.EUW) {
			server = "euw";
		}
		
		String parsedName = summonerName.replaceAll(" ", "%20");
		//https://app.mobalytics.gg/lol/profile/euw/lec%20prodigy/overview
		String url = "https://app.mobalytics.gg/lol/profile/" + server + "/" + parsedName + "/champion-pool";
		
		WebDriver driver = new ChromeDriver();
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS); //Create driver
		
		driver.get(url); //Get page
		 
		try {
			new WebDriverWait(driver, 5).until(e -> e.findElement(By.className("css-njbp03"))); //Wait for page load
		}
		catch(org.openqa.selenium.TimeoutException e) {driver.close(); /*App.log("ERROR");*/ return null;} //If player has no games
		
		
	    String html = driver.getPageSource(); //Get raw HTML
	    
	    driver.close();
	    
		Document doc = Jsoup.parse(html); //Give HTML to Jsoup
		
		//Get Summoners main role
		String mainRole = StringUtils.substringBetween(
				doc.select("#root > div.css-1xqs8p0.e162etri0 > div.css-1i2n825.eyp6zez0 >"
	    		+ " div.css-ci93q4.eyp6zez1 > div > main > div >"
	    		+ " div.e5ge7yp0.css-1gne1qe.e17biqec0 > div.css-9whsf3.e5ge7yp1 >"
	    		+ " div > div > div.css-wwjcud.ekcu6111 > div.css-njbp03.e2l7c7j0 >"
	    		+ " div.css-hl01za.e2l7c7j5 > div > div > div.exa81eq0.css-1mi6gbi.e1fyjdpd1 >"
	    		+ " div > div > div.css-dnucvo.edb6c323").toString(), ">\n ", "\n<");
		
		//Get Summoners ranked wr
		String wr = StringUtils.substringBetween(
				doc.select("#root > div.css-1xqs8p0.e162etri0 > div.css-1i2n825.eyp6zez0 >"
				+ " div.css-ci93q4.eyp6zez1 > div > main > div.css-ht4nkg >"
				+ " div.e5ge7yp0.css-1gne1qe.e17biqec0 > div:nth-child(1) >"
				+ " div > div.css-248241.e8sp88e2 > div:nth-child(1) >"
				+ " div.css-1z03o3k.e1w1l6i81 > div.css-16w6udq.e1w1l6i85 >"
				+ " span > span:nth-child(2)").toString(), ">", "<");
		
		
		//Get Summoners games
		String rawGames = StringUtils.substringBetween(
				doc.select("#root > div.css-1xqs8p0.e162etri0 > div.css-1i2n825.eyp6zez0 >"
				+ " div.css-ci93q4.eyp6zez1 > div > main > div.css-ht4nkg >"
				+ " div.e5ge7yp0.css-1gne1qe.e17biqec0 > div:nth-child(1) >"
				+ " div > div.css-248241.e8sp88e2 > div:nth-child(1) >"
				+ " div.css-1z03o3k.e1w1l6i81 > div.css-16w6udq.e1w1l6i85 > span").toString(), ">", "<");
		String games = ""
				+ (Integer.parseInt(StringUtils.substringBetween(rawGames, "","&"))
				+ Integer.parseInt(StringUtils.substringBetween(rawGames, "W&nbsp;","&nbsp;L")));
		
		
		ArrayList<ParsedChampion> cList = new ArrayList<ParsedChampion>(); 
		
		//Parse Summoners Top 1 champion data
		Elements c1 = doc.select("#root > div.css-1xqs8p0.e162etri0 > div.css-1i2n825.eyp6zez0 >"
				+ " div.css-ci93q4.eyp6zez1 > div > main > div.css-ht4nkg >"
				+ " div.css-15n39th.ekunq820 > div:nth-child(1) > table > tbody:nth-child(3)");
		
		String champOne = StringUtils.substringBetween(
				c1.select("tr > td:nth-child(3) > div > div > div").toString(), ">\n ", "\n<");
		
		String champOneGames = StringUtils.substringBetween(
				c1.select("tr > td:nth-child(5) > div").toString(), ">\n ", "\n<");
		
		String champOneWR = StringUtils.substringBetween(
				c1.select("tr > td:nth-child(6) > div > span").toString(), ">", "<");
		
		String champOneRole = StringUtils.substringBetween(
				c1.select("img").toString(), "alt=\"", "\"");
		
		cList.add(new ParsedChampion(champOne,champOneRole,champOneGames,champOneWR));
		
		
		//Parse Summoners Top 2 champion data
		Elements c2 = doc.select("#root > div.css-1xqs8p0.e162etri0 > div.css-1i2n825.eyp6zez0 >"
				+ " div.css-ci93q4.eyp6zez1 > div > main > div.css-ht4nkg >"
				+ " div.css-15n39th.ekunq820 > div:nth-child(1) > table > tbody:nth-child(5)");
		
		String champTwo = StringUtils.substringBetween(
				c2.select("tr > td:nth-child(3) > div > div > div").toString(), ">\n ", "\n<");
		
		String champTwoGames = StringUtils.substringBetween(
				c2.select("tr > td:nth-child(5) > div").toString(), ">\n ", "\n<");
		
		String champTwoWR = StringUtils.substringBetween(
				c2.select("tr > td:nth-child(6) > div > span").toString(), ">", "<");
		
		String champTwoRole = StringUtils.substringBetween(
				c2.select("img").toString(), "alt=\"", "\"");
		
		cList.add(new ParsedChampion(champTwo,champTwoRole,champTwoGames,champTwoWR));
		
		//Parse Summoners Top 3 champion data
		Elements c3 = doc.select("#root > div.css-1xqs8p0.e162etri0 > div.css-1i2n825.eyp6zez0 >"
				+ " div.css-ci93q4.eyp6zez1 > div > main > div.css-ht4nkg >"
				+ " div.css-15n39th.ekunq820 > div:nth-child(1) > table > tbody:nth-child(7)");
		
		String champThree = StringUtils.substringBetween(
				c3.select("tr > td:nth-child(3) > div > div > div").toString(), ">\n ", "\n<");
		
		String champThreeGames = StringUtils.substringBetween(
				c3.select("tr > td:nth-child(5) > div").toString(), ">\n ", "\n<");
		
		String champThreeWR = StringUtils.substringBetween(
				c3.select("tr > td:nth-child(6) > div > span").toString(), ">", "<");
		
		String champThreeRole = StringUtils.substringBetween(
				c3.select("img").toString(), "alt=\"", "\"");
		
		cList.add(new ParsedChampion(champThree,champThreeRole,champThreeGames,champThreeWR));
		
		/*
		 * Logs
		 * 
		 
		App.log("");
		
		App.log("-----" +summonerName+ "-----");
		
		App.log("URL: " + url); 
		
		App.log("Main Role: " + mainRole);
		
		App.log("Winrate: " + wr);
		
		App.log("Games: " + games);
		
		App.log("Champ 1: " + champOne + " | Games: " + champOneGames + 
				" | Wr: " + champOneWR + " | Role: " + champOneRole);

		App.log("Champ 2: " + champTwo + " | Games: " + champTwoGames + 
				" | Wr: " + champTwoWR + " | Role: " + champTwoRole);
		
		App.log("Champ 3: " + champThree + " | Games: " + champThreeGames + 
				" | Wr: " + champThreeWR + " | Role: " + champThreeRole);

		App.log("-----" +summonerName+ "-----");
		*/

		return new ParsedPlayer(summonerName, mainRole, games, wr, cList, region);
	}
	
	
	/**
	 * Returns the number of ranked games for summoner name
	 * Parses the result from https://www.leagueofgraphs.com, saving queries from riot api
	 * @.pre summonerName != null && championName != null
	 */
	public Double rankedWr(String summonerName, Server region){
		String server = "";
		if(region == Server.EUNE) {
			server = "eune";
		}
		else if(region == Server.EUW) {
			server = "euw";
		}
		
		String parsedName = summonerName.replaceAll(" ", "+");
		
		String html = "https://www.leagueofgraphs.com/summoner/"+server+"/"+parsedName+"#championsData-ranked";
		Document doc = this.getJsoupDoc(html);
		
		Elements e = doc.select("#graphDD4");
		Elements eb = doc.select("#graphDD5");
		
		if(eb.toString().contains("%")) {
			return Double.parseDouble(StringUtils.substringBetween(eb.toString(), "  ", " ").replace("%", ""));
		}
		else {
			return Double.parseDouble(StringUtils.substringBetween(e.toString(), "  ", " ").replace("%", ""));
		}
		
	}

	
	/**
	 * Returns the number of ranked games for summoner name
	 * Parses the result from https://www.leagueofgraphs.com, saving queries from riot api
	 * @.pre summonerName != null && championName != null
	 */
	public int rankedGamesPlayed(String summonerName, Server region){
		String server = "";
		if(region == Server.EUNE) {
			server = "eune";
		}
		else if(region == Server.EUW) {
			server = "euw";
		}
		
		String parsedName = summonerName.replaceAll(" ", "+");
		
		String html = "https://www.leagueofgraphs.com/summoner/"+server+"/"+parsedName+"#championsData-ranked";
		Document doc = this.getJsoupDoc(html);
		
		Elements e = doc.select("#graphDD3");
		Elements eb = doc.select("#graphDD4");
		
		String result = StringUtils.substringBetween(e.toString(), "  ", " ");
		if(!e.toString().contains("%")) {
			return Integer.parseInt(StringUtils.substringBetween(e.toString(), "  ", " ").replace("%", ""));
		}
		else {
			return Integer.parseInt(StringUtils.substringBetween(eb.toString(), "  ", " ").replace("%", ""));
		}
	}
	
	
	/**
	 * Returns the top3 played champs (games+wr) for summoner name
	 * Parses the result from https://www.leagueofgraphs.com, saving queries from riot api
	 * @.pre summonerName != null && championName != null
	 */
	public List<String> getTop3Champs(String summonerName, Server region){
		List<String> re = new ArrayList<String>();
		
		String server = "";
		if(region == Server.EUNE) {
			server = "eune";
		}
		else if(region == Server.EUW) {
			server = "euw";
		}
		
		String parsedName = summonerName.replaceAll(" ", "+");
		
		String html = "https://www.leagueofgraphs.com/summoner/champions/"+server+"/" + parsedName + "#championsData-soloqueue";
		
		Document doc = this.getJsoupDoc(html);
		
		int index = 2;
		while(true) {
			Elements es = doc.select("#mainContent > "
				+ "div.row.summoner_champions_details_table_container > "
				+ "div > div > div.box.box-padding-10-5.tabs-content > "
				+ "div.content.active > div > table > tbody > tr:nth-child("+ index+ ")");
		
			if(es.toString().equals("") || index==5) {break;} //Breaks loop if empty
			
			String name = StringUtils.substringBetween(es.select("span.name").first().toString(), "> "," <");
			String wr = ""+ this.round(Double.parseDouble(
						StringUtils.substringBetween(es.select("progressbar").
						last().toString(), "data-value=\"", "\"")), 2);
			String games = StringUtils.substringBetween(es.select("progressbar").first().toString(), "data-value=\"", "\"");
			
			re.add(name + ": " + games + " Games & " + wr + "% Wr");
			
			index++;
		}
		
		return re;
	}
	
	/**
	 * Returns all champs (games+wr) for summoner name
	 * Parses the result from https://www.leagueofgraphs.com, saving queries from riot api
	 * @.pre summonerName != null && championName != null
	 */
	public List<String> getAllChamps(String summonerName, Server region){
		List<String> re = new ArrayList<String>();
		
		String server = "";
		if(region == Server.EUNE) {
			server = "eune";
		}
		else if(region == Server.EUW) {
			server = "euw";
		}
		
		String parsedName = summonerName.replaceAll(" ", "+");
		
		String html = "https://www.leagueofgraphs.com/summoner/champions/"+server+"/" + parsedName + "#championsData-soloqueue";
		
		Document doc = this.getJsoupDoc(html);
		
		int index = 2;
		while(true) {
			Elements es = doc.select("#mainContent > "
				+ "div.row.summoner_champions_details_table_container > "
				+ "div > div > div.box.box-padding-10-5.tabs-content > "
				+ "div.content.active > div > table > tbody > tr:nth-child("+ index+ ")");
		
			if(es.toString().equals("")) {break;} //Breaks loop if empty
			
			String name = StringUtils.substringBetween(es.select("span.name").first().toString(), "> "," <");
			String wr = ""+ this.round(Double.parseDouble(
						StringUtils.substringBetween(es.select("progressbar").
						last().toString(), "data-value=\"", "\"")), 2);
			String games = StringUtils.substringBetween(es.select("progressbar").first().toString(), "data-value=\"", "\"");
			
			re.add(name + ": " + games + " Games & " + wr + "% Wr");
			
			index++;
		}
		
		return re;
	}
	
	/**
	 * Returns the winrate for summoner name and champion
	 * Parses the result from https://www.leagueofgraphs.com, saving queries from riot api
	 * @.pre summonerName != null && championName != null
	 */
	public double getWinRatio(String summonerName, String championName, Server region) {
		String server = "";
		if(region == Server.EUNE) {
			server = "eune";
		}
		else if(region == Server.EUW) {
			server = "euw";
		}
		
		String parsedName = summonerName.replaceAll(" ", "+");
		
		String parsedChampion = championName.replaceAll(" ", "").toLowerCase();//.replace("'", "");
		if(parsedChampion.toLowerCase().contains("nunu")) {
			parsedChampion = "nunu";
		}
		
		String html = "https://www.leagueofgraphs.com/summoner/champions/"+server+"/" + parsedName + "#championsData-soloqueue";
		
		Document doc = this.getJsoupDoc(html);
		
		int index = 2;
		while(true) {
			Elements es = doc.select("#mainContent > "
				+ "div.row.summoner_champions_details_table_container > "
				+ "div > div > div.box.box-padding-10-5.tabs-content > "
				+ "div.content.active > div > table > tbody > tr:nth-child("+ index+ ")");
		
			if(es.toString().equals("")) {return 0;} //Breaks loop if empty
			
			String name = StringUtils.substringBetween(es.select("span.name").first().toString(), "> "," <");
			if(name.toLowerCase().contains(parsedChampion.toLowerCase())) {
				return Double.parseDouble(StringUtils.substringBetween(es.select("progressbar").
										last().toString(), "data-value=\"", "\""));
			}
			else {index++;}
		}
	  }
	
	
	/**
	 * Returns ranked games played for given summoner and champion
	 * Parses the result from https://www.leagueofgraphs.com, saving queries from riot api
	 * @.pre summonerName != null && championName != null
	 */
	public int getGamesPlayed(String summonerName, String championName, Server region) {
		String server = "";
		if(region == Server.EUNE) {
			server = "eune";
		}
		else if(region == Server.EUW) {
			server = "euw";
		}
		
		String parsedName = summonerName.replaceAll(" ", "+");
		
		String parsedChampion = championName.replaceAll(" ", "").toLowerCase();//.replace("'", "");
		if(parsedChampion.toLowerCase().contains("nunu")) {
			parsedChampion = "nunu";
		}
		
		String html = "https://www.leagueofgraphs.com/summoner/champions/"+server+"/" + parsedName + "#championsData-soloqueue";
		
		Document doc = this.getJsoupDoc(html);
		
		int index = 2;
		while(true) {
			Elements es = doc.select("#mainContent > "
				+ "div.row.summoner_champions_details_table_container > "
				+ "div > div > div.box.box-padding-10-5.tabs-content > "
				+ "div.content.active > div > table > tbody > tr:nth-child("+ index+ ")");
		
			if(es.toString().equals("")) {return 0;} //Breaks loop if empty
			
			String name = StringUtils.substringBetween(es.select("span.name").first().toString(), "> "," <");
			if(name.toLowerCase().contains(parsedChampion.toLowerCase())) {
				return Integer.parseInt(StringUtils.substringBetween(es.select("progressbar").
										first().toString(), "data-value=\"", "\""));
			}
			else {index++;}
		}
	}
	
	public Document getJsoupDoc(String url) {
		Document doc = null;
		try {
			doc = Jsoup.connect(url)
					.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
								+ "AppleWebKit/537.36 (KHTML, like Gecko) "
								+ "Chrome/90.0.4430.85 Safari/537.36")
					.referrer("https://www.leagueofgraphs.com/").
					timeout(30000).ignoreHttpErrors(true).followRedirects(true).get();
		} catch (IOException e) { e.printStackTrace(); }
		
		return doc;
	}
	
	public double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = BigDecimal.valueOf(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
	
		
		
	
}
