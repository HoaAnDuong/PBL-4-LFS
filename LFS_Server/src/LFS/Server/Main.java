package LFS.Server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.DatabaseMetaData;
import java.net.*;
import java.util.ArrayList;

import LFS.Models.*;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Main {
	private String local_path;
	private String url;
	private String config_path;
	private Connection conn;
	private JSONObject config_json;
	
	public static void main(String args[]) throws Exception {
		new Main("D:/PBL4/Shared_Folder");
	}
	
	public void prepareShareFolder(String local_path) throws Exception {
		this.local_path = local_path;
		this.url = "jdbc:sqlite:"+ this.local_path +"/LFS.db";
		this.config_path = this.local_path + "/config.json";
		
		new File(local_path + "/" + "Temp").mkdirs();
		
		try {
			this.config_json = (JSONObject) new JSONParser().parse(new FileReader(this.config_path));
		} catch (FileNotFoundException e){
			this.config_json = new JSONObject();
			this.config_json.put("max_bytes_allocated","104857600");
			this.config_json.put("max_ip_address_used","5");
			this.config_json.put("max_user_on_ip_address","5");
	    	PrintWriter pw = new PrintWriter(this.config_path);
	        pw.write(config_json.toJSONString());
	    	pw.flush();
	    	pw.close();
	    } 
		
		this.conn = DriverManager.getConnection(url);
		this.config_json = (JSONObject) new JSONParser().parse(new FileReader(this.config_path));
		
        if (this.conn != null) {
            DatabaseMetaData meta = conn.getMetaData();
            System.out.println("The driver name is " + meta.getDriverName());
        }
        Accessibility.ensureDatabase(this.conn);
        IP_Address_Status.ensureDatabase(this.conn);
        User_Status.ensureDatabase(this.conn);
        User.ensureDatabase(this.conn);
        FileM.ensureDatabase(this.conn);
        Folder.ensureDatabase(this.conn);
        IP_Address.ensureDatabase(this.conn);
        User_IP_Address.ensureDatabase(this.conn);
        User_Shared_File.ensureDatabase(this.conn);
        User_Shared_Folder.ensureDatabase(this.conn);
	}
	
	public Main(String local_path) throws Exception {
		prepareShareFolder(local_path);
        
        InetAddress localhost = InetAddress.getLocalHost();
        System.out.println("System IP Address : " +
                      (localhost.getHostAddress()).trim());
        
        ServerSocket server_socket = new ServerSocket(2343);
        System.out.println("Port : " + server_socket.getLocalPort());
        while(true) {
        	Socket client_socket = server_socket.accept();
        	ClientHandler client_handler = new ClientHandler(client_socket,this.conn,this.config_json,local_path);
        	new Thread(client_handler).start();
        }
	}
}
