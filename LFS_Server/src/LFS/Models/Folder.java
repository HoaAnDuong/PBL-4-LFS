package LFS.Models;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.simple.JSONObject;

public class Folder{
	private BigInteger id;
	private String path;
	private BigInteger owner_id;
	private BigInteger accessibility_id;
	private LocalDateTime created;
	private LocalDateTime updated;
	private Connection conn;
	private String local_path;

	
	public Folder(BigInteger id,Connection conn) throws Exception{
		this.conn = conn;
		this.retrieveData(id);
	}
	public Folder(User user,String path,Connection conn) throws Exception{
		this.conn = conn;
		this.retrieveData(user,path);
	}
	
	public void setLocalPath(String local_path) {
		this.local_path = local_path;
	}
	public void setAccessibility(Accessibility access) throws Exception {
		this.update("accessibility_id", access.getId().toString(), false);
		this.retrieveData(this.id);
	}
	
	public BigInteger getId() {
		return this.id;
	}
	public String getPath() throws Exception {
		this.retrieveData(this.id);
		return this.path;
	}
	public User getOwner() throws Exception {
		this.retrieveData(this.id);
		return new User(this.owner_id,conn);
	}
	
	public Accessibility getAccessibility() throws Exception {
		this.retrieveData(this.id);
		return new Accessibility(this.accessibility_id,conn);
	}
	
	public Accessibility getSharedAccessibility(User user) throws Exception {
		Accessibility acc = new Accessibility("private",conn);
		
		try {
			if(user != null && this.owner_id.compareTo(user.getId())==0) {
				return new Accessibility("write",this.conn);
			}
			User_Shared_Folder usf = new User_Shared_Folder(user,this,this.conn);
			acc = usf.getAccessibility();
		}catch(Exception e) {
			
		}
		return acc;
	}
	
	public Accessibility getMaxAccessibility(User user) throws Exception {
		HashMap<String,BigInteger> acc_priority = Accessibility.getAccessibilityPriority();
		String acc = this.getAccessibility().getCode();
		
		if(user != null && this.owner_id.compareTo(user.getId())==0) {
			if(acc_priority.get("write").compareTo(acc_priority.get(acc)) == 1) {
				acc = "write";
				return new Accessibility(acc,this.conn);
			}
		}
		
		String[] splitted = this.path.split("/");
		String folder_path = "";
		for(int i = 0;i<splitted.length;i++) {
			folder_path += (i>0 ? "/" : "") + splitted[i];
			try {
				Folder f = new Folder(this.getOwner(),folder_path,this.conn);
				String f_acc = f.getAccessibility().getCode();
				if(acc_priority.get(f_acc).compareTo(acc_priority.get(acc)) == 1) {
					acc = f_acc;
				}
				if(user!=null) {
					String f_shared_acc = f.getSharedAccessibility(user).getCode();
					if(acc_priority.get(f_shared_acc).compareTo(acc_priority.get(acc)) == 1) {
						acc = f_shared_acc;
					}
				}
				
			}catch(Exception e){
				
			}
		}
		return new Accessibility(acc,this.conn);
	}

	private void retrieveData(BigInteger id) throws Exception{
		Statement stmt = this.conn.createStatement();
		String get_sql = String.format("SELECT * FROM Folder \n"
	    		+ "WHERE id == %s;",id.toString());
		ResultSet rs = stmt.executeQuery(get_sql);
		this._assignData(rs);
	}
	private void retrieveData(User user,String path) throws Exception{
		Statement stmt = this.conn.createStatement();
		String get_sql = String.format("SELECT * FROM Folder \n"
	    		+ "WHERE path == '%s' AND owner_id == %s;",path,user.getId().toString());
		ResultSet rs = stmt.executeQuery(get_sql);
		this._assignData(rs);
	}
	
	private void _assignData(ResultSet rs) throws Exception{
		if(rs.getString("id") == null) throw new SQLException("Folder not found");
		this.id = new BigInteger(rs.getString("id"));
		this.path = rs.getString("path");
		this.owner_id = new BigInteger(rs.getString("owner_id"));
		this.accessibility_id = new BigInteger(rs.getString("accessibility_id"));
		this.created = LocalDateTime.parse(rs.getString("created"));
		this.updated = LocalDateTime.parse(rs.getString("updated"));
	}
	public void update(String col,String value,boolean isString) throws Exception {
		Statement stmt = conn.createStatement();
		String update_query = String.format("UPDATE Folder\n"
				+ "SET %s = " + (isString ? "'" : "") + "%s" + (isString ? "'" : "") + "\n"
				+ "WHERE id == %s",col,value,this.id.toString());
		stmt.execute(update_query);
	}
	public void delete() throws Exception {
		Statement stmt = conn.createStatement();
		String delete_query = String.format("DELETE FROM Folder\n"
				+ "WHERE id == %s",this.id.toString());
		stmt.execute(delete_query);
		this.id = null;
		this.path = null;
		this.owner_id = null;
		this.accessibility_id = null;
		this.created = null;
		this.updated = null;
	}
	
	public static void ensureDatabase(Connection conn) {
		String create_table_sql = "CREATE TABLE Folder (\n"
				+ "id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
				+ "path VARCHAR,\n"
				+ "owner_id INTEGER NOT NULL,\n"
				+ "accessibility_id INTEGER NOT NULL,\n"
				+ "created DATETIME,\n"
				+ "updated DATETIME,\n"
				+ "FOREIGN KEY(owner_id) REFERENCES User(id),\n"
				+ "FOREIGN KEY(accessibility_id) REFERENCES Accessibility(id)\n"
				+ ");";
		try{
	    	Statement stmt = conn.createStatement();
	    	
	        stmt.execute(create_table_sql);
	    } catch (SQLException e) {

	    }
	}
	public static Folder createFolder(User user, String path,Connection conn) throws Exception{
		Statement stmt = conn.createStatement();
		
		Accessibility private_acc = new Accessibility("private",conn);
		LocalDateTime ldt_now = LocalDateTime.now();
				
		String create_sql = String.format("INSERT INTO Folder (path,owner_id,accessibility_id,created,updated)"
										+"VALUES('%s',%s,%s,'%s','%s')",
										path,user.getId().toString(),private_acc.getId().toString(),ldt_now.toString(),ldt_now.toString());
		//System.out.println(create_sql);
		
		stmt.execute(create_sql);
		    	
		Folder folder = new Folder(user,path,conn);
		return folder;
	}
	public static Accessibility getFolderAccessibility(User user, User owner,String folder_path,Connection conn) throws Exception {
		HashMap<String,BigInteger> acc_priority = Accessibility.getAccessibilityPriority();
		
		if(user.getId().compareTo(owner.getId())==0) {
			return new Accessibility("write",conn);
		}
		
		String acc = "private";
		String[] splitted = folder_path.split("/");
		
		for(int i = splitted.length - 1;i>=0;i--) {
			String f_path = "";
			for(int j = 0;j<=i;j++) {
				f_path += (j > 0 ? "/" : "") + splitted[j];
			}
			try {
				Folder f =  new Folder(owner,f_path,conn);
				String f_acc = f.getMaxAccessibility(user).getCode();
				if(acc_priority.get(f_acc).compareTo(acc_priority.get(acc)) == 1) {
					acc = f_acc;
				}
				
			}catch(Exception e) {
				
			}
			if (acc == "write") return new Accessibility(acc,conn);
		}
		return new Accessibility(acc,conn);
	}
	public static List<Folder> getSubFolders(User owner,String path, Connection conn) throws Exception {
		List<Folder> folders = new ArrayList<Folder>();
		Statement stmt = conn.createStatement();
		String get_folders_sql = String.format("SELECT * FROM Folder\n"
				+ "WHERE (path LIKE '%s/%%' OR path == '%s') AND owner_id == %s",path,path,owner.getId().toString());
		ResultSet rs = stmt.executeQuery(get_folders_sql);
		while(rs.next()){
			Folder f = new Folder(new BigInteger(rs.getString("id")),conn);
			folders.add(f);
		}
		
		return folders;
	}
}