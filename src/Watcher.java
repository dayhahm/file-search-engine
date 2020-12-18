import java.nio.file.*;
import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.StandardWatchEventKinds;

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
        // create the watch service to notify on file creations, modifications, and deletions for root directory and sub directories
        this.rootDirectory = Paths.get(rootDirectory);
        try {
            watchService = FileSystems.getDefault().newWatchService();
            Files.walkFileTree(this.rootDirectory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    dir.register(watchService,
                            StandardWatchEventKinds.ENTRY_CREATE,
                            StandardWatchEventKinds.ENTRY_DELETE,
                            StandardWatchEventKinds.ENTRY_MODIFY);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
                throw new Exception("io error while creating watcher");
        } catch (Exception e) {
            System.out.println("hello");
        }
        this.buffer = buffer;
    }

    /**
     * Watches for any file notices. On creation, it adds the new file to the buffer. On deletion, it deletes any
     * places in the index that the file may show up. On update, it performs a delete then an update.
     */
    public void run() {
        WatchKey key;
        try {
            while ((key = watchService.take()) != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    try {
                        // the context method returns a relative path, so need to get the parent path from the key
                        Path parent = (Path) (key.watchable());
                        Path child = parent.resolve((Path) event.context());
                        //check that the child object is a file
                        if (!Files.isDirectory(child)) {
                            buffer.enqueue(child.toString());
                        }
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