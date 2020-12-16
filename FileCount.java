import java.nio.file.Path;
import java.nio.file.Paths;

public class FileCount {

    private String filename;
    private int count;

    public FileCount(String filename, int count) {
        this.filename = null;
        this.count = count;
    }

    public String getFilename() {
        return filename;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int newCount) {
        count = newCount;
    }

    public String toString() {
        return filename + ": " + count;
    }

    public static void main(String[] args) {
        // Path path = Paths.get("/Users/debbie/Documents/file-search-engine");
        // FileCount FileCount = new FileCount(path, 4);
        // System.out.println(FileCount.toString());
    }
}