import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.Math;
import java.nio.file.*;

public class Ranker {
    /*
     * Takes in one keyword and returns an ordered/sorted list of files.
     * Have a default limit. Have two constructors: one that takes in a keyword and limit, and one
     * that takes in just a keyword. If using constructor with no limit, uses a default limit of about 10.
     * Priority queue.
     * https://www.geeksforgeeks.org/implement-priorityqueue-comparator-java/
     */

    private ConcurrentHashMap<String, PriorityQueue<ADT>> wordToFileCount;
    private int n;

    public Ranker(ConcurrentHashMap<String, PriorityQueue<ADT>> wordToFileCount) {
        this.wordToFileCount = wordToFileCount;
        this.n = 10;
    }

    public Ranker(ConcurrentHashMap<String, PriorityQueue<ADT>> wordToFileCount, int n) {
        this.wordToFileCount = wordToFileCount;
        this.n = n;
    }

    public ADT[] getTop(String word) throws Exception {
        synchronized (wordToFileCount) {
            if (wordToFileCount.containsKey(word)) {
                PriorityQueue<ADT> q = wordToFileCount.get(word);
                int k = Math.min(q.size(), n);
                ADT[] top = new ADT[k];

                for (int i = 0; i < k; i++) {
                    top[i] = q.poll();
                }

                for (int j = 0; j < k; j++) {
                    q.offer(top[j]);
                }
                return top;
            } else {
                throw new Exception("no indexed files contain this word");
            }
        }
    }
}