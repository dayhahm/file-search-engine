import java.nio.file.Path;

public class BoundedBuffer {

    private int capacity;
    private int size;
    private int start;
    private int end;
    private Path[] buffer;

    public BoundedBuffer(int capacity) {
        this.capacity = capacity;
        this.size = 0;
        this.end = -1;
        this.start = 0;
        this.buffer = new String[capacity];
    }

    public boolean isEmpty() {
        synchronized (this) {
            return this.size == 0;
        }
    }

    public boolean isFull() {
        synchronized (this) {
            return this.size == this.capacity;
        }
    }

    public void enqueue(Path file) {
        synchronized (this) {
            // wait for a spot to open up if the buffer is full
            while (isFull())
                this.wait();

            //fill the buffer
            this.buffer[start++] = file;
            this.count++;

            // the buffer was previously empty, notify all the threads
            if (this.count == 1) {
                this.notifyAll()
            }
        }
    }

    public Path dequeue() {
        synchronized (this) {
            // wait for an object to take out of the buffer
            while (isEmpty())
                this.wait();

            // take out and object from the buffer
            Path file = this.buffer[end--];
            this.count--;

            // the buffer was previously full, notify all the threads. (is this necessary if we only have one producer thread)
            if (count == capacity - 2) {
                this.notifyAll;
            }
            return file;
        }
    }
}