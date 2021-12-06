package wsn;

import java.util.ArrayDeque;
import java.util.Queue;

public class TaskQueue {
    private Queue<Runnable> queue;

    public TaskQueue() {
        this.queue = new ArrayDeque<>();
    }

    public void add(Runnable runnable) {
        queue.add(runnable);
    }
    public Runnable remove() {
        return queue.remove();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}
