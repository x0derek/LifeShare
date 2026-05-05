package x0derek.lifeShare;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class LifeShareListener implements Listener {

    private final LifeShare plugin;

    public LifeShareListener(LifeShare plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (!plugin.isInGroup(playerId)) return;

        LifeShareGroup group = plugin.getGroup(playerId);
        if (group == null) return;

        Player sourcePlayer = null;
        for (UUID memberId : group.getMembers()) {
            if (!memberId.equals(playerId)) {
                Player member = Bukkit.getPlayer(memberId);
                if (member != null && member.isOnline()) {
                    sourcePlayer = member;
                    break;
                }
            }
        }

        if (sourcePlayer == null) return;

        plugin.getSyncing().add(playerId);

        if (group.isShareHealth()) {
            player.setHealth(Math.min(sourcePlayer.getHealth(), player.getMaxHealth()));
        }
        if (group.isShareInventory()) {
            player.getInventory().setContents(sourcePlayer.getInventory().getContents());
            player.getInventory().setArmorContents(sourcePlayer.getInventory().getArmorContents());
        }
        if (group.isShareHunger()) {
            player.setFoodLevel(sourcePlayer.getFoodLevel());
            player.setSaturation(sourcePlayer.getSaturation());
        }

        plugin.getSyncing().remove(playerId);
        player.sendMessage(Component.text("Your data has been synchronized with the group!", NamedTextColor.GREEN));
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!plugin.isInGroup(player.getUniqueId())) return;
        if (plugin.getSyncing().contains(player.getUniqueId())) return;

        double newHealth = player.getHealth() - event.getFinalDamage();
        if (newHealth < 0) newHealth = 0;

        final double healthToSync = newHealth;
        final UUID playerId = player.getUniqueId();

        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getSyncManager().syncGroupHealth(playerId, healthToSync);
            }
        }.runTask(plugin);
    }

    @EventHandler
    public void onRegainHealth(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!plugin.isInGroup(player.getUniqueId())) return;
        if (plugin.getSyncing().contains(player.getUniqueId())) return;

        double newHealth = Math.min(player.getHealth() + event.getAmount(), player.getMaxHealth());

        final double healthToSync = newHealth;
        final UUID playerId = player.getUniqueId();

        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getSyncManager().syncGroupHealth(playerId, healthToSync);
            }
        }.runTask(plugin);
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!plugin.isInGroup(player.getUniqueId())) return;
        if (plugin.getSyncing().contains(player.getUniqueId())) return;

        final int newFoodLevel = event.getFoodLevel();
        final UUID playerId = player.getUniqueId();

        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getSyncManager().syncGroupFood(playerId, newFoodLevel);
            }
        }.runTask(plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getInventory().getType() == InventoryType.CRAFTING) return;
        if (!plugin.isInGroup(player.getUniqueId())) return;
        if (plugin.getSyncing().contains(player.getUniqueId())) return;

        final UUID playerId = player.getUniqueId();

        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getSyncManager().syncGroupInventory(playerId);
            }
        }.runTask(plugin);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!plugin.isInGroup(player.getUniqueId())) return;
        if (plugin.getSyncing().contains(player.getUniqueId())) return;

        final UUID playerId = player.getUniqueId();

        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getSyncManager().syncGroupInventory(playerId);
            }
        }.runTask(plugin);
    }

    @EventHandler
    public void onItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (!plugin.isInGroup(player.getUniqueId())) return;
        if (plugin.getSyncing().contains(player.getUniqueId())) return;

        final UUID playerId = player.getUniqueId();

        new BukkitRunnable() {
            @Override
            public void run() {
                Player p = Bukkit.getPlayer(playerId);
                if (p != null) {
                    plugin.getSyncManager().syncGroupHealth(playerId, p.getHealth());
                    plugin.getSyncManager().syncGroupFood(playerId, p.getFoodLevel());
                    plugin.getSyncManager().syncGroupInventory(playerId);
                }
            }
        }.runTask(plugin);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        plugin.getInvitations().entrySet().removeIf(entry -> entry.getValue().equals(playerId));
        plugin.getInvitations().remove(playerId);
        plugin.getInviteCooldown().remove(playerId);

        if (!plugin.isInGroup(playerId)) return;

        LifeShareGroup group = plugin.getGroup(playerId);
        if (group == null) return;

        if (group.getOwner().equals(playerId)) {
            Player newOwner = null;
            for (UUID memberId : group.getMembers()) {
                if (!memberId.equals(playerId)) {
                    Player member = Bukkit.getPlayer(memberId);
                    if (member != null && member.isOnline()) {
                        newOwner = member;
                        break;
                    }
                }
            }

            if (newOwner != null) {
                UUID newOwnerId = newOwner.getUniqueId();

                group.setOwner(newOwnerId);

                plugin.getGroups().remove(playerId);
                plugin.getGroups().put(newOwnerId, group);

                for (UUID memberId : group.getMembers()) {
                    plugin.getPlayerGroups().put(memberId, newOwnerId);
                }

                plugin.getDataManager().saveData();

                for (UUID memberId : group.getMembers()) {
                    Player member = Bukkit.getPlayer(memberId);
                    if (member != null && member.isOnline()) {
                        member.sendMessage(Component.text("New group owner: ", NamedTextColor.YELLOW)
                                .append(Component.text(newOwner.getName(), NamedTextColor.GREEN)));
                    }
                }
            } else {
                plugin.getGroups().remove(playerId);
                for (UUID memberId : group.getMembers()) {
                    plugin.getPlayerGroups().remove(memberId);
                }
                plugin.getDataManager().saveData();
            }
        }
    }
}