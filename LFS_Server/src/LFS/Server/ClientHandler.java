package LFS.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.HashMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import LFS.Models.IP_Address;
import LFS.Models.User;
import LFS.Models.User_IP_Address;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final Connection conn;
    private final JSONObject config_json;
    private final String local_path;
    // Constructor
    public ClientHandler(Socket socket,Connection conn,JSONObject config_json,String local_path)
    {
        this.socket = socket;
        this.conn = conn;
        this.config_json = config_json;
        this.local_path = local_path;
    }

    public void run()
    {

    	DataInputStream dis = null;
    	DataOutputStream dos = null;
    	
        try {
        	dis = new DataInputStream(this.socket.getInputStream());
        	dos = new DataOutputStream(this.socket.getOutputStream());
        	
        	System.out.println(String.format("IP_Address : %s",this.socket.getLocalAddress().getHostAddress()));
        	System.out.println(String.format("Port : %d",this.socket.getLocalPort()));
        	
        	JSONObject request = (JSONObject) new JSONParser().parse(dis.readUTF());
        	String ip_address = this.socket.getLocalAddress().getHostAddress();
        	
        	IP_Address ipa = null;
        	try {
        		ipa = new IP_Address(ip_address,this.conn);
        	}catch (Exception e){
        		ipa = IP_Address.createIPAddress(ip_address, conn, this.config_json);
        	}
        	
        	User user = null;
        	
        	try {
        		String username = request.get("username").toString();
        		String password = request.get("password").toString();
        		user = new User(username,conn);
        		if(!user.Login(username, password)) throw new Exception("Wrong password!");
        	}catch(Exception e) {
        		user = null;
        	}
        	
        	if(user!=null) {
	    		User_IP_Address check_uipa = new User_IP_Address(user, ipa, conn);
	    		if(user.getUserStatus().getCode() == "disabled") 
	    			throw new Exception("Nguoi dung nay da bi chan, vui long lien he quan tri vien de cap quyen.");
	    		if(ipa.getStatus().getCode() == "disabled") 
	    			throw new Exception("Dia chi IP nay da bi chan, vui long lien he quan tri vien de cap quyen.");
        	}

        	JSONObject response = null;
        	
        	switch(request.get("action").toString()) {
        		case "createFolder":
        			response = Action.actionCreateFolder(request,this.socket,this.conn,this.local_path);
        			break;
        		case "registerUser":
        			response = Action.actionRegisterUser(request,this.socket,this.conn,this.config_json);
        			break;
        		case "uploadFile":
        			response = Action.actionUploadFile(request, this.socket, this.conn, this.local_path);
        			break;
        		case "downloadFile":
        			response = Action.actionDownloadFile(request, this.socket, this.conn, this.local_path);
        			break;
        		case "downloadFolder":
        			response = Action.actionDownloadFolder(request, this.socket, this.conn, this.local_path);
        			break;
        		case "editFile":
        			response = Action.actionEditFile(request, this.socket, this.conn, this.local_path);
        			break;
        		case "editFolder":
        			response = Action.actionEditFolder(request, this.socket, this.conn, this.local_path);
        			break;
        		case "deleteFile":
        			response = Action.actionDeleteFile(request, this.socket, this.conn, this.local_path);
        			break;
        		case "deleteFolder":
        			response = Action.actionDeleteFolder(request, this.socket, this.conn, this.local_path);
        			break;
        	}
        	
        	if(response == null) {
        		response = new JSONObject();
        		response.put("status","failed");
        		response.put("message", "Hay them mot hanh dong hop le vao truong action.");
        	}
        	
        	dos.writeUTF(response.toJSONString());
        	
        }
        catch (Exception e) {
        	JSONObject response = new JSONObject();
        	e.printStackTrace();
        	response.put("status","failed");
			response.put("exception_class", e.getClass().toString());
			response.put("message", e.getMessage());
			try {
				dos.writeUTF(response.toJSONString());
			} catch (Exception e1) {}
        }
        try {
        	this.socket.close();
        }
        catch (Exception e) {}
    }
}
