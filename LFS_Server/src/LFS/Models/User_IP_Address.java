package LFS.Models;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class User_IP_Address {
	private BigInteger user_id;
	private BigInteger ip_address_id;
	private Connection conn;
	
	public User getUser() throws Exception{
		return new User(this.user_id,this.conn);
	}
	public IP_Address getIPAddress(Connection conn) throws Exception{
		return new IP_Address(this.ip_address_id,this.conn);
	}
	
	public User_IP_Address(BigInteger user_id,BigInteger ipa_id,Connection conn) throws Exception{
		this.conn = conn;
		this.retrieveData(user_id,ipa_id);
	}
	public User_IP_Address(User user,IP_Address ipa,Connection conn) throws Exception{
		this.conn = conn;
		this.retrieveData(user,ipa);
	}
	private void retrieveData(BigInteger user_id,BigInteger ipa_id) throws Exception{
		Statement stmt = this.conn.createStatement();
		String get_sql = String.format("SELECT * FROM User_IP_Address \n"
        		+ "WHERE user_id == %s AND ip_address_id == %s;",user_id.toString(),ipa_id.toString());
		ResultSet rs = stmt.executeQuery(get_sql);
		if (rs.getString("user_id") == null) {
			createUserIPAddress(user_id,ipa_id,this.conn);
			rs = stmt.executeQuery(get_sql);
		};
		this._assignData(rs);
	}
	private void retrieveData(User user,IP_Address ipa) throws Exception{
		Statement stmt = this.conn.createStatement();
		String get_sql = String.format("SELECT * FROM User_IP_Address \n"
        		+ "WHERE user_id == %s AND ip_address_id == %s;",user.getId().toString(),ipa.getId().toString());
		ResultSet rs = stmt.executeQuery(get_sql);
		if (rs.getString("user_id") == null) {
			createUserIPAddress(user,ipa,this.conn);
			rs = stmt.executeQuery(get_sql);
		};
		this._assignData(rs);
	}
	
	private void _assignData(ResultSet rs) throws Exception{
		if(rs.getString("user_id") == null) throw new SQLException("User_IP_Address_Status not found");
		this.user_id = new BigInteger(rs.getString("user_id"));
		this.ip_address_id = new BigInteger(rs.getString("ip_address_id"));
	}
	
	public static User_IP_Address createUserIPAddress(BigInteger user_id,BigInteger ipa_id,Connection conn) throws Exception {
		return createUserIPAddress(new User(user_id,conn),new IP_Address(ipa_id,conn),conn);
	}
	
	public static User_IP_Address createUserIPAddress(User user,IP_Address ipa,Connection conn) throws Exception {
		if(user.isReachingLimit()) throw new Exception(String.format("Nguoi dung %s da dat gioi han ve so Dia chi IP duoc su dung. Lien he quan tri vien de gia han.", user.getUsername()));
		if(ipa.isReachingLimit()) throw new Exception(String.format("Dia chi IP %s da dat gioi han ve so Tai khoan duoc su dung. Lien he quan tri vien de gia han.", ipa.getIPAddress()));
		Statement stmt = conn.createStatement();
		String create_sql = String.format("INSERT INTO User_IP_Address(user_id,ip_address_id) \n"
        		+ "VALUES(%s,%s);",user.getId().toString(),ipa.getId().toString());
    	stmt.execute(create_sql);
    	
    	User_IP_Address uipa = new User_IP_Address(user,ipa,conn);
    	return uipa;
	}
	
	public static void ensureDatabase(Connection conn) {
		String create_table_sql = "CREATE TABLE IF NOT EXISTS User_IP_Address (\n"
				+ "user_id INTEGER NOT NULL,\n"
				+ "ip_address_id INTEGER NOT NULL,\n"
				+ "FOREIGN KEY (user_id) REFERENCES User(id),\n"
				+ "FOREIGN KEY (ip_address_id) REFERENCES IP_Address(id),\n"
    			+ "PRIMARY KEY(user_id,ip_address_id)\n"
    			+ ");";
		try{
	    	Statement stmt = conn.createStatement();
	    	
	        stmt.execute(create_table_sql);
	        
	    } catch (SQLException e) {
	    	System.out.println(create_table_sql);
	        System.out.println(e.getMessage());
	    }
	}
	
	
}
