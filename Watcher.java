import java.nio.file.*;
import java.io.IOException;

public class Watcher extends Thread {

    private WatchService watchService;
    private BoundedBuffer buffer;
    private Path rootDirectory;

    public Watcher(BoundedBuffer buffer, String rootDirectory) throws Exception {
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