package x0derek.lifeShare;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class LifeShareGroup {

    private final UUID owner;
    private final Set<UUID> members = new HashSet<>();
    private boolean shareHealth = true;
    private boolean shareHunger = true;
    private boolean shareInventory = true;

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

    public boolean isShareHealth() {
        return shareHealth;
    }

    public void setShareHealth(boolean shareHealth) {
        this.shareHealth = shareHealth;
    }

    public boolean isShareHunger() {
        return shareHunger;
    }

    public void setShareHunger(boolean shareHunger) {
        this.shareHunger = shareHunger;
    }

    public boolean isShareInventory() {
        return shareInventory;
    }

    public void setShareInventory(boolean shareInventory) {
        this.shareInventory = shareInventory;
    }
}