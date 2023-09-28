package LFS.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zip {
	List<String> filesListInDir = new ArrayList<String>();
	private void populateFilesList(File dir) throws IOException {
        File[] files = dir.listFiles();
        for(File file : files){
            if(file.isFile()) filesListInDir.add(file.getAbsolutePath());
            else populateFilesList(file);
        }
	}
      
	public String zipFolder(String source_path, String dest_path,String zipname) throws Exception {

        	
    	File dir = new File(source_path);
        populateFilesList(dir);
        
        new File(dest_path).mkdirs();
        
        FileOutputStream fos = new FileOutputStream(dest_path + "/" + zipname);
        ZipOutputStream zos = new ZipOutputStream(fos);
        for(String filePath : filesListInDir){
            ZipEntry ze = new ZipEntry(filePath.substring(dir.getAbsolutePath().length()+1, filePath.length()));
            zos.putNextEntry(ze);
            FileInputStream fis = new FileInputStream(filePath);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }
            zos.closeEntry();
            fis.close();
        }
        zos.close();
        fos.close();

        return dest_path + "/" + zipname;
    }
}
