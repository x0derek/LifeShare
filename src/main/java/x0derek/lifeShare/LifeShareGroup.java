package x0derek.lifeShare;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class LifeShareGroup {

    private final UUID owner;
    private final Set<UUID> members = new HashSet<>();

    public LifeShareGroup(UUID owner) {
        this.owner = owner;
        this.members.add(owner);
    }

    public UUID getOwner() {
        return owner;
    }

    public Set<UUID> getMembers() {
        return members;
    }

    public void addMember(UUID uuid) {
        members.add(uuid);
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
    }
}