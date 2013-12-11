import java.io.File;
import java.util.*;
import java.util.HashMap;
import java.util.Scanner;
import java.io.FileNotFoundException;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.lang.String;
import java.util.Arrays;

public class Train {
    Map<String, Integer> m_spam = new HashMap<String, Integer>();
    Map<String, Integer> m_ham = new HashMap<String, Integer>();
    Map<String, Double> m_spam_prob = new HashMap<String, Double>();
    Map<String, Double> m_ham_prob = new HashMap<String, Double>();
    int total_spam=0, total_ham=0;


	public static void main (String[] args) {
        Train training = new Train();
        double ham_Prior, spam_Prior;
        if(args.length > 1 && args[0].equals("-xval")) {
            String path = args[1]; 
            File folder = new File(path);
            File[] listOfFiles = folder.listFiles();
            int chunk = listOfFiles.length/10;
            int startIndex = 0, endIndex = 0;
            for(int i=0; i<10; i++) {
                training.total_spam=0;
                training.total_ham=0;
                startIndex=(i*chunk);
                endIndex=startIndex+chunk-1;
                File[] test = Arrays.copyOfRange(listOfFiles, startIndex, endIndex);
                if(startIndex != 0) {
                    File[] train1 = Arrays.copyOfRange(listOfFiles, 0, startIndex-1);
                    training.trainFromFiles(train1);
                }
                if (endIndex != listOfFiles.length -1) {
                    File[] train2 = Arrays.copyOfRange(listOfFiles, endIndex, listOfFiles.length-1);
                    training.trainFromFiles(train2);
                }

                int total=0;
                /*Calculates the probabilities*/
                training.calcProbabilities(true);
                training.calcProbabilities(false);
                
                /*Calculates priors*/
                total = training.total_ham + training.total_spam;
                ham_Prior = (double)training.total_ham/(double)total;
                spam_Prior = (double)training.total_spam/(double)total;

                for(File f: test) {
                    training.filterFile(f, ham_Prior, spam_Prior);
                }

                training.m_ham.clear();
                training.m_ham_prob.clear();
                training.m_spam.clear();
                training.m_spam_prob.clear();
            }
        } else {
    		String files;
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
    						training.readFromFile(f, false);
                            training.total_ham ++;
    					} else if (files.startsWith("spam")) {
    						training.readFromFile(f, true);
                            training.total_spam ++;
    					}
    				}
    			}
    		}
            int total=0;
    		/*Calculates the probabilities*/
            training.calcProbabilities(true);
            training.calcProbabilities(false);
            
            /*Calculates priors*/
            total = training.total_ham + training.total_spam;
            ham_Prior = (double)training.total_ham/(double)total;
            spam_Prior = (double)training.total_spam/(double)total;
            
            /* Write training file */
            training.writeToFile(ham_Prior, spam_Prior);
        }
    }
    public void trainFromFiles(File[] inFiles) {
        String files = new String();
        for (File f: inFiles) {
                if (f.isFile()) {
                    files = f.getName();
                    if (files.endsWith(".txt") || files.endsWith(".TXT")) {
                        if(files.startsWith("ham")) {
                            this.readFromFile(f, false);
                            this.total_ham ++;
                        } else if (files.startsWith("spam")) {
                            this.readFromFile(f, true);
                            this.total_spam ++;
                        }
                    }
                }
            }
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
            out.write(ham_Prior + " " + spam_Prior);
            out.newLine();
            for (Map.Entry<String, Double> entry : m_ham_prob.entrySet()) {
                String key = entry.getKey();
                double ham_value = entry.getValue();
                double spam_value = m_spam_prob.get(key);
                out.write(key+" "+ham_value+" "+spam_value);
                out.newLine();
            }
            out.close();
        } catch (java.io.IOException e) {
            System.out.println(e);
        }
    }

    public String filterFile(File f, double ham_Prior, double spam_Prior) {
         /*The first argument is the test data*/
        String classification = null;
        double arg_ham = -1.0, arg_spam = -1.0;
        if (f.isFile()) {
            classification = new String();
            double spam_probs = multProbabilities(true, f);
            double ham_probs = multProbabilities(false, f);

            arg_spam = Math.log(spam_Prior)*spam_probs;
            arg_ham = Math.log(ham_Prior)*ham_probs;

            if((arg_spam-arg_ham) > 0)
                classification = "spam";
            else
                classification = "ham";
        }
        System.out.println(arg_spam + " " + arg_ham);
        return classification;
    }

    public double multProbabilities(boolean spamFlag, File filename) {
        double product = 0;
        Scanner sc = null;
        double prob;
        try {
            sc = new Scanner(filename);
        //    sc.useDelimiter(("((?<=\\s\\w{1,10})[^\\w\\s])?\\s|[^\\w\\s]$"));
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
                product += Math.log(prob);
            }
        }
        return product;
    }

}
