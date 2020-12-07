public class ThreadRunner {

    public static void main(String[] args) {
        BoundedBuffer buffer = new BoundedBuffer(20);
        Parser parser = new Parser(buffer);
        String dir = "/Users/debbie";
        try {
            Traverser traverser = new Traverser(buffer, dir);
            parser.start();
            traverser.start();
        } catch (Exception e) {
            System.out.println(e);
            System.exit(1);
        }
    }
}