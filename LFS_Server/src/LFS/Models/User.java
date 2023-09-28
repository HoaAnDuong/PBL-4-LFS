package LFS.Models;

import java.io.File;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

import org.json.simple.JSONObject;

import LFS.Utils.Utils;

public class User {
	private BigInteger id;
	private String username;
	private String password;
	private BigInteger status_id;
	private int max_ip_address_used;
	private BigInteger max_bytes_allocated;
	private LocalDateTime created;
	private LocalDateTime updated;
	private Connection conn;
	private String local_path = null;

	
	public User(BigInteger id,Connection conn) throws Exception{
		this.conn = conn;
		this.retrieveData(id);
	}
	public User(String username,Connection conn) throws Exception{
		this.conn = conn;
		this.retrieveData(username);
	}
	
	public void setLocalPath(String local_path) {
		this.local_path = local_path;
	}
	
	public BigInteger getId() {
		return this.id;
	}
	public String getUsername() throws Exception {
		this.retrieveData(this.id);
		return this.username;
	}
	public int getMaxIPAddressUsed() throws Exception {
		this.retrieveData(this.id);
		return this.max_ip_address_used;
	}
	public User_Status getUserStatus() throws Exception {
		this.retrieveData(this.id);
		return new User_Status(this.status_id,this.conn);
	}
	
	public int getNumIPAddressUsed() throws SQLException {
		Statement stmt = this.conn.createStatement();
		String user_count_sql = String.format("SELECT COUNT(*) FROM User_IP_Address \n"
				+ "WHERE user_id == %s",this.id.toString());
		ResultSet rs;
		rs = stmt.executeQuery(user_count_sql);
		return rs.getInt(1);
	}
	public boolean isReachingLimit() throws Exception {
		return this.getNumIPAddressUsed() >= this.getMaxIPAddressUsed();
	}
	
	public boolean Login(String username,String password) throws Exception {
		this.retrieveData(this.id);
		return (this.username.compareTo(username) == 0) && (this.password.compareTo(password) == 0);
	}
	
	public BigInteger getUsedBytes() throws Exception {
		if(this.local_path == null) throw new Exception("Please set local_path before get Used Bytes using setLocalPath");
		File f = new File(this.local_path + "/" +this.username);
		return Utils.folderSize(f);
	}
	public BigInteger getRemainingBytes() throws Exception {
		if(this.local_path == null) throw new Exception("Please set local_path before get Remaining Bytes using setLocalPath");

		return this.max_bytes_allocated.subtract(this.getUsedBytes());
	}
	
	private void retrieveData(BigInteger id) throws Exception{
		Statement stmt = this.conn.createStatement();
		String get_sql = String.format("SELECT * FROM User \n"
	    		+ "WHERE id == %s;",id.toString());
		ResultSet rs = stmt.executeQuery(get_sql);
		this._assignData(rs);
	}
	private void retrieveData(String username) throws Exception{
		Statement stmt = this.conn.createStatement();
		String get_sql = String.format("SELECT * FROM User\n"
	    		+ "WHERE username == '%s';",username);
		ResultSet rs = stmt.executeQuery(get_sql);
		this._assignData(rs);
	}
	
	private void _assignData(ResultSet rs) throws Exception{
		if(rs.getString("id") == null) throw new SQLException("User not found");
		this.id = new BigInteger(rs.getString("id"));
		this.username = rs.getString("username");
		this.password = rs.getString("password");
		this.status_id = new BigInteger(rs.getString("status_id"));
		this.max_ip_address_used = rs.getInt("max_ip_address_used");
		this.max_bytes_allocated = new BigInteger(rs.getString("max_bytes_allocated"));
		this.created = LocalDateTime.parse(rs.getString("created"));
		this.updated = LocalDateTime.parse(rs.getString("updated"));
	}
	private void update(String col,String value,boolean isString) throws Exception {
		Statement stmt = conn.createStatement();
		String update_query = String.format("UPDATE User\n"
				+ "SET %s = " + (isString ? "'" : "") + "%s" + (isString ? "'" : "") + "\n"
				+ "WHERE id == %s",col,value,this.id.toString());
		stmt.execute(update_query);
	}
	
	public static void ensureDatabase(Connection conn) {
		String create_table_sql = "CREATE TABLE User (\n"
				+ "id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
				+ "username VARCHAR NOT NULL UNIQUE,\n"
				+ "password VARCHAR NOT NULL,\n"
				+ "status_id INTEGER NOT NULL,\n"
				+ "max_ip_address_used INTEGER,\n"
				+ "max_bytes_allocated BIGINT,\n"
				+ "created DATETIME,\n"
				+ "updated DATETIME,\n"
				+ "FOREIGN KEY(status_id) REFERENCES User_Status(id)\n"
				+ ");";
		try{
	    	Statement stmt = conn.createStatement();
	    	
	        stmt.execute(create_table_sql);
	    } catch (SQLException e) {
	    }
	}
	public static User createUser(String username,String password,Connection conn,JSONObject config) throws Exception{
		Statement stmt = conn.createStatement();
		
		User_Status disabled_status = new User_Status("disabled",conn);
		String max_ip_address_used = (String) config.get("max_ip_address_used");
		String max_bytes_allocated = (String) config.get("max_bytes_allocated");
		LocalDateTime ldt_now = LocalDateTime.now();
				
		String create_sql = String.format("INSERT INTO User (username,password,status_id,max_ip_address_used,max_bytes_allocated,created,updated)"
										+"VALUES('%s','%s',%s,%s,%s,'%s','%s')",
										username,password,disabled_status.getId().toString(),max_ip_address_used,max_bytes_allocated,ldt_now.toString(),ldt_now.toString());
		
		stmt.execute(create_sql);
		    	
		User user = new User(username,conn);
		return user;
	}
}