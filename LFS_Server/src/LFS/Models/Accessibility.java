package LFS.Models;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Accessibility{
	private BigInteger id;
	private String code;
	private Connection conn;
	
	public BigInteger getId(){
		return this.id;
	}
	public String getCode() throws Exception{
		this.retrieveData(this.id);
		return this.code;
	}
	
	public Accessibility(BigInteger id,Connection conn) throws Exception{
		this.conn = conn;
		this.retrieveData(id);
	}
	public Accessibility(String code,Connection conn) throws Exception{
		this.conn = conn;
		this.retrieveData(code);
	}
	private void retrieveData(BigInteger id) throws Exception{
		Statement stmt = this.conn.createStatement();
		String get_sql = String.format("SELECT * FROM Accessibility \n"
        		+ "WHERE id == %s;",id.toString());
		ResultSet rs = stmt.executeQuery(get_sql);
		this._assignData(rs);
	}
	private void retrieveData(String code) throws Exception{
		Statement stmt = this.conn.createStatement();
		String get_sql = String.format("SELECT * FROM Accessibility \n"
        		+ "WHERE code == '%s';",code);
		ResultSet rs = stmt.executeQuery(get_sql);
		this._assignData(rs);
	}
	
	private void _assignData(ResultSet rs) throws Exception{
		if(rs.getString("id") == null) throw new SQLException("IP_Address_Status not found");
		this.id = new BigInteger(rs.getString("id"));
		this.code = rs.getString("code");
	}
	private void update(String col,String value,boolean isString) throws Exception {
		Statement stmt = conn.createStatement();
		String update_query = String.format("UPDATE Accessibilty\n"
				+ "SET %s = " + (isString ? "'" : "") + "%s" + (isString ? "'" : "") + "\n"
				+ "WHERE id == %s",col,value,this.id.toString());
		stmt.execute(update_query);
	}
	
	public static HashMap<String,BigInteger> getAccessibilityPriority(){
		HashMap<String,BigInteger> map = new HashMap<>();
		map.put("private", BigInteger.valueOf(0));
		map.put("restricted", BigInteger.valueOf(1));
		map.put("read", BigInteger.valueOf(2));
		map.put("write", BigInteger.valueOf(3));
		return map;
	}
	
	public static void ensureDatabase(Connection conn) {
        try{
        	Statement stmt = conn.createStatement();
        	String create_table_sql = "CREATE TABLE IF NOT EXISTS Accessibility (\n"
    				+ "id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
    				+ "code text UNIQUE);";
            stmt.execute(create_table_sql);
            
            String[] codes = {"private","restricted","read","write"};
            
            for(String c:codes) {
            	String get_sql = String.format("SELECT * FROM Accessibility \n"
                		+ "WHERE code == '%s';",c);
                
                ResultSet rs = stmt.executeQuery(get_sql);
                
                if(rs.getString("code") == null) {
                	String create_sql = String.format("INSERT INTO Accessibility (code) \n"
                    		+ "VALUES('%s');",c);
                	stmt.execute(create_sql);
                }
            }
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
	}
}
