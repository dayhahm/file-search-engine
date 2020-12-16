import java.util.Scanner;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.nio.file.*;

// memory consumption tests
// store null instead of path in FileCount
// maybe test on tempest for more memory

public class ThreadRunner {

    public static void main(String[] args) {
        BoundedBuffer buffer = new BoundedBuffer(20);
//        ConcurrentHashMap<String, PriorityQueue<FileCount>> wordToFileCount = new ConcurrentHashMap<>();
        Trie wordToFileCount = new Trie();
        ConcurrentHashMap<String, Integer> stats = new ConcurrentHashMap<>();
        stats.put("files", 0);
        stats.put("bytes", 0);
        stats.put("unreported", 0);
        stats.put("paths", 0);

        boolean verbose = false;
        for (String arg: args) {
            if (arg.equals("-v") || arg.equals("--verbose")) {
                verbose = true;
            }
        }

        TrieParser p1 = new TrieParser(buffer, wordToFileCount, stats, verbose);
        TrieParser p2 = new TrieParser(buffer, wordToFileCount, stats, verbose);
        TrieParser p3 = new TrieParser(buffer, wordToFileCount, stats, verbose);
        TrieParser p4 = new TrieParser(buffer, wordToFileCount, stats, verbose);
        TrieParser p5 = new TrieParser(buffer, wordToFileCount, stats, verbose);
        String dir = "/Users/debbie/Documents/Coronavirus-Twitter-Trends";
        try {
            Traverser traverser = new Traverser(buffer, dir);
            Watcher watcher = new Watcher(buffer, dir);
            System.out.println("Starting up all threads");
            traverser.start();
            p1.start();
            p2.start();
            p3.start();
            p4.start();
            p5.start();
            watcher.start();

            traverser.join();

            System.out.println("Traversal has completed — parsing may be ongoing");

            try {
                TrieRanker ranker = new TrieRanker(wordToFileCount);
                Scanner s = new Scanner(System.in);
                System.out.println("Enter a word to search: ");
                String word = s.nextLine();
                word = word.toLowerCase();
                while (!word.equals("quit")) {
                    try {
                        System.out.println(word);
                        FileCount[] filePairs = ranker.getTop(word);
                        for (int i = 0; i < filePairs.length; i++) {
                            System.out.println((i + 1) + " - " + filePairs[i].getFilename() + ":" + filePairs[i].getCount());
                        }
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                    System.out.println("Enter a word to search: ");
                    word = s.nextLine();
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } catch (Exception e) {
            System.out.println(e);
            System.exit(1);
        }
        System.out.println("quitting");
        System.exit(0);
    }
}