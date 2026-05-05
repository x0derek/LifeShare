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
                plugin.getLogger().warning("Nie można utworzyć data.yml: " + e.getMessage());
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

                group.setShareHealth(dataConfig.getBoolean("groups." + ownerStr + ".shareHealth", true));
                group.setShareHunger(dataConfig.getBoolean("groups." + ownerStr + ".shareHunger", true));
                group.setShareInventory(dataConfig.getBoolean("groups." + ownerStr + ".shareInventory", true));

                plugin.getGroups().put(owner, group);
            }
        }
    }

    public void saveData() {
        dataConfig.set("groups", null);

        for (Map.Entry<UUID, LifeShareGroup> entry : plugin.getGroups().entrySet()) {
            String owner = entry.getKey().toString();
            LifeShareGroup group = entry.getValue();

            List<String> members = new ArrayList<>();
            for (UUID member : group.getMembers()) {
                members.add(member.toString());
            }

            dataConfig.set("groups." + owner + ".members", members);
            dataConfig.set("groups." + owner + ".shareHealth", group.isShareHealth());
            dataConfig.set("groups." + owner + ".shareHunger", group.isShareHunger());
            dataConfig.set("groups." + owner + ".shareInventory", group.isShareInventory());
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Nie można zapisać data.yml: " + e.getMessage());
        }
    }
}