/**
 * FileCount represents a file and the number of times a certain word appears in the file.
 */
public class FileCount {

    private String filename;
    private int count;

    /**
     * Creates a new FileCount with the given file name and count.
     * @param filename the file name
     * @param count the num of times a word appeared in the file.
     */
    public FileCount(String filename, int count) {
        this.filename = filename;
        this.count = count;
    }

    /**
     * Gets the word count
     * @return the word count
     */
    public int getCount() {
        return count;
    }

    /**
     * Creates a string representation of a FileCount object.
     * @return string representation of FileCount object.
     */
    public String toString() {
        return filename + ": " + count;
    }

//    public static void main(String[] args) {
//        // Path path = Paths.get("/Users/debbie/Documents/file-search-engine");
//        // FileCount FileCount = new FileCount(path, 4);
//        // System.out.println(FileCount.toString());
//    }
}