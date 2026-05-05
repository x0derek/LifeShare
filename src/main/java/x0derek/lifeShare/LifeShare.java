package x0derek.lifeShare;

import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LifeShare extends JavaPlugin {

    private final Map<UUID, UUID> invitations = new HashMap<>();
    private final Map<UUID, UUID> playerGroups = new HashMap<>();
    private final Map<UUID, LifeShareGroup> groups = new HashMap<>();

    @Override
    public void onEnable() {
        getCommand("lifeshare").setExecutor(new LifeShareCommand(this));
        getCommand("lifeshare").setTabCompleter(new LifeShareCommand(this));
        getLogger().info("LifeShare enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("LifeShare disabled!");
    }

    public Map<UUID, UUID> getInvitations() {
        return invitations;
    }

    public Map<UUID, UUID> getPlayerGroups() {
        return playerGroups;
    }

    public Map<UUID, LifeShareGroup> getGroups() {
        return groups;
    }

    public boolean isInGroup(UUID player) {
        return playerGroups.containsKey(player);
    }

    public LifeShareGroup getGroup(UUID player) {
        UUID owner = playerGroups.get(player);
        if (owner == null) return null;
        return groups.get(owner);
    }
}