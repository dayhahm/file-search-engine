import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ranker is a thread that finds the most relevant/highly ranked FileCount objects for any word in the index.
 */
public class Ranker {
    /*
     * Takes in one keyword and returns an ordered/sorted list of files.
     * Have a default limit. Have two constructors: one that takes in a keyword and limit, and one
     * that takes in just a keyword. If using constructor with no limit, uses a default limit of about 10.
     * Priority queue.
     * https://www.geeksforgeeks.org/implement-priorityqueue-comparator-java/
     *
     * add summary of the synchronization discipline.
     */

    private ConcurrentHashMap<String, PriorityQueue<FileCount>> wordToFileCount;
    private int n;

    /**
     * Creates a Ranker object that looks at the given index and sets the maximum number of FileCounts to be returned
     * to 10.
     * @param wordToFileCount the index
     */
    public Ranker(ConcurrentHashMap<String, PriorityQueue<FileCount>> wordToFileCount) {
        this.wordToFileCount = wordToFileCount;
        this.n = 10;
    }

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
    public FileCount[] getTop(String word) throws Exception {

        if (wordToFileCount.containsKey(word)) {
            synchronized (wordToFileCount.get(word)) { // need to synchronize as PriorityQueue is not thread safe
                PriorityQueue<FileCount> q = wordToFileCount.get(word);
                int k = Math.min(q.size(), n);
                FileCount[] top = new FileCount[k];

                for (int i = 0; i < k; i++) {
                    top[i] = q.poll();
                }

                for (int j = 0; j < k; j++) {
                    q.offer(top[j]);
                }
                return top;
            }
        } else {
            throw new Exception("no indexed files contain this word");
        }

    }
}