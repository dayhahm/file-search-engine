import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.io.IOException;
import java.util.stream.Collectors;
import java.util.List;
import java.util.ArrayList;

public class Traverser extends Thread {
    /*
     * Only one thread for the traverser. Producer thread for the bounded buffer.
     * Traverses the file system from the root directory. Use BFS or DFS and the FileVisitor interface
     * https://docs.oracle.com/javase/tutorial/essential/io/walk.html
     * Throw all files found into a bounded buffer.
     */
    Path rootDir;
    BoundedBuffer buffer;

    public Traverser(BoundedBuffer buffer, String rootString) throws Exception {
        super();
        this.buffer = buffer;
        try {
            this.rootDir = Paths.get(rootString);
        } catch (InvalidPathException e) {
            throw new Exception("Given root directory (" + rootString + ") is invalid.");
        }
        if (!Files.isDirectory(this.rootDir)) {
            throw new Exception("The given root (" + rootString + ") is not a directory.");
        }
    }

    public void run() {
        try {
//            walk through all path starting from the root directory and filter so only files are remaining
            List<String> files = new ArrayList<>();
            Files.walkFileTree(
                    rootDir, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                                throws IOException {
                            files.add(file.toString());
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFileFailed(Path file, IOException e)
                                throws IOException {
                            System.out.println("Could not visit " + file);
                            return FileVisitResult.SKIP_SUBTREE;
                        }
                    }
            );
//            List<Path> files = Files.walk(rootDir)
//                                    .filter(p -> Files.isRegularFile(p))
//                                    .collect(Collectors.toList());
            for (String file: files) {
                try {

                    buffer.enqueue(file);
                } catch (InterruptedException e) {
                    System.out.println("traverser interrupted");
                }
            }
        } catch (IOException e) {
            System.out.println("Issue accessing the root directory.");
        }
    }
}