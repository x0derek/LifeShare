package x0derek.lifeShare;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.Set;
import java.util.UUID;

public class SyncManager {

    private final LifeShare plugin;

    public SyncManager(LifeShare plugin) {
        this.plugin = plugin;
    }

    public void syncGroupHealth(UUID sourcePlayerId, double newHealth, boolean isDamage) {
        LifeShareGroup group = plugin.getGroup(sourcePlayerId);
        if (group == null) return;

        Set<UUID> members = group.getMembers();

        for (UUID memberId : members) {
            if (memberId.equals(sourcePlayerId)) continue;
            if (plugin.getSyncing().contains(memberId)) continue;

            Player member = Bukkit.getPlayer(memberId);
            if (member == null) continue;

            plugin.getSyncing().add(memberId);

            if (newHealth <= 0) {
                member.setHealth(0);
            } else {
                if (newHealth > member.getMaxHealth()) {
                    member.setHealth(member.getMaxHealth());
                } else {
                    member.setHealth(newHealth);
                }
            }

            plugin.getSyncing().remove(memberId);
        }
    }

    public void syncGroupFood(UUID sourcePlayerId, int foodLevel) {
        LifeShareGroup group = plugin.getGroup(sourcePlayerId);
        if (group == null) return;

        for (UUID memberId : group.getMembers()) {
            if (memberId.equals(sourcePlayerId)) continue;
            if (plugin.getSyncing().contains(memberId)) continue;

            Player member = Bukkit.getPlayer(memberId);
            if (member == null) continue;

            plugin.getSyncing().add(memberId);
            member.setFoodLevel(foodLevel);
            member.setSaturation(20);
            plugin.getSyncing().remove(memberId);
        }
    }

    public void syncGroupInventory(UUID sourcePlayerId) {
        LifeShareGroup group = plugin.getGroup(sourcePlayerId);
        if (group == null) return;

        Player source = Bukkit.getPlayer(sourcePlayerId);
        if (source == null) return;

        for (UUID memberId : group.getMembers()) {
            if (memberId.equals(sourcePlayerId)) continue;
            if (plugin.getSyncing().contains(memberId)) continue;

            Player member = Bukkit.getPlayer(memberId);
            if (member == null) continue;

            plugin.getSyncing().add(memberId);
            member.getInventory().setContents(source.getInventory().getContents());
            member.getInventory().setArmorContents(source.getInventory().getArmorContents());
            member.getInventory().setExtraContents(source.getInventory().getExtraContents());
            plugin.getSyncing().remove(memberId);
        }
    }

    public void syncAllGroupData(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null) return;
        if (!plugin.isInGroup(playerId)) return;

        syncGroupHealth(playerId, player.getHealth(), false);
        syncGroupFood(playerId, player.getFoodLevel());
        syncGroupInventory(playerId);
    }
}