import java.io.File;
import java.util.HashMap;

public class filter {
	HashMap<String, Integer> spam = new HashMap<String, Integer>();
	HashMap<String, Integer> ham = new HashMap<String, Integer>();
	public static void main (String[] args) {
		String files;
		/*Prints out the arguments. Just for testing*/
        for (String s: args) {
            System.out.println(s);
        }
        /*The first argument is the training folder*/
		String path = args[0]; 
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();
		/*Loops through files in the training directory*/
		for (File f: listOfFiles) {
			if (f.isFile()) 
			{
				files = f.getName();
				if (files.endsWith(".txt") || files.endsWith(".TXT")) {
					if(files.startsWith("ham")) {

					} else if (files.startsWith("spam")) {

					}
				  System.out.println(files);
				}
			}
		}
    }
    public static void readFromFile(String filename) {

    }
}
