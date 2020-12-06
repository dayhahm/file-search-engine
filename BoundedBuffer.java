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
        this.buffer = new Path[capacity];
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
            while (isFull()) {
                try {
                    this.wait();
                } catch (InterruptedException e) { // replace with more appropriate catch later on
                    System.out.println("Thread was interrupted while waiting.");
                }
            }


            //fill the buffer
            this.buffer[start++] = file;
            this.size++;

            // the buffer was previously empty, notify all the threads
            if (this.size == 1) {
                this.notifyAll();
            }
        }
    }

    public Path dequeue() {
        synchronized (this) {
            // wait for an object to take out of the buffer
            while (isEmpty()) {
                try {
                    this.wait();
                } catch (InterruptedException e) { // replace with more appropriate catch later on
                    System.out.println("Thread was interrupted while waiting.");
                }
            }

            // take out and object from the buffer
            Path file = this.buffer[end--];
            this.size--;

            // the buffer was previously full, notify all the threads. (is this necessary if we only have one producer thread)
            if (this.size == capacity - 2) {
                this.notifyAll();
            }
            return file;
        }
    }
}