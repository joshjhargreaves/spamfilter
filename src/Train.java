import java.io.File;
import java.util.*;
import java.util.HashMap;
import java.util.Scanner;
import java.io.FileNotFoundException;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;

public class Train {
    Map<String, Integer> m_spam = new HashMap<String, Integer>();
    Map<String, Integer> m_ham = new HashMap<String, Integer>();
    Map<String, Double> m_spam_prob = new HashMap<String, Double>();
    Map<String, Double> m_ham_prob = new HashMap<String, Double>();

	public static void main (String[] args) {
		String files;
        Train training = new Train();
        /*The first argument is the training folder*/
		String path = args[0]; 
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();
        int total_spam=0, total_ham=0, total=0;
        float ham_Prior, spam_Prior;

		/*Loops through files in the training directory*/
		for (File f: listOfFiles) {
			if (f.isFile()) {
				files = f.getName();
				if (files.endsWith(".txt") || files.endsWith(".TXT")) {
					if(files.startsWith("ham")) {
						training.readFromFile(f, false);
                        total_ham ++;
					} else if (files.startsWith("spam")) {
						training.readFromFile(f, true);
                        total_spam ++;
					}
				}
			}
		}
        
		/*Calculates the probabilities*/
        training.calcProbabilities(true);
        training.calcProbabilities(false);
        
        /*Calculates priors*/
        total = total_ham + total_spam;
        ham_Prior = (float)total_ham/(float)total;
        spam_Prior = (float)total_spam/(float)total;
        
        /* Write training file */
        training.writeToFile(ham_Prior, spam_Prior);
    }
    
    public void readFromFile(File filename, boolean spamFlag) {
    	Scanner sc = null;
    	try {
    		sc = new Scanner(filename);
    		//sc.useDelimiter(("((?<=\\s\\w{1,10})[^\\w\\s])?\\s|[^\\w\\s]$"));
    	} catch (FileNotFoundException e) {
    		System.out.println(e);
    	}
    	while (sc.hasNext()) {
    		String w = sc.next();
    		if(spamFlag == true) {
    			if(m_spam.containsKey(w) == true) {
    				m_spam.put(w, m_spam.get(w) + 1);
    			} else {
    				m_ham.put(w, 1);
                    m_spam.put(w, 2);
    			}
	    	} else {
	    		if(m_ham.containsKey(w) == true) {
    				m_ham.put(w, m_ham.get(w) + 1);
    			} else {
                    m_ham.put(w, 2);
                    m_spam.put(w, 1);
    			}
	    	}
    	}
        sc.close();
    }

    public void calcProbabilities(boolean spamFlag) {
        Iterator it = m_ham.keySet().iterator();
        int distinct_words = m_ham.size();
        int n = 0;
        if (spamFlag){
            for (Map.Entry<String, Integer> entry : m_spam.entrySet()) {
                int value = entry.getValue();
                if(value > 1)
                    n += value - 1;
            }
            for (Map.Entry<String, Integer> entry : m_spam.entrySet()) {
                String key = entry.getKey();
                int value = entry.getValue();
                double temp = (double) ((double)value/((double)distinct_words + (double)n));
                m_spam_prob.put(key, temp);
            }
        }
        else {
            for (Map.Entry<String, Integer> entry : m_ham.entrySet()) {
                int value = entry.getValue();
                if(value > 1)
                    n += value - 1;
            }
            for (Map.Entry<String, Integer> entry : m_ham.entrySet()) {
                String key = entry.getKey();
                int value = entry.getValue();
                double temp = (double) ((double)value/((double)distinct_words + (double)n));
                m_ham_prob.put(key, temp);
            }
        }
    }
    
    public void writeToFile(double ham_Prior, double spam_Prior){
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter("training.txt"));
            out.write(ham_Prior + "," + spam_Prior);
            out.newLine();
            for (Map.Entry<String, Double> entry : m_ham_prob.entrySet()) {
                String key = entry.getKey();
                double ham_value = entry.getValue();
                double spam_value = m_spam_prob.get(key);
                out.write(key+","+ham_value+","+spam_value);
                out.newLine();
            }
            out.close();
        } catch (java.io.IOException e) {
            System.out.println(e);
        }
    }
}
