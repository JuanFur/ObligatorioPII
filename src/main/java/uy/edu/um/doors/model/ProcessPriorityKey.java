package uy.edu.um.doors.model;

public class ProcessPriorityKey implements Comparable<ProcessPriorityKey> {
    private final int priority;
    private final int pid;

    public ProcessPriorityKey(int priority, int pid) {
        this.priority = priority;
        this.pid = pid;
    }

    public int getPriority() {
        return priority;
    }

    public int getPid() {
        return pid;
    }

    @Override
    public int compareTo(ProcessPriorityKey other) {
        int priorityComparison = Integer.compare(this.priority, other.priority);
        if (priorityComparison != 0) {
            return priorityComparison;
        }
        return Integer.compare(this.pid, other.pid);
    }
}
