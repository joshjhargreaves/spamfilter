import java.io.File;
import java.util.*;
import java.util.HashMap;
import java.util.Scanner;
import java.io.FileNotFoundException;

public class Filter {
    Map<String, Integer> m_spam = new HashMap<String, Integer>();
    Map<String, Integer> m_ham = new HashMap<String, Integer>();
    Map<String, Double> m_spam_prob = new HashMap<String, Double>();
    Map<String, Double> m_ham_prob = new HashMap<String, Double>();

	public static void main (String[] args) {
		String files;
        Filter filter = new Filter();
        /*The first argument is the training folder*/
		String path = args[0]; 
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();
        int total_spam=0, total_ham=0, total=0;
        float ham_Prior, spam_Prior;
        String classification;

		/*Loops through files in the training directory*/
		for (File f: listOfFiles) {
			if (f.isFile()) {
				files = f.getName();
				if (files.endsWith(".txt") || files.endsWith(".TXT")) {
					if(files.startsWith("ham")) {
						filter.readFromFile(f, false);
                        total_ham ++;
					} else if (files.startsWith("spam")) {
						filter.readFromFile(f, true);
                        total_spam ++;
					}
				}
			}
		}
        /*Calculates the probabilities*/
        filter.calcProbabilities(true);
        filter.calcProbabilities(false);
        /*Calculates priors*/
        total = total_ham + total_spam;
        ham_Prior = (float)total_ham/(float)total;
        spam_Prior = (float)total_spam/(float)total;
        /*The second argument is the test data*/
        path = args[1]; 
        File f = new File(path);
        if (f.isFile()) {
            double spam_probs = filter.multProbabilities(true, f);
            double ham_probs = filter.multProbabilities(false, f);
            
            double arg_spam = spam_Prior*spam_probs;
            double arg_ham = ham_Prior*ham_probs;

            if((arg_spam-arg_ham) > 0)
                classification = "spam";
            else
                classification = "ham";
            System.out.println(classification);
        }

    }
    public void readFromFile(File filename, boolean spamFlag) {

    	Scanner sc = null;
    	try {
    		sc = new Scanner(filename);
    		//useDelimiter(("?<=\\s\\w{1,10})[^\\w\\s])?\\s|[^\\w\\s]$"));
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

    public double multProbabilities(boolean spamFlag, File filename) {
        double product = 1;
        Scanner sc = null;
        double prob;
        try {
            sc = new Scanner(filename);
            //useDelimiter(("?<=\\s\\w{1,10})[^\\w\\s])?\\s|[^\\w\\s]$"));
        } catch (FileNotFoundException e) {
            System.out.println(e);
        }
        while (sc.hasNext()) {
            String w = sc.next();
            if(m_spam_prob.containsKey(w)) {
                if (spamFlag){
                    prob = m_spam_prob.get(w);
                } else {
                    prob = m_ham_prob.get(w);
                }
                product *= prob;
            }
        }
        return product;
    }
}
