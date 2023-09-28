package LFS.Client;

import java.io.DataOutputStream;
import java.net.Socket;
import LFS.Client.Action;

public class Main {
	public static void main(String args[]) throws Exception {
		Socket socket = new Socket("192.168.87.1",2343);
		System.out.println(Action.actionDownloadFolder("hoaan123","hoaan123","hoaan123","Image","D:/PBL4/Image.zip",socket));


//		System.out.println(Action.actionCreateFolder("hoaan123","hoaan123","hoaan213","foo/bar",socket));
//		socket = new Socket("192.168.87.1",2343);
//		System.out.println(Action.actionCreateFolder("hoaan123","hoaan123","hoaan123","foo/bar",socket));
		
	}
}
