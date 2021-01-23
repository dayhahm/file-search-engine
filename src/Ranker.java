import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.io.IOException;
import java.util.Scanner;
import java.io.FileInputStream;
import java.nio.file.*;
import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * Ranker is a thread that finds the most relevant/highly ranked FileCount objects for any word in the index.
 */
public class Ranker {
    /*
     * Takes in a keyword and returns an ordered/sorted list of files.
     * Constructor takes a string and an int
     * Priority queue
     * https://www.geeksforgeeks.org/implement-priorityqueue-comparator-java/
     *
     * add summary of the synchronization discipline.
     */

    private ConcurrentHashMap<String, PriorityQueue<FileCount>> wordToFileCount;
    private int n;

    /**
     * Creates a Ranker object that looks at the given index and sets the maximum number of FileCounts to be returned
     * to the specified num.
     * @param wordToFileCount the index
     * @param n max number of results to return
     */
    public Ranker(ConcurrentHashMap<String, PriorityQueue<FileCount>> wordToFileCount, int n) {
        this.wordToFileCount = wordToFileCount;
        this.n = n;
    }

    /**
     * Gets the top max number of FileCount objects. If there are less items at the index than the max number,
     * all objects are returned.
     * @param word the search term
     * @return an array of the top n FileCount objects
     * @throws Exception On index does not have the word
     */
    public FileCount[] rank(String word) throws Exception {
        PriorityQueue<FileCount> q;

        String[] words = word.split(" ");
        
        // case with two key words
        if (words.length > 1){
            if (wordToFileCount.containsKey(words[0]) && wordToFileCount.containsKey(words[1])) {
                q = twoWords(words[0], words[1]);
            }
            else {
                throw new Exception(String.format("no indexed files contain '%s' and/or '%s'", words[0], words[1]));
            }
        } 
        // case with one key word
        else {
            if (wordToFileCount.containsKey(word)) {
                synchronized (wordToFileCount.get(word)) { // need to synchronize as PriorityQueue is not thread safe
                    q = wordToFileCount.get(word);
                }
            } else {
                throw new Exception(String.format("no indexed files contains '%s'", word));
            }
        }
        return getTop(q);
    }  

    /**
     * Creates a new priority queue based on two search terms. 
     * @param wOne, wTwo are the keywords
     * @return a priority queue containing files that include the search phrase
     */
    private PriorityQueue<FileCount> twoWords(String wOne, String wTwo) {
        synchronized (wordToFileCount.get(wOne)) { // need to synchronize as PriorityQueue is not thread safe
            synchronized (wordToFileCount.get(wTwo)){
                PriorityQueue<FileCount> qOne = wordToFileCount.get(wOne);
                PriorityQueue<FileCount> qTwo = wordToFileCount.get(wTwo);
        
                PriorityQueue<FileCount> bothWords = new PriorityQueue<>(new Comparator<FileCount>(){
                    @Override
                    public int compare(FileCount a1, FileCount a2) {
                        if (a1.getCount() < a2.getCount()) {
                            return 1;
                        } else if (a1.getCount() > a2.getCount()) {
                            return -1;
                        }
                        return 0;
                    }
                });    

                //iterates through each element of qOne and qTwo, finding files that exist in both
                for (FileCount elementOne : qOne){
                    String name1 = elementOne.getFileName();
                    for (FileCount elementTwo : qTwo) {
                        String name2 = elementTwo.getFileName();
                        if (name1.equals(name2)){

                            //scan each line in a file that contains both words
                            try{
                                Scanner scan = new Scanner(new FileInputStream(name2));
                                int internalCount = 0;
                                while(scan.hasNextLine()) {
                                    String line = scan.nextLine().toLowerCase();
                                    if(line.indexOf(wOne + " " + wTwo) != -1) {
                                        internalCount++;
                                    }
                                }

                                //if the file contains the key phrase, add to priority queue
                                if (internalCount > 0){
                                    bothWords.offer(new FileCount(name2, internalCount));
                                }
                            } catch (Exception e) { // in the case that the scanner can't open the file
                                System.out.println(e.getMessage());
                            }
                        }
                    }
                }
                return bothWords;              
            }
        }
}

    private FileCount[] getTop(PriorityQueue<FileCount> q){
        int k = Math.min(q.size(), n);
        FileCount[] top = new FileCount[k];

        //add each value to return array
        for (int i = 0; i < k; i++) {
            top[i] = q.poll();
        }

        //return each element in the priority queue
        for (int j = 0; j < k; j++) {
            q.offer(top[j]);
        }

        return top;
    }
}