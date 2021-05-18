package database;

import java.io.Serializable;

import io.jsondb.annotation.Document;
import io.jsondb.annotation.Id;

@Document(collection = "reports", schemaVersion= "1.0")
public class Report implements Serializable{
	
	//Saves reported accounts with reason and reporter info
	//No encryption is needed since saved info is not confidential in anyway
	
  @Id
  private String id; 
  private String reporterId; //Same as User id
  private String accountName;
  private Server server;
  private ReportReason reason;
  
  private static final long serialVersionUID = 1L;

  public String getId() { return id; }
  public void setId(String id) { this.id = id;}
  
  public String getReporterId() { return reporterId; }
  public void setReporterId(String id) { this.reporterId = id;}
  
  public String getAccountName() { return this.accountName; }
  public void setAccountName(String accountName) { this.accountName = accountName; }
  
  public Server getServer() { return this.server; }
  public void setServer(Server server) { this.server = server; }
  
  public ReportReason getReason() { return this.reason; }
  public void setReason(ReportReason reason) { this.reason = reason; }
  
}
