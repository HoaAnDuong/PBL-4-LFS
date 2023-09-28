package LFS.Models;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class User_Shared_Folder {
	private BigInteger user_id;
	private BigInteger folder_id;
	private BigInteger accessibility_id;
	private Connection conn;
	
	public User_Shared_Folder (BigInteger user_id,BigInteger folder_id,Connection conn) throws Exception{
		this.conn = conn;
		this.retrieveData(user_id,folder_id);
	}
	public User_Shared_Folder(User user,Folder folder,Connection conn) throws Exception{
		this.conn = conn;
		this.retrieveData(user,folder);
	}
	
	public void setAccessibility(Accessibility access) throws Exception {
		this.update("accessibility_id", access.getId().toString(), false);
		this.retrieveData(this.user_id,this.folder_id);
	}
	
	public User getUser() throws Exception {
		this.retrieveData(this.user_id, this.folder_id);
		return new User(this.user_id,this.conn);
	}
	public Folder getFolder() throws Exception {
		this.retrieveData(this.user_id, this.folder_id);
		return new Folder(this.folder_id,this.conn);
	}
	public Accessibility getAccessibility() throws Exception {
		this.retrieveData(this.user_id, this.folder_id);
		return new Accessibility(this.accessibility_id,this.conn);
	}
	
	private void retrieveData(BigInteger user_id,BigInteger folder_id) throws Exception{
		Statement stmt = this.conn.createStatement();
		String get_sql = String.format("SELECT * FROM User_Shared_Folder \n"
        		+ "WHERE user_id == %s AND folder_id == %s;",user_id.toString(),folder_id.toString());
		ResultSet rs = stmt.executeQuery(get_sql);
		this._assignData(rs);
	}
	private void retrieveData(User user,Folder folder) throws Exception{
		Statement stmt = this.conn.createStatement();
		String get_sql = String.format("SELECT * FROM User_Shared_Folder \n"
        		+ "WHERE user_id == %s AND folder_id == %s;",user.getId().toString(),folder.getId().toString());
		ResultSet rs = stmt.executeQuery(get_sql);
		this._assignData(rs);
	}
	private void _assignData(ResultSet rs) throws Exception{
		if(rs.getString("user_id") == null) throw new SQLException("User_Shared_Folder not found");
		this.user_id = new BigInteger(rs.getString("user_id"));
		this.folder_id = new BigInteger(rs.getString("folder_id"));
		this.accessibility_id = new BigInteger(rs.getString("accessibility_id"));
	}
	
	public void update(String col,String value,boolean isString) throws Exception {
		Statement stmt = conn.createStatement();
		String update_query = String.format("UPDATE User_Shared_Folder\n"
				+ "SET %s = " + (isString ? "'" : "") + "%s" + (isString ? "'" : "") + "\n"
				+ "WHERE user_id == %s AND folder_id == %s",col,value,this.user_id.toString(),this.folder_id.toString());
		stmt.execute(update_query);
	}
	
	public void delete() throws Exception {
		Statement stmt = conn.createStatement();
		String delete_query = String.format("DELETE FROM User_Shared_Folder\n"
				+ "WHERE user_id == %s AND folder_id == %s",this.user_id.toString(),this.folder_id.toString());
		stmt.execute(delete_query);
		this.user_id = null;
		this.folder_id = null;
		this.accessibility_id = null;

	}
	
	public static void ensureDatabase(Connection conn) {
		String create_table_sql = "CREATE TABLE IF NOT EXISTS User_Shared_Folder (\n"
				+ "user_id INTEGER NOT NULL,\n"
				+ "folder_id INTEGER NOT NULL,\n"
				+ "accessibility_id INTEGER NOT NULL,\n"
				+ "FOREIGN KEY (user_id) REFERENCES User(id),\n"
				+ "FOREIGN KEY (folder_id) REFERENCES Folder(id),\n"
				+ "FOREIGN KEY (accessibility_id) REFERENCES Accessibility(id),\n"
    			+ "PRIMARY KEY(user_id,folder_id)\n"
    			+ ");";
		try{
	    	Statement stmt = conn.createStatement();
	    	
	        stmt.execute(create_table_sql);
	        
	    } catch (SQLException e) {
	    	System.out.println(create_table_sql);
	        System.out.println(e.getMessage());
	    }
	}
	public static User_Shared_Folder createUserSharedFolder(User user, Folder folder,Accessibility access,Connection conn) throws Exception {
		Statement stmt = conn.createStatement();
		
		String create_sql = String.format("INSERT INTO User_Shared_Folder (user_id,folder_id,accessibility_id)\n"
										+"VALUES(%s,%s,%s);",
										user.getId().toString(),folder.getId().toString(),access.getId().toString());
		
		stmt.execute(create_sql);
		    	
		User_Shared_Folder usf = new User_Shared_Folder(user,folder,conn);
		return usf;
	}
	public static List<User_Shared_Folder> getAllUserSharedFolders(Folder folder,Connection conn) throws Exception{
		List<User_Shared_Folder> usf_list = new ArrayList<User_Shared_Folder>();
		
		Statement stmt = conn.createStatement();
		
		String create_sql = String.format("SELECT * FROM User_Shared_Folder \n"
										+"WHERE folder_id == %s;",
										folder.getId().toString());
		ResultSet rs = stmt.executeQuery(create_sql);
		while(rs.next()) {
			usf_list.add(new User_Shared_Folder(new BigInteger(rs.getString("user_id"))
					,new BigInteger(rs.getString("folder_id")),conn));
		}
		return usf_list;
	}
}
