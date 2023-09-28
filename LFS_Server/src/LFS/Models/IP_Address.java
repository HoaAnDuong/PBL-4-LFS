package LFS.Models;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

import org.json.simple.JSONObject;

public class IP_Address {
	private BigInteger id;
	private String ip_address;
	private int max_user_on_ip_address;
	private BigInteger status_id;
	private LocalDateTime last_used;
	private Connection conn;
	
	public BigInteger getId() {
		return this.id;
	}
	public String getIPAddress() throws Exception {
		this.retrieveData(this.id);
		return this.ip_address;
	}
	public int getMaxUserOnIPAddress() throws Exception {
		this.retrieveData(this.id);
		return this.max_user_on_ip_address;
	}
	public IP_Address_Status getStatus() throws Exception {
		this.retrieveData(this.id);
		return new IP_Address_Status(this.status_id,this.conn);
	}
	public LocalDateTime getLastUsed() throws Exception {
		this.retrieveData(this.id);
		return this.last_used;
	}
	public int getNumUser() throws SQLException {
		Statement stmt = this.conn.createStatement();
		String user_count_sql = String.format("SELECT COUNT(*) FROM User_IP_Address \n"
				+ "WHERE ip_address_id == %s",this.id.toString());
		ResultSet rs;
		rs = stmt.executeQuery(user_count_sql);
		return rs.getInt(1);
	}
	public boolean isReachingLimit() throws Exception {
		return this.getNumUser() >= this.getMaxUserOnIPAddress();
	}
	
	public IP_Address(BigInteger id,Connection conn) throws Exception{
		this.conn = conn;
		this.retrieveData(id);
	}
	public IP_Address(String ip_address,Connection conn) throws Exception{
		this.conn = conn;
		this.retrieveData(ip_address);
	}
	
	private void retrieveData(BigInteger id) throws Exception{
		Statement stmt = this.conn.createStatement();
		String get_sql = String.format("SELECT * FROM IP_Address \n"
        		+ "WHERE id == %s;",id.toString());
		ResultSet rs = stmt.executeQuery(get_sql);
		this._assignData(rs);
	}
	private void retrieveData(String ip_address) throws Exception{
		Statement stmt = this.conn.createStatement();
		String get_sql = String.format("SELECT * FROM IP_Address \n"
        		+ "WHERE ip_address == '%s';",ip_address);
		ResultSet rs = stmt.executeQuery(get_sql);
		this._assignData(rs);
	}
	
	private void _assignData(ResultSet rs) throws Exception{
		if(rs.getString("id") == null) throw new SQLException("IP_Address not found");
		this.id = new BigInteger(rs.getString("id"));
		this.ip_address = rs.getString("ip_address");
		this.max_user_on_ip_address = rs.getInt("max_user_on_ip_address");
		this.status_id = new BigInteger(rs.getString("status_id"));
		this.last_used = LocalDateTime.parse(rs.getString("last_used"));
	}
	private void update(String col,String value,boolean isString) throws Exception {
		Statement stmt = conn.createStatement();
		String update_query = String.format("UPDATE IP_Address\n"
				+ "SET %s = " + (isString ? "'" : "") + "%s" + (isString ? "'" : "") + "\n"
				+ "WHERE id == %s",col,value,this.id.toString());
		stmt.execute(update_query);
	}
	
	public static void ensureDatabase(Connection conn) {
		String create_table_sql = "CREATE TABLE IF NOT EXISTS IP_Address (\n"
				+ "id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
				+ "ip_address VARCHAR NOT NULL UNIQUE,\n"
    			+ "max_user_on_ip_address INTEGER,\n"
				+ "status_id INTEGER NOT NULL,\n"
    			+ "last_used DATETIME,\n"
				+ "FOREIGN KEY (status_id) REFERENCES IP_Address_Status(id)\n"
				+");";
		try{
	    	Statement stmt = conn.createStatement();
	    	
	        stmt.execute(create_table_sql);
	    } catch (SQLException e) {

	    }
	}
	public static IP_Address createIPAddress(String ip_address,Connection conn,JSONObject config) throws Exception{
		Statement stmt = conn.createStatement();
		
		IP_Address_Status active_status = new IP_Address_Status("active",conn);
		String max_user_on_ip_address = (String) config.get("max_user_on_ip_address");
		LocalDateTime ldt_now = LocalDateTime.now();
				
		String create_sql = String.format("INSERT INTO IP_Address (ip_address,max_user_on_ip_address,status_id,last_used) \n"
        		+ "VALUES('%s',%s,%s,'%s');",ip_address,max_user_on_ip_address,active_status.getId().toString(),ldt_now.toString());
		//System.out.println(create_sql);
		
		stmt.execute(create_sql);
    	    	
    	IP_Address ipa = new IP_Address(ip_address,conn);
    	return ipa;
	}
}
