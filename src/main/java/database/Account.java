package database;

import java.io.Serializable;

import io.jsondb.annotation.Document;
import io.jsondb.annotation.Id;

@Document(collection = "accounts", schemaVersion= "1.0")
public class Account implements Serializable{
	
	//For saving User League account names with server info
	
  @Id
  private String id; 
  private String userId; //Same as User id
  private String accountName;
  private Server server;
  
  private static final long serialVersionUID = 1L;

  public String getId() { return id; }
  public void setId(String id) { this.id = id;}
  
  public String getUserId() { return userId; }
  public void setUserId(String id) { this.userId = id;}
  
  public String getAccountName() { return this.accountName; }
  public void setAccountName(String accountName) { this.accountName = accountName; }
  
  public Server getServer() { return this.server; }
  public void setServer(Server server) { this.server = server; }
  
}
