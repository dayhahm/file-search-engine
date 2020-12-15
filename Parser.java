import java.nio.file.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Parser extends Thread {
    /*
     * Will have multiple threads. consumer thread for the bounded buffer.
     * Takes in a file frome the bounded buffer, creates and updates a HashTable with a word as the key and an ADT
     * object as a value.
     * number of words in the hashamp
     * size of words in index
     * priority queue size + path length
     *
     * memory profiling tools (google later) measures the kinds of objects that are the msot costly
     */

    // this map needs to be shared by all the threads. so maybe this needs to exist in the thread control
    //    private Map<String, ADT> wordMap;   //is HashTable an implementation of Map?
    private BoundedBuffer buffer;
    private ConcurrentHashMap<String, PriorityQueue<ADT>> wordToFileCount;
    private ConcurrentHashMap<String, Integer> stats;
    private boolean verbose;

    public Parser(BoundedBuffer buffer, ConcurrentHashMap<String, PriorityQueue<ADT>> wordToFileCount,
                  ConcurrentHashMap<String, Integer> stats, boolean verbose) {
        super();
        this.buffer = buffer;
        this.wordToFileCount = wordToFileCount;
        this.stats = stats;
        this.verbose = verbose;
    }

    public void parse(String file) {
        Map<String, Integer> wordCounter = new HashMap<>();
        try {
            // change to read incrementally instead of reading all the lines at once
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = "";
            while ((line=br.readLine()) != null) {
                String[] words = line.split("\\s+");
                for (String word: words) {
                    wordCounter.put(word.toLowerCase(), wordCounter.getOrDefault(word, 0) + 1);
                }
            }

            synchronized (stats) {
                stats.put("unreported", stats.get("unreported") + 1);
                stats.put("files", stats.get("files") + 1);
                stats.put("bytes", stats.get("bytes") + (int) Files.size(Paths.get(file)));
                stats.put("paths", stats.get("paths") + file.length());
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        synchronized(wordToFileCount) {
            for (String word : wordCounter.keySet()) {
                if (wordToFileCount.containsKey(word)) {
                    PriorityQueue<ADT> q = wordToFileCount.get(word);
                    q.offer(new ADT(file, wordCounter.get(word)));
                } else {
                    PriorityQueue<ADT> q = new PriorityQueue<>(new Comparator<ADT>(){
                        @Override
                        public int compare(ADT a1, ADT a2) {
                            return a1.getCount() < a2.getCount() ? 1: -1;
                        }
                    });
                    q.offer(new ADT(file, wordCounter.get(word)));
                    wordToFileCount.put(word, q);
                }
            }
        }
        //keep count of how many bytes of files/how many files we have read
    }

    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                String file = buffer.dequeue();
                parse(file);
            } catch (InterruptedException e) {
                System.out.println("parser interrupted");
            }
            if (verbose) {
                synchronized (wordToFileCount) {
                    if (stats.get("unreported") >= 100) {
                        System.out.println("------------------");
                        System.out.println("Files read: " + stats.get("files"));
                        System.out.println("Bytes read: " + stats.get("bytes"));
                        System.out.println("Unique words:" + wordToFileCount.keySet().size());
                        System.out.println("Total size of path names stored: " + stats.get("paths"));
                        stats.put("unreported", 0);
                    }
                }
            }
        }
    }

}