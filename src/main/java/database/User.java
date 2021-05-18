package database;

import java.io.Serializable;

import io.jsondb.annotation.Document;
import io.jsondb.annotation.Id;

@Document(collection = "users", schemaVersion= "1.0")
public class User implements Serializable{
	
	//Saves needed info about bot users to DB
	//No encryption is needed since saved info is not confidential in anyway
	
  @Id
  private String id;
  private String username;
  private String defaultAccountId;
  
  private static final long serialVersionUID = 1L;

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  
  public String getUsername() { return username; }
  public void setUsername(String username) { this.username = username; }
  
  public String getDefaultAccountId() { return this.defaultAccountId; }
  public void setDefaultAccountId(String id) { this.defaultAccountId = id; }
  
}