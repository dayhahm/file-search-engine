import java.nio.file.*;
import java.io.IOException;

/**
 * Watcher is a thread that serves as a notification service for newly created files, modified files, and deleted files.
 */
public class Watcher extends Thread {

    private WatchService watchService;
    private BoundedBuffer buffer;
    private Path rootDirectory;

    /**
     * Creates a Watcher object that watches the given root directory and adds to the given buffer
     * @param buffer bounded buffer to add to
     * @param rootDirectory root directory to watch
     * @throws Exception on IO error
     */
    public Watcher(BoundedBuffer buffer, String rootDirectory) throws Exception {
        // create the watch service to notify on file creations, modifications, and deletions
        this.rootDirectory = Paths.get(rootDirectory);
        try {
            watchService = FileSystems.getDefault().newWatchService();
            this.rootDirectory.register(watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY);
        } catch (IOException e) {
            throw new Exception("io error while creating watcher");
        }
        this.buffer = buffer;
    }

    /**
     * Watches for any file notices. On creation, it adds the new file to the buffer. On deletion, it deletes any
     * places in the index that the file may show up. On update, it performs a delete then an update.
     */
    public void run() {
        WatchKey key;
        System.out.println("running watcher");
        try {
            while ((key = watchService.take()) != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    try {
                        Path context = (Path) event.context();
                        System.out.println("throwing " + context + "into queue");
                        buffer.enqueue((rootDirectory.resolve(context)).toString());
                    } catch (InterruptedException e) {
                        System.out.println("watcher interrupted");
                    }
                }
                key.reset();
            }
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
}