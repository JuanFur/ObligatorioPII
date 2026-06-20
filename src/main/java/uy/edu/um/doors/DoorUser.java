package uy.edu.um.doors;

public class DoorUser {
    private final int uid;
    private final String alias;
    private final UserType type;

    public DoorUser(int uid, String alias, UserType type) {
        this.uid = uid;
        this.alias = alias;
        this.type = type;
    }

    public int getUid() {
        return uid;
    }

    public String getAlias() {
        return alias;
    }

    public UserType getType() {
        return type;
    }

    public int getPriorityWeight() {
        return type.getPriorityWeight();
    }
}
