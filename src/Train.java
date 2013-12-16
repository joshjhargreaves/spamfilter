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
    List<String> stopwords = new ArrayList<String>();
    int total_spam=0, total_ham=0;

	public static void main (String[] args) {
        Train training = new Train();
        training.readInStopWords();
        double ham_Prior, spam_Prior;
        if(args.length > 1 && args[0].equals("-xval")) {
            String path = args[1]; 
            File folder = new File(path);
            File[] listOfFiles = folder.listFiles();
            int chunk = listOfFiles.length/10;
            int startIndex = 0, endIndex = 0;
            double correct, incorrect;
            double true_positive, true_negative, false_positive, false_negative;
            double error = 0;
            double av_false_positive = 0;
            double av_false_negative = 0;
            double errors[] = new double[10];

            int test_ham_total = 0;
            int test_spam_total = 0;
            for(int i=0; i<10; i++) {
                correct = 0;
                incorrect = 0;

                true_positive = 0;
                true_negative = 0;
                false_positive = 0;
                false_negative = 0;

                training.total_spam=0;
                training.total_ham=0;
                startIndex=(i*chunk);
                endIndex=startIndex+chunk;
                File[] test = Arrays.copyOfRange(listOfFiles, startIndex, endIndex);
                if(startIndex != 0) {
                    File[] train1 = Arrays.copyOfRange(listOfFiles, 0, startIndex);
                    training.trainFromFiles(train1);
                }
                if (endIndex != listOfFiles.length) {
                    File[] train2 = Arrays.copyOfRange(listOfFiles, endIndex, listOfFiles.length);
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

                    String result = training.filterFile(f, ham_Prior, spam_Prior);

                    if(f.getName().startsWith("ham")) {

                        test_ham_total++;

                        if (result.equals("ham"))
                            true_positive++;
                        else
                            false_positive++;

                    } else {

                        test_spam_total++;

                        if (result.equals("spam"))
                            true_negative++;
                        else
                            false_negative++;
                    }
                }

                correct = true_positive + true_negative;
                incorrect = false_positive + false_negative;

                false_positive /= (double)test_ham_total;
                false_negative /= (double)test_spam_total;
                av_false_positive += false_positive;
                av_false_negative += false_negative;

                error+=(double)incorrect/((double)correct + (double)incorrect);
                errors[i] = ((double)incorrect/((double)correct + (double)incorrect))*100;
                System.out.println("Error[" + i + "] = " + errors[i]);
                training.m_ham.clear();
                training.m_ham_prob.clear();
                training.m_spam.clear();
                training.m_spam_prob.clear();
            }
            System.out.println(av_false_positive/10);
            System.out.println(av_false_negative/10);
            System.out.println(100*error/10 + "%");
            Statistics stat = new Statistics(errors);
            System.out.println("Error std:dev = " + stat.getStdDev());
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
   public void readInStopWords() {
        Scanner sc = null;
        try {
            sc = new Scanner(new File("stopwords.txt")).useDelimiter(",");
        } catch (Exception e) {
            System.out.println("Cannot find stopwords.txt");
        }
        while (sc.hasNext()) {
            String w = sc.next();
            stopwords.add(w);
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
        boolean subjectSeen = false;
    	try {
    		sc = new Scanner(filename);
    		//sc.useDelimiter(("((?<=\\s\\w{1,10})[^\\w\\s])?\\s|[^\\w\\s]$"));
    	} catch (FileNotFoundException e) {
    		System.out.println(e);
    	}
    	while (sc.hasNextLine()) {
    		String words = sc.nextLine();
            String[] wordsArray = words.split(" ");
            if(words.toLowerCase().startsWith("subject:") && subjectSeen == false) {
                subjectSeen = true;
                for(int i=1; i<wordsArray.length;i++)
                    wordsArray[i] = "subject" + wordsArray[i];
            }
            //if(subjectSeen) {
                for(String w: wordsArray) {
                    w = w.replaceAll("[^A-Za-z0-9']", "");
                    if(!w.equals("") && !w.equals(" ") && !stopwords.contains(w)) {
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
                }
           // }
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
        } else {
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

            arg_spam = Math.log(spam_Prior)+spam_probs;
            arg_ham = Math.log(ham_Prior)+ham_probs;

            if((arg_spam-arg_ham) > 0)
                classification = "spam";
            else
                classification = "ham";
        }
        return classification;
    }

    public double multProbabilities(boolean spamFlag, File filename) {
        double product = 0;
        Scanner sc = null;
        double prob;
        boolean subjectSeen = false;
        try {
            sc = new Scanner(filename);
        //    sc.useDelimiter(("((?<=\\s\\w{1,10})[^\\w\\s])?\\s|[^\\w\\s]$"));
        } catch (FileNotFoundException e) {
            System.out.println(e);
        }
        while (sc.hasNextLine()) {
            String words = sc.nextLine();
            String[] wordsArray = words.split(" ");
            if(words.toLowerCase().startsWith("subject:") && !subjectSeen) {
                subjectSeen = true;
                for(int i=1; i<wordsArray.length; i++) {
                    wordsArray[i] = "subject" + wordsArray[i];
                }
            }
            //if(subjectSeen) {
                for(String w: wordsArray) {
                    w = w.replaceAll("[^A-Za-z0-9']", "");
                    if(!w.equals("") && !w.equals(" ") && !stopwords.contains(w)) {
                        if(m_spam_prob.containsKey(w)) {
                            if (spamFlag){
                                prob = m_spam_prob.get(w);
                            } else {
                                prob = m_ham_prob.get(w);
                            }
                            product += Math.log(prob);
                        }
                    }
                }
           // }
        }
        return product;
    }

    public void writeWords()  {
        BufferedWriter spamout = null, hamout = null;
        try {
            spamout = new BufferedWriter(new FileWriter("spamWords.txt"));
            hamout = new BufferedWriter(new FileWriter("hamWords.txt"));
            for (Map.Entry<String, Integer> entry : m_ham.entrySet()) {
                String key = entry.getKey();
                double ham_value = entry.getValue();
                double spam_value = m_spam_prob.get(key);
                hamout.write(key+" "+ham_value);
                hamout.newLine();
            }
            for (Map.Entry<String, Integer> entry : m_spam.entrySet()) {
                String key = entry.getKey();
                double ham_value = entry.getValue();
                double spam_value = m_spam_prob.get(key);
                spamout.write(key+" "+ham_value);
                spamout.newLine();
            }
            hamout.close();
            spamout.close();
        } catch (java.io.IOException e) {
            System.out.println(e);
        }
    }
}