package x0derek.lifeShare;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class DataManager {
    private final LifeShare plugin;
    private File dataFile;
    private FileConfiguration dataConfig;

    public DataManager(LifeShare plugin) {
        this.plugin = plugin;
        loadData();
    }

    public void loadData() {
        dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Cannot create data.yml: " + e.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        if (dataConfig.contains("groups")) {
            for (String ownerStr : dataConfig.getConfigurationSection("groups").getKeys(false)) {
                UUID owner = UUID.fromString(ownerStr);
                LifeShareGroup group = new LifeShareGroup(owner);

                List<String> members = dataConfig.getStringList("groups." + ownerStr + ".members");
                for (String memberStr : members) {
                    UUID member = UUID.fromString(memberStr);
                    group.addMember(member);
                    plugin.getPlayerGroups().put(member, owner);
                }
                plugin.getGroups().put(owner, group);
            }
        }
    }

    public void saveData() {
        dataConfig.set("groups", null);

        for (Map.Entry<UUID, LifeShareGroup> entry : plugin.getGroups().entrySet()) {
            String owner = entry.getKey().toString();
            List<String> members = new ArrayList<>();
            for (UUID member : entry.getValue().getMembers()) {
                members.add(member.toString());
            }
            dataConfig.set("groups." + owner + ".members", members);
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Cannot save data.yml: " + e.getMessage());
        }
    }
}