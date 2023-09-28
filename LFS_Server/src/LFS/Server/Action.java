package LFS.Server;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;

import org.json.simple.JSONObject;

import LFS.Models.*;

import LFS.Utils.*;

public class Action {
	public static JSONObject actionRegisterUser(JSONObject request,Socket socket,Connection conn,JSONObject config_json) throws Exception{
		JSONObject response = new JSONObject();
		try {
			String username = request.get("username").toString();
			String password = request.get("password").toString();

			User user = null;
			
			String ip_address = socket.getLocalAddress().getHostAddress();
        	IP_Address ipa = null;
        	try {
        		ipa = new IP_Address(ip_address,conn);
        	}catch (Exception e){
        		ipa = IP_Address.createIPAddress(ip_address, conn, config_json);
        	}
			
			try {
				user = new User(username,conn);
				throw new Exception(String.format("Nguoi dung %s da ton tai",username));
			}catch(SQLException e) {
				user = User.createUser(username, password, conn, config_json);
			}
			
			User_IP_Address.createUserIPAddress(user, ipa, conn);
			
			response.put("message",String.format("Nguoi dung %s da duoc tao thanh cong",username));
		}catch(Exception e){
			e.printStackTrace();
			response.put("status","failed");
			response.put("exception_class", e.getClass().toString());
			response.put("message", e.getMessage());
		}
		return response;
	}
	public static JSONObject actionCreateFolder(JSONObject request,Socket socket,Connection conn,String local_path) throws Exception{

		JSONObject response = new JSONObject();
		try {
			String username = request.get("username").toString();
			String password = request.get("password").toString();
			String folder_owner = request.get("target_user").toString();
			String folder_path = request.get("folder_path").toString();
			User user = null;
			User owner = null;
			try {
				user = new User(username,conn);
				if(!user.Login(username, password)) throw new Exception();
			}catch(Exception e) {
				throw new Exception("Ten nguoi dung hoac mat khau sai.");
			}
			try {
				owner = new User(folder_owner,conn);
			}catch(Exception e) {
				throw new Exception(String.format("Nguoi dung %s khong ton tai.",folder_owner));
			}
			
			Utils.createFolder(user,owner,local_path,folder_path,conn);
			
			response.put("status","succeeded");
			response.put("message",String.format("Folder %s/%s da duoc tao thanh cong",username,folder_path));
		}catch(Exception e){
			System.out.println(e);
			response.put("status","failed");
			response.put("exception_class", e.getClass().toString());
			response.put("message", e.getMessage());
		}
		return response;
		
	}
	public static JSONObject actionUploadFile(JSONObject request,Socket socket,Connection conn,String local_path) throws Exception{

		JSONObject response = new JSONObject();
		try {
			String username = request.get("username").toString();
			String password = request.get("password").toString();
			String file_owner = request.get("target_user").toString();
			String folder_path = request.get("folder_path").toString();
			String filename = request.get("filename").toString();
			User user = null;
			User owner = null;
			try {
				if(username != null) {
					user = new User(username,conn);
					if(!user.Login(username, password)) throw new Exception();
				}
			}catch(Exception e) {
				e.printStackTrace();
				throw new Exception("Ten nguoi dung hoac mat khau sai.");
			}
			try {
				owner = new User(file_owner,conn);
				owner.setLocalPath(local_path);
			}catch(Exception e) {
				throw new Exception(String.format("Nguoi dung %s khong ton tai.",file_owner));
			}
			
			response = Utils.uploadFile(user, owner, local_path, folder_path, filename, conn, socket);

		}catch(Exception e){
			e.printStackTrace();
			response.put("status","failed");
			response.put("exception_class", e.getClass().toString());
			response.put("message", e.getMessage());
		}
		return response;
		
	}

	public static JSONObject actionEditFile(JSONObject request,Socket socket,Connection conn,String local_path) {
		JSONObject response = new JSONObject();
		try {
			String username = request.get("username").toString();
			String password = request.get("password").toString();
			String file_owner = request.get("target_user").toString();
			String folder_path = request.get("edit_path").toString();
			String filename = request.get("filename").toString();
			User user = null;
			User owner = null;
			try {
				if(username != null) {
					user = new User(username,conn);
					if(!user.Login(username, password)) throw new Exception();
				}
			}catch(Exception e) {
				throw new Exception("Ten nguoi dung hoac mat khau sai.");
			}
			try {
				owner = new User(file_owner,conn);
				owner.setLocalPath(local_path);
			}catch(Exception e) {
				throw new Exception(String.format("Nguoi dung %s khong ton tai.",file_owner));
			}
			
			response = Utils.editFile(user, owner, local_path, request, conn);

		}catch(Exception e){
			e.printStackTrace();
			response.put("status","failed");
			response.put("exception_class", e.getClass().toString());
			response.put("message", e.getMessage());
		}
		return response;
	}
	public static JSONObject actionEditFolder(JSONObject request,Socket socket,Connection conn,String local_path) {
		JSONObject response = new JSONObject();
		try {
			String username = request.get("username").toString();
			String password = request.get("password").toString();
			String file_owner = request.get("target_user").toString();
			String folder_path = request.get("edit_path").toString();
			User user = null;
			User owner = null;
			try {
				if(username != null) {
					user = new User(username,conn);
					if(!user.Login(username, password)) throw new Exception();
				}
			}catch(Exception e) {
				throw new Exception("Ten nguoi dung hoac mat khau sai.");
			}
			try {
				owner = new User(file_owner,conn);
				owner.setLocalPath(local_path);
			}catch(Exception e) {
				throw new Exception(String.format("Nguoi dung %s khong ton tai.",file_owner));
			}
			
			response = Utils.editFolder(user, owner, local_path, request, conn);

		}catch(Exception e){
			e.printStackTrace();
			response.put("status","failed");
			response.put("exception_class", e.getClass().toString());
			response.put("message", e.getMessage());
		}
		return response;
	}
	public static JSONObject actionDownloadFile(JSONObject request,Socket socket,Connection conn,String local_path) {
		JSONObject response = new JSONObject();
		try {
			String username = request.get("username").toString();
			String password = request.get("password").toString();
			String file_owner = request.get("target_user").toString();
			String folder_path = request.get("folder_path").toString();
			String filename = request.get("filename").toString();
			User user = null;
			User owner = null;
			try {
				if(username != null) {
					user = new User(username,conn);
					if(!user.Login(username, password)) throw new Exception();
				}
			}catch(Exception e) {
				throw new Exception("Ten nguoi dung hoac mat khau sai.");
			}
			try {
				owner = new User(file_owner,conn);
				owner.setLocalPath(local_path);
			}catch(Exception e) {
				throw new Exception(String.format("Nguoi dung %s khong ton tai.",file_owner));
			}
			
			response = Utils.downloadFile(user, owner, local_path, folder_path, filename, conn, socket);

		}catch(Exception e){
			e.printStackTrace();
			response.put("status","failed");
			response.put("exception_class", e.getClass().toString());
			response.put("message", e.getMessage());
		}
		return response;
	}
	public static JSONObject actionDownloadFolder(JSONObject request,Socket socket,Connection conn,String local_path) {
		JSONObject response = new JSONObject();
		try {
			String username = request.get("username").toString();
			String password = request.get("password").toString();
			String file_owner = request.get("target_user").toString();
			String folder_path = request.get("folder_path").toString();

			User user = null;
			User owner = null;
			try {
				user = new User(username,conn);
				if(!user.Login(username, password)) throw new Exception();
			}catch(Exception e) {
				throw new Exception("Ten nguoi dung hoac mat khau sai.");
			}
			try {
				owner = new User(file_owner,conn);
				owner.setLocalPath(local_path);
			}catch(Exception e) {
				throw new Exception(String.format("Nguoi dung %s khong ton tai.",file_owner));
			}
			
			response = Utils.downloadFolder(user, owner, local_path, folder_path, conn, socket);

		}catch(Exception e){
			e.printStackTrace();
			response.put("status","failed");
			response.put("exception_class", e.getClass().toString());
			response.put("message", e.getMessage());
		}
		return response;
	}
	public static JSONObject actionDeleteFile(JSONObject request,Socket socket,Connection conn,String local_path) {
		JSONObject response = new JSONObject();
		try {
			String username = request.get("username").toString();
			String password = request.get("password").toString();
			String file_owner = request.get("target_user").toString();
			String folder_path = request.get("path").toString();
			String filename = request.get("filename").toString();
			User user = null;
			User owner = null;
			try {
				if(username != null) {
					user = new User(username,conn);
					if(!user.Login(username, password)) throw new Exception();
				}
			}catch(Exception e) {
				throw new Exception("Ten nguoi dung hoac mat khau sai.");
			}
			try {
				owner = new User(file_owner,conn);
				owner.setLocalPath(local_path);
			}catch(Exception e) {
				throw new Exception(String.format("Nguoi dung %s khong ton tai.",file_owner));
			}
			
			Utils.editFile(user, owner, local_path, request, conn);
			response.put("status","succeeded");
			response.put("message", String.format("File %s trong Folder %s cua nguoi dung %s da duoc xoa",
					filename,folder_path,owner.getUsername()));

		}catch(Exception e){
			e.printStackTrace();
			response.put("status","failed");
			response.put("exception_class", e.getClass().toString());
			response.put("message", e.getMessage());
		}
		return response;
	}
	public static JSONObject actionDeleteFolder(JSONObject request,Socket socket,Connection conn,String local_path) {
		JSONObject response = new JSONObject();
		try {
			String username = request.get("username").toString();
			String password = request.get("password").toString();
			String file_owner = request.get("target_user").toString();
			String folder_path = request.get("path").toString();
			String filename = request.get("filename").toString();
			User user = null;
			User owner = null;
			try {
				if(username != null) {
					user = new User(username,conn);
					if(!user.Login(username, password)) throw new Exception();
				}
			}catch(Exception e) {
				throw new Exception("Ten nguoi dung hoac mat khau sai.");
			}
			try {
				owner = new User(file_owner,conn);
				owner.setLocalPath(local_path);
			}catch(Exception e) {
				throw new Exception(String.format("Nguoi dung %s khong ton tai.",file_owner));
			}
			
			Utils.editFolder(user, owner, local_path, request, conn);
			response.put("status","succeeded");
			response.put("message", String.format("Folder %s cua nguoi dung %s da duoc xoa",
					folder_path,owner.getUsername()));

		}catch(Exception e){
			e.printStackTrace();
			response.put("status","failed");
			response.put("exception_class", e.getClass().toString());
			response.put("message", e.getMessage());
		}
		return response;
	}
}
