package models;

import org.joda.time.LocalTime;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

public class CycleQueue {

    private LinkedList<LocalTime> queue;

    public CycleQueue() {
        queue = new LinkedList<>();
    }

    public LocalTime peek() {
        return queue.getFirst();
    }

    public void add(LocalTime t) {
        queue.add(t);
    }

    public void adjust() {

        Comparator<LocalTime> comparator = (i1, i2) -> {
            if (i1.isBefore(i2)) return -1;
            if (i1.isAfter(i2)) return 1;
            return 0;
        };

        LocalTime now = new LocalTime();

        LocalTime biggest = Collections.max(queue, comparator);
        LocalTime smallest = Collections.min(queue, comparator);

        if (now.isBefore(smallest) || now.isAfter(biggest)) {
            while (!peek().equals(smallest)) {
                next();
            }
            return;
        }

        if (peek().isAfter(now)) {
            while (!peek().isBefore(now)) {
                prev();
            }
            next();
        } else if (peek().isBefore(now)) {
            while (!peek().isAfter(now)) {
                next();
            }
        } else {
            adjust();
        }

    }

    private void prev() {
        LocalTime time = queue.removeLast();
        queue.addFirst(time);
    }

    public void next() {
        LocalTime t = queue.poll();
        queue.add(t);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        queue.forEach((i) -> builder.append("[ ").append(i).append(" ]"));
        return builder.toString();
    }

}
