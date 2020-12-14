import java.util.Scanner;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.nio.file.*;

public class ThreadRunner {

    public static void main(String[] args) {
        BoundedBuffer buffer = new BoundedBuffer(20);
        ConcurrentHashMap<String, PriorityQueue<ADT>> wordToFileCount = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, Integer> stats = new ConcurrentHashMap<>();
        stats.put("files", 0);
        stats.put("bytes", 0);
        stats.put("unreported", 0);
        Parser p1 = new Parser(buffer, wordToFileCount, stats);
        Parser p2 = new Parser(buffer, wordToFileCount, stats);
        Parser p3 = new Parser(buffer, wordToFileCount, stats);
        Parser p4 = new Parser(buffer, wordToFileCount, stats);
        Parser p5 = new Parser(buffer, wordToFileCount, stats);
        String dir = "/Users/debbie/Documents";
        try {
            Traverser traverser = new Traverser(buffer, dir);
            System.out.println("Starting up all threads");
            traverser.start();
            p1.start();
            p2.start();
            p3.start();
            p4.start();
            p5.start();

            traverser.join();

            System.out.println("Traversal has completed — parsing may be ongoing");

            try {
                Ranker ranker = new Ranker(wordToFileCount);
                Scanner s = new Scanner(System.in);
                System.out.println("Enter a word to search: ");
                String word = s.nextLine();
                while (!word.equals("quit")) {
                    ADT[] filePairs = ranker.getTop(word);
                    for (int i = 0; i < filePairs.length; i++) {
                        System.out.println((i + 1) + " - " + filePairs[i].getFilename() + ":" + filePairs[i].getCount());
                    }
                    System.out.println("Enter a word to search: ");
                    word = s.nextLine();
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } catch (
                Exception e) {
            System.out.println(e);
            System.exit(1);
        }
        System.out.println("quitting");
        System.exit(0);
    }
}