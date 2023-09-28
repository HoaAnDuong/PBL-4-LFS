package LFS.Models;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class User_Shared_File {
	private BigInteger user_id;
	private BigInteger file_id;
	private BigInteger accessibility_id;
	private Connection conn;
	
	public User_Shared_File (BigInteger user_id,BigInteger folder_id,Connection conn) throws Exception{
		this.conn = conn;
		this.retrieveData(user_id,folder_id);
	}
	public User_Shared_File(User user,FileM file,Connection conn) throws Exception{
		this.conn = conn;
		this.retrieveData(user,file);
	}
	public void setAccessibility(Accessibility access) throws Exception {
		this.update("accessibility_id", access.getId().toString(), false);
		this.retrieveData(this.user_id,this.file_id);
	}
	
	public User getUser() throws Exception {
		this.retrieveData(this.user_id, this.file_id);
		return new User(this.user_id,this.conn);
	}
	public FileM getFile() throws Exception {
		this.retrieveData(this.user_id, this.file_id);
		return new FileM(this.file_id,this.conn);
	}
	public Accessibility getAccessibility() throws Exception {
		this.retrieveData(this.user_id, this.file_id);
		return new Accessibility(this.accessibility_id,this.conn);
	}
	
	private void retrieveData(BigInteger user_id,BigInteger file_id) throws Exception{
		Statement stmt = this.conn.createStatement();
		String get_sql = String.format("SELECT * FROM User_Shared_File \n"
        		+ "WHERE user_id == %s AND file_id == %s;",user_id.toString(),file_id.toString());
		ResultSet rs = stmt.executeQuery(get_sql);
		this._assignData(rs);
	}
	private void retrieveData(User user,FileM file) throws Exception{
		Statement stmt = this.conn.createStatement();
		String get_sql = String.format("SELECT * FROM User_Shared_File \n"
        		+ "WHERE user_id == %s AND file_id == %s;",user.getId().toString(),file.getId().toString());
		ResultSet rs = stmt.executeQuery(get_sql);
		this._assignData(rs);
	}
	private void _assignData(ResultSet rs) throws Exception{
		if(rs.getString("user_id") == null) throw new SQLException("User_Shared_File not found");
		this.user_id = new BigInteger(rs.getString("user_id"));
		this.file_id = new BigInteger(rs.getString("file_id"));
		this.accessibility_id = new BigInteger(rs.getString("accessibility_id"));
	}
	
	private void update(String col,String value,boolean isString) throws Exception {
		Statement stmt = conn.createStatement();
		String update_query = String.format("UPDATE User_Shared_File\n"
				+ "SET %s = " + (isString ? "'" : "") + "%s" + (isString ? "'" : "") + "\n"
				+ "WHERE user_id == %s AND file_id == %s",col,value,this.user_id.toString(),this.file_id.toString());
		stmt.execute(update_query);
	}
	public void delete() throws Exception {
		Statement stmt = conn.createStatement();
		String delete_query = String.format("DELETE FROM User_Shared_Folder\n"
				+ "WHERE user_id == %s AND file_id == %s",this.user_id.toString(),this.file_id.toString());
		stmt.execute(delete_query);
		this.user_id = null;
		this.file_id = null;
		this.accessibility_id = null;

	}
	
	public static void ensureDatabase(Connection conn) {
		String create_table_sql = "CREATE TABLE IF NOT EXISTS User_Shared_File (\n"
				+ "user_id INTEGER NOT NULL,\n"
				+ "file_id INTEGER NOT NULL,\n"
				+ "accessibility_id INTEGER NOT NULL,\n"
				+ "FOREIGN KEY (user_id) REFERENCES User(id),\n"
				+ "FOREIGN KEY (file_id) REFERENCES File(id),\n"
				+ "FOREIGN KEY (accessibility_id) REFERENCES Accessibility(id),\n"
    			+ "PRIMARY KEY(user_id,file_id)\n"
    			+ ");";
		try{
	    	Statement stmt = conn.createStatement();
	    	
	        stmt.execute(create_table_sql);
	        
	    } catch (SQLException e) {
	    	System.out.println(create_table_sql);
	        System.out.println(e.getMessage());
	    }
	}
	
	public static User_Shared_File createUserSharedFile(User user, FileM file,Accessibility access,Connection conn) throws Exception {
		Statement stmt = conn.createStatement();
		
		String create_sql = String.format("INSERT INTO User_Shared_File (user_id,file_id,accessibility_id)\n"
										+"VALUES(%s,%s,%s);",
										user.getId().toString(),file.getId().toString(),access.getId().toString());
		
		stmt.execute(create_sql);
		    	
		User_Shared_File usf = new User_Shared_File(user,file,conn);
		return usf;
	}
	
	public static List<User_Shared_File> getAllUserSharedFiles(FileM file,Connection conn) throws Exception{
		List<User_Shared_File> usf_list = new ArrayList<User_Shared_File>();
		
		Statement stmt = conn.createStatement();
		
		String create_sql = String.format("SELECT * FROM User_Shared_File \n"
										+"WHERE file_id == %s;",
										file.getId().toString());
		ResultSet rs = stmt.executeQuery(create_sql);
		while(rs.next()) {
			usf_list.add(new User_Shared_File(new BigInteger(rs.getString("user_id"))
					,new BigInteger(rs.getString("file_id")),conn));
		}
		return usf_list;
	}
}
