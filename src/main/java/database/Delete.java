package database;

import java.io.Serializable;
import java.time.LocalDateTime;

import io.jsondb.annotation.Document;
import io.jsondb.annotation.Id;

@Document(collection = "deletes", schemaVersion= "1.0")
public class Delete implements Serializable{
	private static final long serialVersionUID = 1L;
	
	@Id
	private long id; 
	private LocalDateTime time;
	  
	public long getId() { return id; }
	public void setId(long id) { this.id = id;}
	  
	public LocalDateTime getTime() {return time;}
	public void setTime(LocalDateTime time) {this.time = time;}
}
