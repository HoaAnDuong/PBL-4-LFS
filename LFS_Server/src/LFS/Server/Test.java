package LFS.Server;

import java.io.File;
import java.math.BigInteger;
import java.nio.file.Path;
import static java.nio.file.StandardCopyOption.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import LFS.Models.FileM;
import LFS.Models.Folder;
import LFS.Models.User;
import LFS.Utils.*;

public class Test {
	public static void main(String args[]) throws Exception {
		String local_path = "D:/PBL4/Shared_Folder";
		String url = "jdbc:sqlite:"+ local_path +"/LFS.db";
		Connection conn = DriverManager.getConnection(url);
		User user = new User("hoaan123",conn);
		Folder folder = new Folder(user,"foo/bar/foobar",conn);
		JSONObject json = new JSONObject();
		json.put("edit_path", "New Folder/Image");
//		json.put("new_path", "New Folder/Image");
//		json.put("copy_method", "copy");
		json.put("filename", "T2 - Copy.gif");
		
//		json.put("accessibility", "read");
//		JSONArray shared_list = new JSONArray();
//		JSONObject shared_user_1 = new JSONObject();
//		shared_user_1.put("username", "phitruong123");
//		shared_user_1.put("accessibility", "write");
//		shared_list.add(shared_user_1);
//		json.put("shared_users", shared_list);
		
		json = (JSONObject) new JSONParser().parse(json.toJSONString());
		
		JSONObject json2 = new JSONObject();
		json2.put("path", "New Folder/Image");
		json2.put("filename", null);
		System.out.println(json2);
		
		//new Zip().zipFolder(local_path + "/" + user.getUsername() + "/" + "New Folder",local_path + "/" + "Temp", "New Folder.zip");
		//Utils.deleteFile(new User("hoaan123",conn),new User("hoaan123",conn),local_path,json2,conn);
		
		//Utils.directDeleteFile(user, local_path, "New Folder/Image", "T2.gif", conn);
		
		//Utils.editFolder(new User("hoaan123",conn),new User("hoaan123",conn),local_path,json,conn);
		//Utils.deleteFolder(new User("hoaan123",conn),new User("hoaan123",conn),local_path,json2,conn);
		//Utils.directDeleteFolder(user, local_path, "New Folder/Image", conn);
//		Utils.directDeleteFolder(user, local_path, "foo/bar/Image - Copy", conn);
		//Utils.directMoveFolder(user,local_path,"foo/Image","Image",conn);
		//Utils.editFolder(user, user, local_path, json, conn);
		//System.out.println(Folder.getFolderAccessibility(new User("phitruong123",conn), user, "Image/brrr/foofoo", conn).getCode());	
		
	}
}
