package LFS.Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Action {
	public static JSONObject actionCreateFolder(String username, String password, String folder_owner, String folder_path, Socket socket) throws Exception{
		JSONObject request = new JSONObject();
		request.put("action", "createFolder");
		request.put("username", username);
		request.put("password", password);
		request.put("folder_path", folder_path);
		request.put("targer_user", folder_owner);

		DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
		DataInputStream dis = new DataInputStream(socket.getInputStream());
		dos.writeUTF(request.toJSONString());
		
		JSONObject response = (JSONObject) new JSONParser().parse(dis.readUTF());
		return response;
	}
	public static JSONObject actionRegisterUser(String username, String password, Socket socket) throws Exception{
		JSONObject request = new JSONObject();
		request.put("action", "registerUser");
		request.put("username", username);
		request.put("password", password);
		DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
		DataInputStream dis = new DataInputStream(socket.getInputStream());
		dos.writeUTF(request.toJSONString());
		
		JSONObject response = (JSONObject) new JSONParser().parse(dis.readUTF());
		return response;
	}
	public static JSONObject actionUploadFile(String username, String password,String owner,String folder_path,String filename,String file_path, Socket socket) throws Exception{
		JSONObject request = new JSONObject();
		request.put("action", "uploadFile");
		request.put("username", username);
		request.put("password", password);
		request.put("target_user", owner);
		request.put("folder_path", folder_path);
		request.put("filename", filename);
		
		DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
		DataInputStream dis = new DataInputStream(socket.getInputStream());
		dos.writeUTF(request.toJSONString());
		
		JSONObject response = (JSONObject) new JSONParser().parse(dis.readUTF());
		
		if(response.get("status").toString().compareTo("accepted") == 0) {
			try {
				Utils.sendFile(file_path,socket);
			}catch(Exception e){
				response = new JSONObject();
	    		response.put("status", "failed");
	    		response.put("message",String.format("Server da tu choi nhan file.",owner));
			}
		}
		
		try {
			response = (JSONObject) new JSONParser().parse(dis.readUTF());
		}catch(Exception e){

		}
		return response;
	}
	public static JSONObject actionDownloadFile(String username, String password,String owner,String folder_path,String filename,String file_path, Socket socket) throws Exception{
		JSONObject request = new JSONObject();
		request.put("action", "downloadFile");
		request.put("username", username);
		request.put("password", password);
		request.put("target_user", owner);
		request.put("folder_path", folder_path);
		request.put("filename", filename);
		
		DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
		DataInputStream dis = new DataInputStream(socket.getInputStream());
		dos.writeUTF(request.toJSONString());
		
		JSONObject response = (JSONObject) new JSONParser().parse(dis.readUTF());
		
		if(response.get("status").toString().compareTo("accepted") == 0) {
			System.out.println("Accepted");
			try {
				Utils.receiveFile(file_path,socket);
			}catch(Exception e){
				response = new JSONObject();
	    		response.put("status", "failed");
	    		response.put("message",String.format("Server da tu choi gui file.",owner));
			}
		}
		
		try {
			response = (JSONObject) new JSONParser().parse(dis.readUTF());
		}catch(Exception e){

		}
		return response;
	}
	public static JSONObject actionDownloadFolder(String username, String password,String owner,String folder_path,String file_path, Socket socket) throws Exception{
		JSONObject request = new JSONObject();
		request.put("action", "downloadFolder");
		request.put("username", username);
		request.put("password", password);
		request.put("target_user", owner);
		request.put("folder_path", folder_path);
		
		DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
		DataInputStream dis = new DataInputStream(socket.getInputStream());
		dos.writeUTF(request.toJSONString());
		
		JSONObject response = (JSONObject) new JSONParser().parse(dis.readUTF());
		
		if(response.get("status").toString().compareTo("accepted") == 0) {
			System.out.println("Accepted");
			try {
				Utils.receiveFile(file_path,socket);
			}catch(Exception e){
				response = new JSONObject();
	    		response.put("status", "failed");
	    		response.put("message",String.format("Server da tu choi gui file.",owner));
			}
		}
		
		try {
			response = (JSONObject) new JSONParser().parse(dis.readUTF());
		}catch(Exception e){

		}
		return response;
	}
	public static JSONObject actionEditFolder(String username, String password,String owner,
			String edit_path,String accessibility,JSONArray shared_users,String new_path,String copy_method,Socket socket) throws Exception {
		JSONObject request = new JSONObject();
		request.put("action", "editFolder");
		request.put("username", username);
		request.put("password", password);
		request.put("target_user", owner);
		request.put("edit_path", edit_path);
		
		request.put("accessibility", accessibility);
		request.put("shared_users",shared_users);
		
		request.put("new_path", new_path);
		request.put("copy_method", copy_method);

		
		DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
		DataInputStream dis = new DataInputStream(socket.getInputStream());
		dos.writeUTF(request.toJSONString());
		
		JSONObject response = (JSONObject) new JSONParser().parse(dis.readUTF());
		
		return response;
	}
	public static JSONObject actionEditFile(String username, String password,String owner,
			String edit_path,String filename,String accessibility,JSONArray shared_users,String new_path,String copy_method,Socket socket) throws Exception {
		JSONObject request = new JSONObject();
		request.put("action", "editFile");
		request.put("username", username);
		request.put("password", password);
		request.put("target_user", owner);
		request.put("edit_path", edit_path);
		request.put("filename", filename);
		
		request.put("accessibility", accessibility);
		request.put("shared_users",shared_users);
		
		request.put("new_path", new_path);
		request.put("copy_method", copy_method);

		
		DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
		DataInputStream dis = new DataInputStream(socket.getInputStream());
		dos.writeUTF(request.toJSONString());
		
		JSONObject response = (JSONObject) new JSONParser().parse(dis.readUTF());
		
		return response;
	}
	public static JSONObject actionDeleteFile(String username, String password,String owner,
			String deleting_path,String filename,Socket socket) throws Exception {
		JSONObject request = new JSONObject();
		request.put("action", "deleteFile");
		request.put("username", username);
		request.put("password", password);
		request.put("target_user", owner);
		request.put("path", deleting_path);
		request.put("filename", filename);

		
		DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
		DataInputStream dis = new DataInputStream(socket.getInputStream());
		dos.writeUTF(request.toJSONString());
		
		JSONObject response = (JSONObject) new JSONParser().parse(dis.readUTF());
		
		return response;
	}
	public static JSONObject actionDeleteFolder(String username, String password,String owner,
			String deleting_path,String filename,Socket socket) throws Exception {
		JSONObject request = new JSONObject();
		request.put("action", "deleteFolder");
		request.put("username", username);
		request.put("password", password);
		request.put("target_user", owner);
		request.put("path", deleting_path);

		
		DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
		DataInputStream dis = new DataInputStream(socket.getInputStream());
		dos.writeUTF(request.toJSONString());
		
		JSONObject response = (JSONObject) new JSONParser().parse(dis.readUTF());
		
		return response;
	}
}
