package LFS.Utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import LFS.Models.Accessibility;
import LFS.Models.FileM;
import LFS.Models.Folder;
import LFS.Models.User;
import LFS.Models.User_Shared_File;
import LFS.Models.User_Shared_Folder;

public class Utils {
	public static void sendFile(String path,Socket socket) throws Exception{
		System.out.println(path);
    	DataOutputStream dos= new DataOutputStream(
                socket.getOutputStream());
        int bytes = 0;
        // Open the File where he located in your pc
        File file = new File(path);
        FileInputStream fileInputStream
            = new FileInputStream(file);
 
        // Here we send the File to Server
        dos.writeLong(file.length());
        // Here we  break file into chunks
        byte[] buffer = new byte[4 * 1024];
        while ((bytes = fileInputStream.read(buffer))
               != -1) {
          // Send the file to Server Socket 
          dos.write(buffer, 0, bytes);
          dos.flush();
        }
        // close the file here
        fileInputStream.close();
    }
	public static void receiveFile(String filePath,Socket socket) throws Exception{
    	DataInputStream dis = new DataInputStream(socket.getInputStream());
        int bytes = 0;
        FileOutputStream fileOutputStream
            = new FileOutputStream(filePath);
 
        long size
            = dis.readLong(); // read file size
        System.out.println(size);
        
        byte[] buffer = new byte[4 * 1024];
        while (size > 0
               && (bytes = dis.read(
                       buffer, 0,
                       (int)Math.min(buffer.length, size)))
                      != -1) {
            // Here we write the file using write method
            fileOutputStream.write(buffer, 0, bytes);
            size -= bytes; // read upto file size
        }
        // Here we received file
        System.out.println("File is Received");
        fileOutputStream.close();
	}
	public static void createFolder(User user,User owner,String local_path,String folder_path,Connection conn) throws Exception {
		
		String acc = Folder.getFolderAccessibility(user, owner, folder_path, conn).getCode();
		
		if(acc.compareTo("write") !=0 ) // That's JAVA String comparison bruhh 
			throw new Exception(String.format("Nguoi dung %s khong co quyen tao thu muc %s tren folder cua nguoi dung %s",user.getUsername(),folder_path,owner.getUsername()));
		directCreateFolder(owner,local_path,folder_path,conn);
		
	}
	public static void directCreateFolder(User owner,String local_path,String folder_path,Connection conn) throws Exception {
		File f = new File(local_path + "/" + owner.getUsername() + "/" + "/" + folder_path);
		f.mkdirs();
		
		try {
			Folder folder = new Folder(owner,folder_path,conn);
			throw new Exception(String.format("Folder %s existed",folder_path));
		}catch(SQLException e) {
			Folder.createFolder(owner, folder_path, conn);
		}
	}
	public static JSONObject downloadFolder(User user,User owner,String local_path,String path,Connection conn,Socket socket) throws Exception {
		HashMap<String,BigInteger> access_priority = Accessibility.getAccessibilityPriority();
		String acc = Folder.getFolderAccessibility(user, owner, path, conn).getCode();
		
		if(access_priority.get(acc).compareTo(access_priority.get("read")) == -1)
			throw new Exception(String.format("Nguoi dung %s khong co quyen download folder %s cua nguoi dung %s",user.getUsername(),path,owner.getUsername()));
		
		String real_path = local_path + "/"  + owner.getUsername() + "/" + path;
		String temp_path = local_path + "/" + "Temp";
		if(!Files.exists(new File(real_path).toPath()) || !new File(real_path).isDirectory())
			throw new Exception(String.format("Folder %s cua nguoi dung %s khong ton tai",path,owner.getUsername()));
		
		File temp_zip = new File(temp_path + "/" + new File(real_path).getName() + ".zip");
		
		new Zip().zipFolder(real_path, temp_path, new File(real_path).getName() + ".zip");
		
		JSONObject response = new JSONObject();
		response.put("status", "accepted");
		DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
		dos.writeUTF(response.toJSONString());
		
		sendFile(temp_zip.getAbsolutePath(),socket);
		
		temp_zip.delete();
		
		response = new JSONObject();
		response.put("status", "succeeded");
		response.put("message", String.format("Folder %s cua nguoi dung %s da duoc gui thanh cong",
				path,owner.getUsername()));
		return response;
	}
	public static JSONObject uploadFile(User user,User owner,String local_path,String folder_path,String filename,Connection conn,Socket socket) throws Exception {
		String acc = FileM.getFileAccessibility(user, owner, folder_path,filename, conn).getCode();
		
		if(acc.compareTo("write") !=0 )
			throw new Exception(String.format("Nguoi dung %s khong co quyen upload file tren folder %s cua nguoi dung %s",user.getUsername(),folder_path,owner.getUsername()));
		
		String real_folder_path = local_path + "/" + user.getUsername() + "/" + folder_path;
		String real_file_path = real_folder_path + "/" + filename;
		
		
		
		JSONObject response = new JSONObject();
		response.put("status", "accepted");
		DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
		dos.writeUTF(response.toJSONString());
		
		owner.setLocalPath(local_path);
		
		DataInputStream dis = new DataInputStream(socket.getInputStream());
        int bytes = 0;

        long size = dis.readLong(); // read file size
        if(owner.getRemainingBytes().compareTo(BigInteger.valueOf(size)) == -1) {
        	response = new JSONObject();
    		response.put("status", "failed");
    		response.put("message",String.format("Dung luong con lai cua nguoi dung %s khong du de upload file len",owner.getUsername()));
    		return response;
        }else {
        	File f = new File(real_folder_path);
    		f.mkdirs();
        
	        FileOutputStream fos = new FileOutputStream(real_file_path);
	        
	        byte[] buffer = new byte[4 * 1024];
	        while (size > 0
	               && (bytes = dis.read(buffer, 0,(int)Math.min(buffer.length, size)))
	                      != -1) {
	            fos.write(buffer, 0, bytes);
	            size -= bytes;
	        }
	
	        fos.close();
        }
		
		Folder folder = null;
		try {
			folder = new Folder(owner,folder_path,conn);
		}catch(SQLException e) {
			folder = Folder.createFolder(owner, folder_path, conn);
		}
		FileM fm = null;
		try {
			fm = new FileM(owner,folder,filename,conn);
		}catch(SQLException e) {
			fm = FileM.createFile(owner,folder,filename,conn);
		}
		
		response = new JSONObject();
		response.put("status", "succeeded");
		response.put("message", String.format("File %s da duoc upload thanh cong len folder %s cua nguoi dung %s",
				filename,folder_path,owner.getUsername()));
		return response;
	}
	public static JSONObject downloadFile(User user,User owner,String local_path,String path,String filename,Connection conn,Socket socket) throws Exception {
		HashMap<String,BigInteger> access_priority = Accessibility.getAccessibilityPriority();
		String acc = FileM.getFileAccessibility(user, owner, path,filename, conn).getCode();
		
		if(access_priority.get(acc).compareTo(access_priority.get("read")) == -1)
			throw new Exception(String.format("Nguoi dung %s khong co quyen download file %s tren folder %s cua nguoi dung %s",user.getUsername(),filename,path,owner.getUsername()));
		
		String real_path = local_path + "/"  + owner.getUsername() + "/" + path + "/" + filename;
		if(!Files.exists(new File(real_path).toPath()) || !new File(real_path).isFile())
			throw new Exception(String.format("File %s khong ton tai trong Folder %s cua nguoi dung %s",filename,path,owner.getUsername()));
		
		JSONObject response = new JSONObject();
		response.put("status", "accepted");
		DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
		dos.writeUTF(response.toJSONString());
		
		sendFile(real_path,socket);
		
		response = new JSONObject();
		response.put("status", "succeeded");
		response.put("message", String.format("File %s trong folder %s cua nguoi dung %s da duoc gui thanh cong",
				filename,path,owner.getUsername()));
		return response;
	}
	public static BigInteger folderSize(File directory) {
		BigInteger length = BigInteger.valueOf(0);
	    for (File f : directory.listFiles()) {
	        if (f.isFile())
	            length = length.add(BigInteger.valueOf(f.length()));
	        else
	        	length = length.add(folderSize(f));
	    }
	    return length;
	}
	public static JSONObject editFolder(User user,User owner,String local_path,JSONObject json,Connection conn) throws Exception {
		JSONObject response = new JSONObject();
		String message = "";
		
		String edit_path = null;
		String new_path = null;
		Accessibility new_access = null; 
		JSONArray shared_users = null;

		if(json.get("edit_path") == null) throw new Exception("Yeu cau bo sung thu muc can chinh sua vao truong edit_path");
		edit_path = (String) json.get("edit_path");

		
		if((!Files.exists(new File(local_path + "/" + owner.getUsername() + "/" + edit_path).toPath())) || (!new File(local_path + "/" + owner.getUsername() + "/" + edit_path).isDirectory()))
			throw new Exception(String.format("Folder %s khong ton tai",edit_path));
				
		String acc = Folder.getFolderAccessibility(user, owner, edit_path, conn).getCode();
		
		if(acc.compareTo("write") !=0)
			throw new Exception(String.format("Nguoi dung %s khong the thuc hien thay doi tren folder %s cua nguoi dung %s",
					user.getUsername(),edit_path,owner.getUsername()));
		
		try {
			if((String) json.get("accessibility") != null)
			new_access = new Accessibility((String) json.get("accessibility"),conn);
		}catch(SQLException e){
			throw new Exception("Cac kha nang truy cap(accessibility) co the dat cho 1 Folder la 'private', 'restricted', 'read', 'write'");
		}catch(Exception e){}
		
		try {
			shared_users = (JSONArray) json.get("shared_users");
			if(shared_users != null) {
				JSONObject user_accessibility = null;
				for(int i = 0;i < shared_users.size();i++)
					user_accessibility = (JSONObject)shared_users.get(i);
					if(user_accessibility.get("username") == null || user_accessibility.get("accessibility") == null)
						throw new Exception("Missing key");
					new Accessibility((String) user_accessibility.get("accessibility"),conn);
			}
		}catch(ClassCastException e) {
			throw new Exception("Truong shared_users phai duoc dinh nghia la JSONArray chua cac JSONObject, co dang\n [{\n'username':username,\n'accessibility':accessibility\n},...]");
		}catch(SQLException e){
			throw new Exception("Cac kha nang truy cap(accessibility) co the dat cho 1 Folder la 'private', 'restricted', 'read', 'write'");
		}catch(Exception e){
			if(e.getMessage().compareTo("Missing key") == 0)
				throw new Exception("Truong shared_users phai duoc dinh nghia la JSONArray chua cac JSONObject, co dang\n [{\n'username':username,\n'accessibility':accessibility\n},...]");
		}
			
		
		if ((new_access != null || shared_users != null) && (user.getId().equals(owner.getId().toString()))) 
			throw new Exception(String.format("Nguoi dung %s khong the thay doi kha nang truy cap folder %s cua nguoi dung %s",
					user.getUsername(),edit_path,owner.getUsername()));
		
		Folder edit_folder = null;
		Folder new_folder = null;
		try {
			edit_folder = new Folder(owner,edit_path,conn);
		}catch(Exception e){
			edit_folder = Folder.createFolder(owner, edit_path, conn);
		}
		
		try {
			new_path =(String) json.get("new_path");
			while(Files.exists(new File(local_path + "/" + owner.getUsername() + "/" + new_path).toPath())) {
				new_path += " - Copy";
			}
		}catch(Exception e){}
		
		if(new_path!=null) {
			acc = Folder.getFolderAccessibility(user, owner, new_path, conn).getCode();
		 	if(acc.compareTo("write") !=0)
				throw new SQLException(String.format("Nguoi dung %s khong the di chuyen folder %s sang folder %s cua nguoi dung %s",
						user.getUsername(),edit_path,new_path,owner.getUsername()));
		 	String copy_method;
		 	try {
		 		copy_method = (String) json.get("copy_method");
		 		if(copy_method.compareTo("copy")!=0 && copy_method.compareTo("move")!=0) throw new Exception();
		 	}catch(Exception e){
		 		throw new Exception("Cac phuong thuc copy(copy_method) co the su dung la 'copy' va 'move'");
		 	}
		 	
			try {
				new_folder = new Folder(owner,new_path,conn);
			}catch(Exception e){
				new_folder = Folder.createFolder(owner, new_path, conn);
			}
			switch(copy_method) {
				case "move":
					directMoveFolder(owner,local_path,edit_path,new_path,conn);
					edit_folder = null;
					message += String.format("Folder %s cua nguoi dung %s da duoc di chuyen sang Folder %s \n",
							edit_path,owner.getUsername(),new_path);
					break;
				case "copy":
					directCopyFolder(owner,local_path,edit_path,new_path,conn);
					message += String.format("Folder %s cua nguoi dung %s da duoc copy sang Folder %s \n",
							edit_path,owner.getUsername(),new_path);
					break;
			}
		}
		if(edit_folder != null) {
			if(new_access!=null) { 
				edit_folder.setAccessibility(new_access);
				message += String.format("Folder %s cua nguoi dung %s duoc doi quyen truy cap thanh %s \n",
						edit_path,owner.getUsername(),new_access.getCode());
			}
			if(shared_users != null) {
				JSONObject user_accessibility = null;
				User sh_user = null;
				Accessibility sh_acc = null;
				for(int i = 0;i < shared_users.size();i++) {
					user_accessibility = (JSONObject)shared_users.get(i);
					sh_user = new User(user_accessibility.get("username").toString(),conn);
					sh_acc = new Accessibility(user_accessibility.get("accessibility").toString(),conn);
					
					try {
						User_Shared_Folder.createUserSharedFolder(sh_user,
								edit_folder, sh_acc, conn);
					}catch(Exception e) {
						new User_Shared_Folder(sh_user,edit_folder,conn).setAccessibility(sh_acc);
					}
					message += String.format("Folder %s cua nguoi dung %s duoc chia se voi nguoi dung %s voi quyen truy cap la %s \n",
							edit_path,owner.getUsername(),sh_user.getUsername(),sh_acc.getCode());
				}
				
			}
		}
		if(new_folder != null) {
			if(new_access!=null) { 
				new_folder.setAccessibility(new_access);
				message += String.format("Folder %s cua nguoi dung %s duoc doi quyen truy cap thanh %s \n",
						new_path,owner.getUsername(),new_access.getCode());
			}
			if(shared_users != null) {
				JSONObject user_accessibility = null;
				User sh_user = null;
				Accessibility sh_acc = null;
				for(int i = 0;i < shared_users.size();i++) {
					user_accessibility = (JSONObject)shared_users.get(i);
					sh_user = new User(user_accessibility.get("username").toString(),conn);
					sh_acc = new Accessibility(user_accessibility.get("accessibility").toString(),conn);
					try {
						User_Shared_Folder.createUserSharedFolder(sh_user,
								new_folder, sh_acc, conn);
					}catch(Exception e) {
						new User_Shared_Folder(sh_user,new_folder,conn).setAccessibility(sh_acc);
					}
					message += String.format("Folder %s cua nguoi dung %s duoc chia se voi nguoi dung %s voi quyen truy cap la %s \n",
							new_path,owner.getUsername(),sh_user.getUsername(),sh_acc.getCode());
				}
			}
		}
		
		response.put("status", "succeeeded");
		response.put("message", message);
		return response;
	}
	public static void deleteFolder(User user,User owner,String local_path,JSONObject json,Connection conn) throws Exception {
		String deleting_path = null;


		if(json.get("path") == null) throw new Exception("Yeu cau bo sung thu muc can xoa vao truong path");
		deleting_path = (String) json.get("path");

		
		if((!Files.exists(new File(local_path + "/" + owner.getUsername() + "/" + deleting_path).toPath())) || (!new File(local_path + "/" + owner.getUsername() + "/" + deleting_path).isDirectory()))
			throw new Exception(String.format("Folder %s khong ton tai",deleting_path));
				
		String acc = Folder.getFolderAccessibility(user, owner, deleting_path, conn).getCode();
		
		if(acc.compareTo("write") !=0)
			throw new Exception(String.format("Nguoi dung %s khong the xoa folder %s cua nguoi dung %s",
					user.getUsername(),deleting_path,owner.getUsername()));
		
		
		directDeleteFolder(owner,local_path,deleting_path,conn);
	}
	public static JSONObject editFile(User user,User owner,String local_path,JSONObject json,Connection conn) throws Exception {
		JSONObject response = new JSONObject();
		String message = "";
		
		String edit_path = null;
		String new_path = null;
		String filename = null;
		String new_filename = null;
		Accessibility new_access = null; 
		JSONArray shared_users = null;

		if(json.get("edit_path") == null) throw new Exception("Yeu cau bo sung thu muc cua tap tin can chinh sua vao truong edit_path");
		edit_path = (String) json.get("edit_path");
		
		if(json.get("filename") == null) throw new Exception("Yeu cau bo sung tap tin can chinh sua vao truong filename");
		filename = (String) json.get("filename");
		
		File old_f = new File(local_path + "/" + owner.getUsername() + "/" + edit_path + "/" + filename);
		
		if((!Files.exists(old_f.toPath())) || (!old_f.isFile()))
			throw new Exception(String.format("File %s khong ton tai trong Folder %s cua nguoi dung %s",filename,edit_path,owner.getUsername()));
				
		String acc = FileM.getFileAccessibility(user, owner, edit_path,filename, conn).getCode();
		
		if(acc.compareTo("write") !=0)
			throw new Exception(String.format("Nguoi dung %s khong the thuc hien thay doi tren file %s trong folder %s cua nguoi dung %s",
					user.getUsername(),filename,edit_path,owner.getUsername()));
		try {
			if((String) json.get("accessibility") != null)
			new_access = new Accessibility((String) json.get("accessibility"),conn);
		}catch(SQLException e){
			throw new Exception("Cac kha nang truy cap(accessibility) co the dat cho 1 File la 'private', 'restricted', 'read', 'write'");
		}catch(Exception e){}
		
		try {
			shared_users = (JSONArray) json.get("shared_users");
			if(shared_users != null) {
				JSONObject user_accessibility = null;
				for(int i = 0;i < shared_users.size();i++)
					user_accessibility = (JSONObject)shared_users.get(i);
					if(user_accessibility.get("username") == null || user_accessibility.get("accessibility") == null)
						throw new Exception("Missing key");
					new Accessibility((String) user_accessibility.get("accessibility"),conn);
			}
		}catch(ClassCastException e) {
			throw new Exception("Truong shared_users phai duoc dinh nghia la JSONArray chua cac JSONObject, co dang\n [{\n'username':username,\n'accessibility':accessibility\n},...]");
		}catch(SQLException e){
			throw new Exception("Cac kha nang truy cap(accessibility) co the dat cho 1 File la 'private', 'restricted', 'read', 'write'");
		}catch(Exception e){
			if(e.getMessage().compareTo("Missing key") == 0)
				throw new Exception("Truong shared_users phai duoc dinh nghia la JSONArray chua cac JSONObject, co dang\n[{\n'username':username,\n'accessibility':accessibility\n},...]");
		}
			
		
		if ((new_access != null || shared_users != null) && (user.getId().equals(owner.getId().toString()))) 
			throw new Exception(String.format("Nguoi dung %s khong the thay doi kha nang truy cap file %s trong folder %s cua nguoi dung %s",
					user.getUsername(),filename,edit_path,owner.getUsername()));
		
		Folder edit_folder = null;
		Folder new_folder = null;
		FileM edit_file = null;
		FileM new_file = null;
		try {
			edit_folder = new Folder(owner,edit_path,conn);
		}catch(Exception e){
			edit_folder = Folder.createFolder(owner, edit_path, conn);
		}
		try {
			edit_file = new FileM(owner,edit_folder,filename,conn);
		}catch(Exception e){
			edit_file = FileM.createFile(owner, edit_folder,filename, conn);
		}
		
		try {
			new_path = (String) json.get("new_path");
			new_filename = filename;
			while(Files.exists(new File(local_path + "/" + owner.getUsername() + "/" + new_path + "/" + new_filename).toPath())) {
				String[] splitted = new_filename.split("\\.");
				new_filename = "";
				for(int i = 0; i < splitted.length-1;i++) {
					new_filename += splitted[i] + (i == splitted.length-2 ? " - Copy" :"") + ".";
				}
				if(splitted.length >= 2) new_filename += splitted[splitted.length - 1];
			}
			
		}catch(Exception e){}
		
		if(new_path!=null) {
			acc = FileM.getFileAccessibility(user, owner, new_path,new_filename, conn).getCode();
		 	if(acc.compareTo("write") !=0)
				throw new SQLException(String.format("Nguoi dung %s khong the di chuyen file %s tu folder %s sang folder %s cua nguoi dung %s",
						user.getUsername(),filename,edit_path,new_path,owner.getUsername()));
		 	String copy_method;
		 	try {
		 		copy_method = (String) json.get("copy_method");
		 		if(copy_method.compareTo("copy")!=0 && copy_method.compareTo("move")!=0) throw new Exception();
		 	}catch(Exception e){
		 		throw new Exception("Cac phuong thuc copy(copy_method) co the su dung la 'copy' va 'move'");
		 	}
			switch(copy_method) {
				case "move":
					directMoveFile(owner,local_path,edit_path,new_path,filename,conn);
					edit_folder = null;
					edit_file = null;
					message += String.format("File %s trong Folder %s cua nguoi dung %s da duoc di chuyen sang Folder %s \n",
							filename,edit_path,owner.getUsername(),new_path);
					break;
				case "copy":
					directCopyFile(owner,local_path,edit_path,new_path,filename,conn);
					message += String.format("File %s trong Folder %s cua nguoi dung %s da duoc copy sang Folder %s \n",
							filename,edit_path,owner.getUsername(),new_path);
					break;
			}
			try {
				new_folder = new Folder(user,new_path,conn);
			}catch(Exception e) {
				new_folder = Folder.createFolder(user,new_path,conn);
			}
			try {
				new_file = new FileM(owner,new_folder,new_filename,conn);
			}catch(Exception e){
				new_file = FileM.createFile(owner, new_folder,new_filename, conn);
			}
		}
		if(edit_file != null) {
			if(new_access!=null) {
				edit_file.setAccessibility(new_access);
				message += String.format("File %s trong Folder %s cua nguoi dung %s duoc doi quyen truy cap thanh %s \n",
						filename,edit_path,owner.getUsername(),new_access.getCode());
			}
			if(shared_users != null) {
				JSONObject user_accessibility = null;
				User sh_user = null;
				Accessibility sh_acc = null;
				for(int i = 0;i < shared_users.size();i++) {
					user_accessibility = (JSONObject)shared_users.get(i);
					sh_user = new User(user_accessibility.get("username").toString(),conn);
					sh_acc = new Accessibility(user_accessibility.get("accessibility").toString(),conn);
					
					try {
						User_Shared_File.createUserSharedFile(sh_user,
								edit_file, sh_acc, conn);
					}catch(Exception e) {
						new User_Shared_File(sh_user,edit_file,conn).setAccessibility(sh_acc);
					}
					message += String.format("File %s Folder %s cua nguoi dung %s duoc chia se voi nguoi dung %s voi quyen truy cap la %s \n",
							filename,edit_path,owner.getUsername(),sh_user.getUsername(),sh_acc.getCode());
				}
			}
		}
		if(new_file != null) {
			if(new_access!=null) {
				new_file.setAccessibility(new_access);
				message += String.format("File %s trong Folder %s cua nguoi dung %s duoc doi quyen truy cap thanh %s \n",
						new_filename,new_path,owner.getUsername(),new_access.getCode());
			}
			if(shared_users != null) {
				JSONObject user_accessibility = null;
				User sh_user = null;
				Accessibility sh_acc = null;
				for(int i = 0;i < shared_users.size();i++) {
					user_accessibility = (JSONObject)shared_users.get(i);
					sh_user = new User(user_accessibility.get("username").toString(),conn);
					sh_acc = new Accessibility(user_accessibility.get("accessibility").toString(),conn);
					
					try {
						User_Shared_File.createUserSharedFile(sh_user,
								new_file, sh_acc, conn);
					}catch(Exception e) {
						new User_Shared_File(sh_user,new_file,conn).setAccessibility(sh_acc);
					}
					message += String.format("File %s Folder %s cua nguoi dung %s duoc chia se voi nguoi dung %s voi quyen truy cap la %s \n",
							new_filename,new_path,owner.getUsername(),sh_user.getUsername(),sh_acc.getCode());
				}
				
			}
		}
		
		return response;
	}
	public static void deleteFile(User user,User owner,String local_path,JSONObject json,Connection conn) throws Exception {
		String deleting_path = null;
		String deleting_filename = null;

		if(json.get("path") == null) throw new Exception("Yeu cau bo sung thu muc cua file can xoa vao truong path");
		deleting_path = (String) json.get("path");
		if(json.get("filename") == null) throw new Exception("Yeu cau bo sung ten file can xoa vao truong filename");
		deleting_filename = (String) json.get("filename");
		
		File check_file = new File(local_path + "/" + owner.getUsername() + "/" + deleting_path + "/" + deleting_filename);
		
		if((!Files.exists(check_file.toPath())) || (!check_file.isFile()))
			throw new Exception(String.format("File %s trong folder %s cua nguoi dung %s khong ton tai",deleting_filename,deleting_path,owner.getUsername()));
				
		String acc = FileM.getFileAccessibility(user, owner, deleting_path,deleting_filename, conn).getCode();
		
		if(acc.compareTo("write") !=0)
			throw new Exception(String.format("Nguoi dung %s khong the xoa folder %s cua nguoi dung %s",
					user.getUsername(),deleting_path,owner.getUsername()));
		
		directDeleteFile(owner,local_path,deleting_path,deleting_filename,conn);
	}
	public static void directCopyFolder(User owner,String local_path,String old_path,String new_path,Connection conn) throws Exception {
		String real_old_path = local_path + "/" + owner.getUsername() + "/" + old_path;
		String real_new_path = local_path + "/" + owner.getUsername() + "/" + new_path;
		
		File old_dir = new File(real_old_path);
		
		if((!Files.exists(old_dir.toPath())) || (!old_dir.isDirectory()))
			throw new Exception(String.format("Folder %s khong ton tai tren tai khoan %s",old_path,owner.getUsername()));
		
		
		File new_dir = new File(real_new_path);
		new_dir.mkdirs();	
		Folder old_folder;
		try {
			old_folder = new Folder(owner,old_path,conn); 
		}catch(Exception e){
			old_folder = Folder.createFolder(owner, old_path, conn);
		}
		Folder new_folder;
		try {
			new_folder = new Folder(owner,new_path,conn); 
		}catch(Exception e){
			new_folder = Folder.createFolder(owner, new_path, conn);
		}
		
		for(String p: old_dir.list()) {
			File f = new File(real_old_path + "/" + p);
			if(f.isDirectory()) {
				String sub_old_folder = real_old_path + "/" + p;
				String sub_new_folder = p;
				while(Files.exists(new File(real_new_path + "/" + sub_new_folder).toPath())) sub_new_folder += " - Copy";
				String final_sub_final_folder = real_new_path + "/" + sub_new_folder;
				
				Files.walk(Paths.get(sub_old_folder))
			      .forEach(source -> {
			    	  
			          Path destination = Paths.get(final_sub_final_folder, source.toString()
			            .substring(sub_old_folder.length()));
			          try {
			              Files.copy(source, destination);
			          } catch (Exception e) {
			              e.printStackTrace();
			          }
			      });
					    
				try {
					Folder.createFolder(owner, new_path + "/" + sub_new_folder, conn);
				}catch(Exception e) {}
				
			}else{
				String sub_new_file = p;
				boolean created = false;
				while(!created) {
					try {
						Files.copy(new File(real_old_path + "/" + p).toPath(), new File(real_new_path + "/" + sub_new_file).toPath());
						created = true;
					}catch(NoSuchFileException e){
						new File(real_new_path).mkdirs();
						Files.copy(new File(real_old_path + "/" + p).toPath(), new File(real_new_path + "/" + sub_new_file).toPath());
						created = true;
					}catch(FileAlreadyExistsException e) {
						String[] splitted = sub_new_file.split("\\.");
						sub_new_file = "";
						for(int i = 0; i < splitted.length-1;i++) {
							sub_new_file += splitted[i] + (i == splitted.length-2 ? " - Copy" :"") + ".";
						}
						if(splitted.length >= 2) sub_new_file += splitted[splitted.length - 1];
					}
				}
				try {
					FileM.createFile(owner, new_folder ,sub_new_file, conn);
				}catch(Exception e) {}
			}
		}
	}
	
	public static void directDeleteFolder(File f,User owner,String path,Connection conn) throws Exception{
	  if((!Files.exists(f.toPath()))||(!f.isDirectory()))
		 throw new Exception(String.format("Folder %s khong ton tai tren tai khoan %s",path,owner.getUsername()));
	  try {
		  List<FileM> deleting_files = FileM.getSubFiles(owner, path, conn);
		  for(FileM file: deleting_files) {
			  for(User_Shared_File usf: User_Shared_File.getAllUserSharedFiles(file, conn)) usf.delete();
			  file.delete();
		  }
		  List<Folder> deleting_folders = Folder.getSubFolders(owner, path, conn);
		  for(Folder folder: deleting_folders) {
			  for(User_Shared_Folder usf: User_Shared_Folder.getAllUserSharedFolders(folder, conn)) usf.delete();
			  folder.delete();
		  }
		  
	  }catch(Exception e) {
		  e.printStackTrace();
	  } 
	 
	  for (File sub_f : f.listFiles()) {
	     if(sub_f.isDirectory()) {
	        directDeleteFolder(sub_f,owner,path+sub_f.getName(),conn);
	     } else {
		   	  sub_f.delete();
	     }
	  }
	  f.delete();
   }
	public static void directDeleteFolder(User owner,String local_path,String path,Connection conn) throws Exception {
		File f = new File(local_path + "/" + owner.getUsername() + "/" + path);
		directDeleteFolder(f,owner,path,conn);
	}
	public static void directMoveFolder(User owner,String local_path,String old_path,String new_path,Connection conn) throws Exception {
		directCopyFolder(owner,local_path,old_path,new_path,conn);
		File f = new File(local_path + "/" + owner.getUsername() + "/" + old_path);
		directDeleteFolder(f,owner,old_path,conn);
	}
	public static void directCopyFile(User owner,String local_path,String old_path,String new_path,String filename,Connection conn) throws Exception {
		String real_old_path = local_path + "/" + owner.getUsername() + "/" + old_path + "/" + filename;
		File old_dir = new File(real_old_path);
		if((!Files.exists(old_dir.toPath()))||(!old_dir.isFile()))
			throw new Exception(String.format("File %s khong ton tai trong Folder %s cua nguoi dung %s",filename,old_path,owner.getUsername()));
		String new_filename = filename;
		while(Files.exists(new File(local_path + "/" + owner.getUsername() + "/" + new_path + "/" + new_filename).toPath())) {
			String[] splitted = new_filename.split("\\.");
			new_filename = "";
			for(int i = 0; i < splitted.length-1;i++) {
				new_filename += splitted[i] + (i == splitted.length-2 ? " - Copy" :"") + ".";
			}
			if(splitted.length >= 2) new_filename += splitted[splitted.length - 1];
		}
		
		String real_new_path = local_path + "/" + owner.getUsername() + "/" + new_path + "/" + new_filename;
		new File(local_path + "/" + owner.getUsername() + "/" + new_path).mkdirs();
		Files.copy(new File(real_old_path).toPath(), new File(real_new_path).toPath());
		
		Folder folder;
		try {
			folder = new Folder(owner,new_path,conn);
		}catch(Exception e){
			folder = Folder.createFolder(owner, new_path, conn);
		}
		FileM filem;
		try {
			filem = new FileM(owner,folder,filename,conn);
		}catch(Exception e){
			filem = FileM.createFile(owner,folder,filename, conn);
		}
	}
	public static void directDeleteFile(User owner,String local_path,String path,String filename,Connection conn) throws Exception {
		String real_path = local_path + "/" + owner.getUsername() + "/" + path + "/" + filename;
		File dir = new File(real_path);
		if((!Files.exists(dir.toPath()))||(!dir.isFile()))
			throw new Exception(String.format("File %s khong ton tai trong Folder %s cua nguoi dung %s",filename,path,owner.getUsername()));
		try {
			Folder folderm = new Folder(owner,path,conn);
			FileM filem = new FileM(owner,folderm,filename,conn);
			
			for(User_Shared_File usf: User_Shared_File.getAllUserSharedFiles(filem, conn)) 
				usf.delete();
			filem.delete();
		}catch(Exception e){
		}
		
		dir.delete();
		
	}
	public static void directMoveFile(User owner,String local_path,String old_path,String new_path,String filename,Connection conn) throws Exception {
		directCopyFile(owner,local_path,old_path,new_path,filename,conn);
		directDeleteFile(owner,local_path,old_path,filename,conn);
	}
}
