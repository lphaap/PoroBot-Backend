package app;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import com.merakianalytics.orianna.Orianna;
import com.merakianalytics.orianna.types.common.Region;

import database.DBCleaner;
import database.DBHandler;
import database.Delete;
import database.Player;
import database.PlayerChampion;
import database.Report;
import database.ReportReason;
import database.Server;

import discord.MessageProcessor;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Activity.ActivityType;

import tracker.ReportHandler;
import tracker.Reportable;
import websitedata.HTMLParser;
import websitedata.ParsedPlayer;

@SpringBootApplication
public class App implements CommandLineRunner{
	
	public static String webdriverPath;
	public static String databasePath;
	public static boolean cmdSetup = false;
	
    public static void main( String[] args ) throws Exception {
    	SpringApplication.run(App.class, args);
    }
	
	//public static void start(String[] args) throws Exception {
	
	@Override
	public void run(String... args) throws Exception { 
		if(true) {
    		if(args != null && args.length == 2) {
	    		databasePath = args[0];
	    		webdriverPath = args[1];
	    		cmdSetup = true;
	    		
	    		try {//Test database filepath
	    			new DBHandler().defaultAccountExists("1111");
	    		}
	    		catch(Exception e){throw new Exception("No Database found, incorrect file path.");}
	    		
	    		try {//Test webdriver filepath
	    			System.setProperty("webdriver.chrome.driver", App.webdriverPath);
	    			WebDriver driver = new ChromeDriver();
	    			driver.close();
	    		}
	    		catch(Exception e){throw new Exception("No Web-Driver found, incorrect file path.");}
	    		
	    		App.log("CMD SETUP:");
	    		App.log("Chrome-Webdriver path: " + webdriverPath);
	    		App.log("Json-Database path: " + databasePath);
    		}
    		
	    	try {
	    		
				JDA jda = JDABuilder.createDefault("Api key here").build();
				jda.awaitReady();
				jda.addEventListener(new MessageProcessor());
				
				jda.getPresence().setActivity(Activity.of(ActivityType.STREAMING, "|| READY  TO  HELP ! ||" +" Dm me !info to start. ＼(＾O＾)／"));
				
				System.out.println("DISCORD INIT");
			} catch (Exception e1) {e1.printStackTrace();}
	    	
	    	Orianna.setRiotAPIKey("API KEY HERE");
			System.out.println("ORIANNA INIT");
			
			new Thread(new DBCleaner()).start();

    	}
	}
	
	private static void prepSpringTest() {
		DBHandler h = new DBHandler();
		//h.getRawDB().createCollection(Player.class);
		
		PlayerChampion c11 = new PlayerChampion();
		c11.setChamp("g");
		c11.setGames(1);
		c11.setWr(0.5);
		PlayerChampion c12 = new PlayerChampion();
		c12.setChamp("gg");
		c12.setGames(11);
		c12.setWr(0.55);
		PlayerChampion c13 = new PlayerChampion();
		c13.setChamp("ggg");
		c13.setGames(111);
		c13.setWr(0.55);
		
		Player p1 = new Player();
		p1.setC1(c11);
		p1.setC2(c12);
		p1.setC3(c13);
		p1.setId(1);
		p1.setGameId(1111);
		p1.setGames(10);
		p1.setWr(0.5);
		p1.setRole("Tester");
		p1.setName("Player1");
		p1.setNegativeWr(true);
		p1.setOtp(true);
		p1.setToxic(true);
		p1.setGriefer(true);
		
		
		
		h.getRawDB().upsert(p1);
		
	}
	
    
	public static void log(String s) {
		System.out.println(s);
	}   
}

