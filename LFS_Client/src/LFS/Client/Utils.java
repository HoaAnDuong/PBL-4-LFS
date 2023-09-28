package LFS.Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.Socket;

public class Utils {
	public static void sendFile(String path,Socket socket) throws Exception{
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
}
