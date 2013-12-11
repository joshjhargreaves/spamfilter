import java.io.File;
import java.util.*;
import java.util.HashMap;
import java.util.Scanner;
import java.io.FileNotFoundException;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.lang.Math;

public class Filter {
    Map<String, Double> m_spam_prob = new HashMap<String, Double>();
    Map<String, Double> m_ham_prob = new HashMap<String, Double>();
    double ham_Prior;
    double spam_Prior;

    public static void main (String[] args) {
        Filter filter = new Filter();
        filter.readTrainingFile();
        String classification;
        System.out.println(filter.filterFile(new File(args[0])));
    }
    public String filterFile(File f) {
         /*The first argument is the test data*/
        String classification = null;
        if (f.isFile()) {
            classification = new String();
            double spam_probs = this.multProbabilities(true, f);
            double ham_probs = this.multProbabilities(false, f);

            double arg_spam = Math.log(this.spam_Prior)+spam_probs;
            double arg_ham = Math.log(this.ham_Prior)+ham_probs;

            if((arg_spam-arg_ham) > 0)
                classification = "spam";
            else
                classification = "ham";
            return classification;
        }
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
    
    public void readTrainingFile(){
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("training.txt"));
            String currentLine;
            String[] lineArray;
            currentLine = br.readLine();
            lineArray = currentLine.split(" ");
            ham_Prior = Double.parseDouble(lineArray[0]);
            spam_Prior = Double.parseDouble(lineArray[1]);
            
            while ((currentLine = br.readLine()) != null) {
                lineArray = currentLine.split(" ");
                String key = lineArray[0];
                double ham_prob = Double.parseDouble(lineArray[1]);
                double spam_prob = Double.parseDouble(lineArray[2]);
                m_ham_prob.put(key, ham_prob);
                m_spam_prob.put(key, spam_prob);
            }
        } catch (java.io.IOException e) {
            System.out.println(e);
        }
    }
}
