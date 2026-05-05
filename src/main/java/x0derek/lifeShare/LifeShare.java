package x0derek.lifeShare;

import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class LifeShare extends JavaPlugin {

    private final Map<UUID, UUID> invitations = new HashMap<>();
    private final Map<UUID, UUID> playerGroups = new HashMap<>();
    private final Map<UUID, LifeShareGroup> groups = new HashMap<>();
    private final Set<UUID> syncing = new HashSet<>();
    private final Map<UUID, Long> inviteCooldown = new HashMap<>();
    private DataManager dataManager;
    private SyncManager syncManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        getConfig().set("owner-can-change-options", getConfig().getBoolean("owner-can-change-options", true));
        saveConfig();

        File dataFile = new File(getDataFolder(), "data.yml");
        if (dataFile.exists()) {
            try {
                String content = new String(java.nio.file.Files.readAllBytes(dataFile.toPath()));
                if (content.contains("default")) {
                    File backup = new File(getDataFolder(), "data.yml.corrupted_" + System.currentTimeMillis());
                    dataFile.renameTo(backup);
                    getLogger().warning("Uszkodzony data.yml został przeniesiony do: " + backup.getName());
                    dataFile.createNewFile();
                }
            } catch (IOException e) {
                getLogger().warning("Nie można sprawdzić data.yml: " + e.getMessage());
            }
        }

        getCommand("lifeshare").setExecutor(new LifeShareCommand(this));
        getCommand("lifeshare").setTabCompleter(new LifeShareCommand(this));
        getServer().getPluginManager().registerEvents(new LifeShareListener(this), this);
        dataManager = new DataManager(this);
        syncManager = new SyncManager(this);
        getLogger().info("\u001B[32mLifeShare by x0derek\u001B[0m");
        getLogger().info("LifeShare enabled!");
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.saveData();
        }
        getLogger().info("\u001B[32mLifeShare by x0derek\u001B[0m");
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

    public Set<UUID> getSyncing() {
        return syncing;
    }

    public Map<UUID, Long> getInviteCooldown() {
        return inviteCooldown;
    }

    public boolean isInGroup(UUID player) {
        return playerGroups.containsKey(player);
    }

    public LifeShareGroup getGroup(UUID player) {
        UUID owner = playerGroups.get(player);
        if (owner == null) return null;
        return groups.get(owner);
    }

    public boolean hasInviteCooldown(UUID player) {
        Long lastInvite = inviteCooldown.get(player);
        if (lastInvite == null) return false;
        return System.currentTimeMillis() - lastInvite < 5000;
    }

    public void setInviteCooldown(UUID player) {
        inviteCooldown.put(player, System.currentTimeMillis());
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public SyncManager getSyncManager() {
        return syncManager;
    }
}