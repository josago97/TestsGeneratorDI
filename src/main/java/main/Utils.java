package main;
import java.io.File;
import java.util.*;

public class Utils {

	public static List<File> listFiles(String path, String extension, boolean recursive) {
		List<File> files = new ArrayList<File>();
		
		File directory = new File(path);
		
		for(File file : directory.listFiles()) {
			if(file.isFile() && extension != null && file.getName().endsWith(extension)) {
				files.add(file);
			} else if(file.isDirectory() && recursive){
				files.addAll(listFiles(file.getPath(), extension, true));
			}
		}
		
		return files;		
	}
}
