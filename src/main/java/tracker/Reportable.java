package tracker;

import java.util.ArrayList;
import java.util.List;

import app.App;

public class Reportable implements Runnable{
	private ArrayList<String> report;
	private String userId;
	private boolean timedOut = false;
	
	public Reportable(List<String> reports, String userId) {
		this.report = (ArrayList<String>) reports;
		this.userId = userId;
	}
	
	@Override
	public void run() {
		try {
			Thread.sleep(600000);
		} catch (InterruptedException e) {e.printStackTrace();}
		this.timedOut = true;
		
	}
	
	public boolean isForUser(String id) {
		return this.userId.equals(id);
	}
	
	public String getName(int number) {
		if(number <= report.size()-1) {
			return report.get(number);
		}
		else {return null;}
	}
	
	public boolean isExpired() {
		return timedOut;
	}


}
