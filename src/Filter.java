import java.io.File;
import java.util.HashMap;
import java.util.Scanner;
import java.io.FileNotFoundException;

public class Filter {
	HashMap<String, Integer> m_spam = new HashMap<String, Integer>();
	HashMap<String, Integer> m_ham = new HashMap<String, Integer>();
    int m_spam_count = 0;
    int m_ham_count = 0;
	public static void main (String[] args) {
		String files;
        Filter filter = new Filter();
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
			if (f.isFile()) {
				files = f.getName();
				if (files.endsWith(".txt") || files.endsWith(".TXT")) {
					if(files.startsWith("ham")) {
						filter.readFromFile(f.toString(), false);
					} else if (files.startsWith("spam")) {
						filter.readFromFile(f.toString(), true);
					}
				}
			}
		}
    }
    public void readFromFile(String filename, boolean spamFlag) {
    	Scanner sc = null;
    	try {
    		sc = new Scanner(new File(filename));
    		//useDelimiter(("?<=\\s\\w{1,10})[^\\w\\s])?\\s|[^\\w\\s]$"));
    	} catch (FileNotFoundException e) {
    		System.out.println(e);
    	}
    	while (sc.hasNext()) {
    		String w = sc.next();
    		System.out.println(w);
    		if(spamFlag == true) {
    			if(m_spam.containsKey(w) == true) {
    				m_spam.put(w, m_spam.get(w) + 1);
                    m_spam_count++;
    			} else {
    				m_ham.put(w, 1);
                    m_spam.put(w, 2);
                    m_spam_count+=2;
                    m_ham_count++;
    			}
	    	} else {
	    		if(m_ham.containsKey(w) == true) {
    				m_ham.put(w, m_ham.get(w) + 1);
                    m_ham_count++;
    			} else {
                    m_ham.put(w, 2);
                    m_spam.put(w, 1);
                    m_ham_count+=2;
                    m_spam_count++;
    			}
	    	}
    	}
    	sc.close();
    }


}
