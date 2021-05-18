package discord;

import database.User;
import net.dv8tion.jda.api.entities.MessageChannel;


public class MessageUtility {
	private MessageChannel dms;
	private User user;
	
	public MessageUtility(MessageChannel dmChannel, User user) {
		this.dms = dmChannel;
		this.user = user;
	}
	
	public void sendMessage(String msg) {
		dms.sendMessage("\r\n" + msg).queue();
	}
	
	public void sendMessageAtUser(String msg) {
		dms.sendMessage( "\r\n" + getUserAt() + "\r\n" + msg).queue();
	}
	
	public String getUserName() {
		return this.user.getUsername();
	}
	
	public String getUserAt() {
		return "<@" + user.getId() + ">";
	}
}
