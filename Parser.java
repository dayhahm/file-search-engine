import java.nio.file.*;

public class Parser extends Thread {
    /*
     * Will have multiple threads. consumer thread for the bounded buffer.
     * Takes in a file frome the bounded buffer, creates and updates a HashTable with a word as the key and an ADT
     * object as a value.
     */

    // this map needs to be shared by all the threads. so maybe this needs to exist in the thread control
//    private Map<String, ADT> wordMap;   //is HashTable an implementation of Map?
    private BoundedBuffer buffer;

    public Parser(BoundedBuffer buffer) {
        super();
        this.buffer = buffer;
    }

    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Path file = buffer.dequeue();
                System.out.println(file);
            } catch (InterruptedException e) {
                System.out.println("parser interrupted");
            }
        }
    }

}