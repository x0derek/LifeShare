package x0derek.lifeShare;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.UUID;

public class SyncManager {

    private final LifeShare plugin;

    public SyncManager(LifeShare plugin) {
        this.plugin = plugin;
    }

    public void syncGroupHealth(UUID sourcePlayerId, double newHealth) {
        LifeShareGroup group = plugin.getGroup(sourcePlayerId);
        if (group == null || !group.isShareHealth()) return;

        for (UUID memberId : group.getMembers()) {
            if (memberId.equals(sourcePlayerId) || plugin.getSyncing().contains(memberId)) continue;

            Player member = Bukkit.getPlayer(memberId);
            if (member == null || !member.isOnline()) continue;

            plugin.getSyncing().add(memberId);
            try {
                double healthToSet = Math.max(0, Math.min(newHealth, member.getMaxHealth()));
                member.setHealth(healthToSet);
            } finally {
                plugin.getSyncing().remove(memberId);
            }
        }
    }

    public void syncGroupFood(UUID sourcePlayerId, int foodLevel) {
        LifeShareGroup group = plugin.getGroup(sourcePlayerId);
        if (group == null || !group.isShareHunger()) return;

        for (UUID memberId : group.getMembers()) {
            if (memberId.equals(sourcePlayerId) || plugin.getSyncing().contains(memberId)) continue;

            Player member = Bukkit.getPlayer(memberId);
            if (member == null || !member.isOnline()) continue;

            plugin.getSyncing().add(memberId);
            try {
                member.setFoodLevel(foodLevel);
            } finally {
                plugin.getSyncing().remove(memberId);
            }
        }
    }

    public void syncGroupInventory(UUID sourcePlayerId) {
        LifeShareGroup group = plugin.getGroup(sourcePlayerId);
        if (group == null || !group.isShareInventory()) return;

        Player source = Bukkit.getPlayer(sourcePlayerId);
        if (source == null || !source.isOnline()) return;

        for (UUID memberId : group.getMembers()) {
            if (memberId.equals(sourcePlayerId) || plugin.getSyncing().contains(memberId)) continue;

            Player member = Bukkit.getPlayer(memberId);
            if (member == null || !member.isOnline()) continue;

            plugin.getSyncing().add(memberId);
            try {
                member.getInventory().setContents(source.getInventory().getContents());
                member.getInventory().setArmorContents(source.getInventory().getArmorContents());
            } finally {
                plugin.getSyncing().remove(memberId);
            }
        }
    }

    public void syncAllGroupData(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null || !plugin.isInGroup(playerId)) return;

        syncGroupHealth(playerId, player.getHealth());
        syncGroupFood(playerId, player.getFoodLevel());
        syncGroupInventory(playerId);
    }
}