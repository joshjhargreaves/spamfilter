import java.io.File;

public class filter {
	public static void main (String[] args) {
		/*Prints out the arguments. Just for testing*/
		String files;
        for (String s: args) {
            System.out.println(s);
        }
        /*The first argument is the training folder*/
		String path = args[0]; 
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();
		/*Loops through files in the training directory*/
		for (File f: listOfFiles) 
		{
			if (f.isFile()) 
			{
				files = f.getName();
				if (files.endsWith(".txt") || files.endsWith(".TXT"))
				{
				  System.out.println(files);
				}
			}
		}
    }
}
