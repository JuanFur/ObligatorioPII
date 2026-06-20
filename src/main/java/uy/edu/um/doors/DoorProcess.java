package uy.edu.um.doors;

import uy.edu.um.tad.list.MyList;

public class DoorProcess implements Comparable<DoorProcess> {
    private final int pid;
    private final String name;
    private final DoorUser user;
    private final MyList<ProcessEvent> events;
    private int priority;
    private ProcessState state;
    private FinishState finishState;
    private DoorUser terminatedBy;

    public DoorProcess(int pid, String name, DoorUser user, MyList<ProcessEvent> events) {
        this.pid = pid;
        this.name = name;
        this.user = user;
        this.events = events;
        this.priority = 0;
        this.state = ProcessState.NEW;
    }

    public int getPid() {
        return pid;
    }

    public String getName() {
        return name;
    }

    public DoorUser getUser() {
        return user;
    }

    public MyList<ProcessEvent> getEvents() {
        return events;
    }

    public int getPriority() {
        return priority;
    }

    public ProcessState getState() {
        return state;
    }

    public void setState(ProcessState state) {
        this.state = state;
    }

    public FinishState getFinishState() {
        return finishState;
    }

    public DoorUser getTerminatedBy() {
        return terminatedBy;
    }

    public void finish(FinishState finishState, DoorUser terminatedBy) {
        this.finishState = finishState;
        this.terminatedBy = terminatedBy;
        this.state = ProcessState.FINISHED;
    }

    public void calculatePriority() {
        int totalEvents = events == null ? 0 : events.size();
        if (totalEvents == 0) {
            priority = 0;
            return;
        }

        int cpuEvents = 0;
        int ramEvents = 0;
        int diskEvents = 0;
        for (int i = 0; i < totalEvents; i++) {
            ProcessEvent event = events.get(i);
            if (event.getType() == EventType.CPU) {
                cpuEvents++;
            } else if (event.getType() == EventType.RAM) {
                ramEvents++;
            } else if (event.getType() == EventType.DISK) {
                diskEvents++;
            }
        }

        int eventWeight = ((8 * cpuEvents) + (2 * ramEvents) + (2 * diskEvents)) / totalEvents;
        priority = eventWeight + (user.getPriorityWeight() * totalEvents);
    }

    @Override
    public int compareTo(DoorProcess other) {
        return Integer.compare(this.priority, other.priority);
    }

}