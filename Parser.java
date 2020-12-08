import java.nio.file.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Parser extends Thread {
    /*
     * Will have multiple threads. consumer thread for the bounded buffer.
     * Takes in a file frome the bounded buffer, creates and updates a HashTable with a word as the key and an ADT
     * object as a value.
     */

    // this map needs to be shared by all the threads. so maybe this needs to exist in the thread control
//    private Map<String, ADT> wordMap;   //is HashTable an implementation of Map?
    private BoundedBuffer buffer;
    private ConcurrentHashMap<String, PriorityQueue<ADT>> wordToFileCount;

    public Parser(BoundedBuffer buffer, ConcurrentHashMap wordToFileCount) {
        super();
        this.buffer = buffer;
        this.wordToFileCount = wordToFileCount;
    }

    public void parse(Path file) {
        Map<String, Integer> wordCounter = new HashMap<>();
        try {
            List<String> lines = Files.readAllLines(file);
            for (String line: lines) {
                for (String word: line.split("\\s+")) {
                    wordCounter.put(word, wordCounter.getOrDefault(word, 0) + 1);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading " + file);
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
    }

    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Path file = buffer.dequeue();
                parse(file);
            } catch (InterruptedException e) {
                System.out.println("parser interrupted");
            }
        }
    }

}