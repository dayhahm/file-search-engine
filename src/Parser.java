import java.nio.file.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// or some parts of this structure may be in memory
// look into maximum heap size relative to stats

/**
 * Parser is a thread that takes files out of a BoundedBuffer, parses the file, creates the appropriate FileCount
 * objects, and inserts them into the shared index.
 */
public class Parser extends Thread {
    /*
     * Will have multiple threads. consumer thread for the bounded buffer.
     * Takes in a file frome the bounded buffer, creates and updates a HashTable with a word as the key and an FileCount
     * object as a value.
     * number of words in the hashamp
     * size of words in index
     * priority queue size + path length
     *
     * memory profiling tools (google later) measures the kinds of objects that are the msot costly
     */

    private BoundedBuffer buffer;
    private ConcurrentHashMap<String, PriorityQueue<FileCount>> wordToFileCount;
    private ConcurrentHashMap<String, Integer> stats;
    private boolean verbose;

    /**
     * Creates a Parser object that consumes the specified buffer and adds to the specified index. If verbose is true,
     * the stats map will be updated with statistics about the index.
     * @param buffer the BoundedBuffer out of which to take file names
     * @param wordToFileCount the index
     * @param stats map that contains index stats
     * @param verbose true if verbose mode is on
     */
    public Parser(BoundedBuffer buffer, ConcurrentHashMap<String, PriorityQueue<FileCount>> wordToFileCount,
                  ConcurrentHashMap<String, Integer> stats, boolean verbose) {
        super();
        this.buffer = buffer;
        this.wordToFileCount = wordToFileCount;
        this.stats = stats;
        this.verbose = verbose;
    }

    /**
     * Parses the given file and adds the appropriate FileCount objects into the index.
     * @param file
     */
    public void parse(String file) {
        // initialize file word counter
        Map<String, Integer> wordCounter = new HashMap<>();

        try {
            // read the file line by line
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = "";
            while ((line=br.readLine()) != null) {
                // split each line by any non alphanumeric character (not including apostrophes)
                String[] words = line.split("[^a-zA-Z0-9']+");
                // add to word counter. words are changed to lowercase to ensure matching regardless of capitalization
                for (String word: words) {
                    word = word.toLowerCase();
                    wordCounter.put(word, wordCounter.getOrDefault(word, 0) + 1);
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        if (wordCounter.containsKey("holmes")) {
            System.out.println(wordCounter.get("holmes"));
        }
        // go through the word counter and add the corresponding FileCount objects into the proper priority queue
        for (String word : wordCounter.keySet()) {
            if (wordToFileCount.containsKey(word)) {
                PriorityQueue<FileCount> q = wordToFileCount.get(word);
                q.offer(new FileCount(file, wordCounter.get(word)));
            } else { // if the word has not yet been seen, create a new priority queue that sorts by word count and add
                // sort by FileCount count, descending
                PriorityQueue<FileCount> q = new PriorityQueue<>(new Comparator<FileCount>(){
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
                q.offer(new FileCount(file, wordCounter.get(word)));
                wordToFileCount.put(word, q);
                if (verbose) {
                    stats.put("keysLength", stats.get("keysLength") + word.length());
                }
            }
        }
        try {
            // if verbose mode is on, update stats
            if (verbose) {
                stats.put("unreported", stats.get("unreported") + 1);
                stats.put("files", stats.get("files") + 1);
                stats.put("bytes", stats.get("bytes") + (int) Files.size(Paths.get(file)));
                stats.put("paths", stats.get("paths") + file.length());
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Takes a file name out of the buffer and passes it to be parsed. Prints the stats report after about 100 files are
     * read by all Parser threads.
     */
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                String file = buffer.dequeue();
                parse(file);
            } catch (InterruptedException e) {
                System.out.println("parser interrupted");
            }
            if (verbose) {
                synchronized(stats) {
                    if (stats.get("unreported") >= 100) {
                        System.out.println("------------------------");
                        System.out.println("Files:");
                        System.out.println(String.format("\t%d files read", stats.get("files")));
                        System.out.println(String.format("\t%d bytes read", stats.get("bytes")));
                        System.out.println(String.format("\t%d avg file name length/ %d total length",
                                (stats.get("paths") / stats.get("files")),
                                stats.get("paths")));

                        System.out.println("Index:");
                        System.out.println(String.format("\t%d keys", wordToFileCount.size()));
                        System.out.println(String.format("\t%d avg key length/ %d total length",
                                (stats.get("keysLength") / wordToFileCount.size()),
                                stats.get("keysLength")));

                        stats.put("unreported", 0);
                    }
                }
            }
        }
    }
}