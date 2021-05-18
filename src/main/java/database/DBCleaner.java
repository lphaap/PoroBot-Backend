package database;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import app.App;

public class DBCleaner implements Runnable {
	DBHandler db = new DBHandler();
	
	@Override
	public void run() {
		App.log("CLEANER INIT");
		
		while(true) {
			LocalDateTime current = LocalDateTime.now();
			List<Delete> deletes = db.findAllDeletes();
			for(Delete d : deletes) {
				if(ChronoUnit.HOURS.between(d.getTime(), current) >= 10) {//Remove game data after 10hours
					App.log("Removing Game: " + d.getId());
					db.removeAllDeletesByGameId(d.getId());
					db.removeAllPlayersByGameId(d.getId());
				}
			}
			
			try {
				Thread.sleep(1000 * 60 * 10); //Sleep 10 mins
			} catch (InterruptedException e) {}
			
		}

	}

}
