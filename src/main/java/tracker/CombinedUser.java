package tracker;

import database.User;
import discord.MessageUtility;
import net.dv8tion.jda.api.entities.MessageChannel;

//Combine user-data, MessageUtility and TrackerUtility functionalities
public class CombinedUser {
	private MessageUtility msgUtil;
	private TrackerUtility trackUtil;
	private User user;
	
	public CombinedUser(User u) {
		this.user = u;
	}
	
	public CombinedUser(User u, MessageUtility msgUtil) {
		this.msgUtil = msgUtil;
		this.user = u;
	}
	
	public CombinedUser(User u, MessageUtility msgUtil, TrackerUtility trackUtil) {
		this.msgUtil = msgUtil;
		this.trackUtil = trackUtil;
		this.user = u;
	}
	
	public void setMsgUtility(MessageChannel channel) {
		this.msgUtil = new MessageUtility(channel,this.user);
	}
	
	public void setTrackerUtility(TrackerUtility tracker) {
		this.trackUtil = tracker;
	}
	
	public MessageUtility getMsgUtility() {
		return this.msgUtil;
	}
	
	public TrackerUtility getTrackerUtility() {
		return this.trackUtil;
	}
	
	public User getUserData() {
		return this.user;
	}
	
	public boolean isMessageable() {
		if(msgUtil != null) {return true;}
		else {return false;}
	}
	
	public boolean isTrackable() {
		if(trackUtil != null) {return true;}
		else {return false;}
	}
}
