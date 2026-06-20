package uy.edu.um.doors;

public enum UserType {
    ADMIN(32),
    GENERIC(16);

    private final int priorityWeight;

    UserType(int priorityWeight) {
        this.priorityWeight = priorityWeight;
    }

    public int getPriorityWeight() {
        return priorityWeight;
    }
}
