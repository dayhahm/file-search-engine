/**
 * BoundedBuffer allows producer and consumer classes to communicate through an array, acting like a pipe. All
 * methods are thread safe and ensure against deadlocks and improper dequeuing and enqueuing.
 */
public class BoundedBuffer {

    private int capacity;
    private int size;
    private int start;
    private int end;
    private String[] buffer;

    /**
     * Creates a BoundedBuffer object with the specified capacity.
     */
    public BoundedBuffer(int capacity) {
        this.capacity = capacity;
        this.size = 0;
        this.end = 0;
        this.start = 0;
        this.buffer = new String[capacity];
    }

    /**
     * Checks if the buffer is empty.
     * @return  true if the buffer is empty.
     */
    private boolean isEmpty() {
        synchronized (this) {
            return this.size == 0;
        }
    }

    /**
     * Checks if the buffer has reached capacity.
     * @return  true if the buffer is full.
     */
    private boolean isFull() {
        synchronized (this) {
            return this.size == this.capacity;
        }
    }

    /**
     * Adds an item to the buffer.
     * @param file file name string
     * @exception InterruptedException on thread interruption
     */
    public void enqueue(String file) throws InterruptedException {
        synchronized (this) {
            // wait for a spot to open up if the buffer is full
            while (isFull())
                this.wait();

            //fill the buffer
            this.buffer[start] = file;
            start = (start + 1) % capacity;
            this.size++;

            // the buffer was previously empty, notify all the threads so that consumers wake up
            if (this.size == 1) {
                this.notifyAll();
            }
        }
    }

    /**
     * Removes item from the end of the buffer.
     * @return the file name
     * @exception InterruptedException on thread interruption
     */
    public String dequeue() throws InterruptedException {
        synchronized (this) {
            // wait for an object to take out of the buffer
            while (isEmpty())
                this.wait();

            // take out and object from the buffer
            String file = this.buffer[end];
            end = (end + 1) % capacity;
            this.size--;

            // the buffer was previously full, notify all the threads so producers can wake up again
            if (this.size == capacity - 2) {
                this.notifyAll();
            }
            return file;
        }
    }
}