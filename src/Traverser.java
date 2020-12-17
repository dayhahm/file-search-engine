import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Traverser is a producer thread that recursively travels through a specified directory's subdirectories, finding all
 * files and adding them to the bounded buffer.
 */
public class Traverser extends Thread {

    private Path rootDir;
    private BoundedBuffer buffer;

    /**
     * Creates a Traverser object that has a starting directory of the specified root and adds to the given buffer.
     * @param buffer the bounded buffer to add to
     * @param rootString the root directory to traverse through
     * @throws Exception On invalid root directory
     */
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

    /**
     * Traverses the root directory recursively. Adds all of the files it finds into the bounded buffer.
     */
    public void run() {
        try {
//            walk through all path starting from the root directory and add to buffer
            Files.walkFileTree(
                    rootDir, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                                throws IOException {
                            try {
                                buffer.enqueue(file.toString());
                            } catch (InterruptedException e) {
                                System.out.println(e);
                            }
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFileFailed(Path file, IOException e)
                                throws IOException {
                            System.out.println("Could not visit " + file + "due to:");
                            System.out.println("e");
                            return FileVisitResult.SKIP_SUBTREE;
                        }
                    }
            );
        } catch (IOException e) {
            System.out.println("Issue accessing the root directory.");
        }
    }
}