import java.util.Locale;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Driver class.
 */
public class ThreadRunner {

    /**
     * Driver method for the search engine. Creates all the necessary threads, takes user input for directory and search
     * terms, and prints them out. Exits with code 0 on successful run, and 1 on error.
     */
    public static void main(String[] args) {
        //initialize
        BoundedBuffer buffer = new BoundedBuffer(20);
        ConcurrentHashMap<String, PriorityQueue<FileCount>> wordToFileCount = new ConcurrentHashMap<>();

        //verbose mode statistics
        boolean verbose = false;
        ConcurrentHashMap<String, Integer> stats = new ConcurrentHashMap<>();
        stats.put("files", 0);
        stats.put("bytes", 0);
        stats.put("unreported", 0);
        stats.put("paths", 0);
        stats.put("keysLength", 0);

        // check for verbose option
        for (String arg: args) {
            if (arg.equals("-v") || arg.equals("--verbose")) {
                verbose = true;
            }
        }

        // take in user input for root directory
        Scanner s = new Scanner(System.in);
        System.out.print("Enter the directory you'd like to index: ");
        String dir = s.nextLine().trim();

        // create parser threads
        Parser p1 = new Parser(buffer, wordToFileCount, stats, verbose);
        Parser p2 = new Parser(buffer, wordToFileCount, stats, verbose);
        Parser p3 = new Parser(buffer, wordToFileCount, stats, verbose);
        Parser p4 = new Parser(buffer, wordToFileCount, stats, verbose);
        Parser p5 = new Parser(buffer, wordToFileCount, stats, verbose);
        try {
            // create the traverser and watcher. if user gave invalid root, traverser will throw error and program quits
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

            //wait for traverser to finish initial indexing before allowing users to search
            traverser.join();

            System.out.println("Traversal has completed — parsing may be ongoing");
            try {
                // create the ranker
                Ranker ranker = new Ranker(wordToFileCount);

                // take user input for search term
                System.out.println("Enter a word to search (enter !quit to exit): ");
                String word = s.nextLine();
                word = word.toLowerCase().trim(); // want to match words regardless of capitalization

                while (!word.equals("!quit")) {
                    try {
                        // get and print the top file matches
                        FileCount[] filePairs = ranker.getTop(word);
                        for (int i = 0; i < filePairs.length; i++) {
                            System.out.println((i + 1) + " - " + filePairs[i].toString());
                        }
                    } catch (Exception e) { // in the case that there are no files that contain the keyword
                        System.out.println(e.getMessage());
                    }
                    System.out.println("Enter a word to search (enter !quit to exit): ");
                    word = s.nextLine();
                    word = word.toLowerCase().trim();
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } catch (Exception e) {
            System.out.println(e);
            System.exit(1);
        }
        System.out.println("Exiting file search engine");
        System.exit(0);
    }
}