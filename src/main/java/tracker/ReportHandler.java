package tracker;

import java.util.ArrayList;
import java.util.List;

import app.App;
import database.DBHandler;
import database.Server;
import database.ReportReason;

public class ReportHandler implements Runnable{
	private List<Reportable> reportable = new ArrayList<Reportable>();
	private DBHandler db = new DBHandler(); 
	private boolean kill;
	
	@Override
	public void run() {
		while(!kill) {
			List<Reportable> rm = new ArrayList<Reportable>();
			for(Reportable r : reportable) {
				if(r.isExpired()) {
					rm.add(r);
				}
			}
			for(Reportable r : rm) {
				reportable.remove(r);
			}
		}
		
	}
	
	public void addReportable(Reportable r) {
		this.reportable.add(r);
		Runnable run = r;
		new Thread(run).start();
	}
	
	//Returns info String for reported players
	public String reportGriefer(List<Integer> numbers, String userId) {
		String re = "";
		Server server = db.findDefaultAccount(userId).getServer();
		for(Reportable r : reportable) {
			if(r.isForUser(userId) && !r.isExpired()) {
				re = "Reported for Griefing: ";
				for(Integer i : numbers) {
					String name = r.getName((i-1));
					db.newReport(name, userId, server, ReportReason.GRIEFER);
					re = re + name + " | ";
				}
				return re;
			}
		}
		return "";
	}
	
	//Returns info String for reported players
	public String reportToxic(List<Integer> numbers, String userId) {
		String re = "";
		Server server = db.findDefaultAccount(userId).getServer();
		for(Reportable r : reportable) {
			if(r.isForUser(userId) && !r.isExpired()) {
				re = "Reported for Toxicity: ";
				for(Integer i : numbers) {
					String name = r.getName((i-1));
					db.newReport(name, userId, server, ReportReason.TOXIC);
					re = re + name + " | ";
				}
				return re;
			}
		}
		return "";
	}
	
	


	
}
