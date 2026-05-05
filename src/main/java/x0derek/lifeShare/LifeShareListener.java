package x0derek.lifeShare;

import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import java.util.UUID;

public class LifeShareListener implements org.bukkit.event.Listener {

    private final LifeShare plugin;

    public LifeShareListener(LifeShare plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (plugin.isInGroup(playerId)) {
            LifeShareGroup group = plugin.getGroup(playerId);
            UUID ownerId = group.getOwner();
            Player owner = Bukkit.getPlayer(ownerId);

            if (owner != null && owner.isOnline()) {
                if (!owner.getUniqueId().equals(playerId)) {
                    plugin.getSyncing().add(playerId);

                    if (group.isShareHealth()) {
                        player.setHealth(owner.getHealth());
                    }
                    if (group.isShareInventory()) {
                        player.getInventory().setContents(owner.getInventory().getContents());
                        player.getInventory().setArmorContents(owner.getInventory().getArmorContents());
                    }
                    if (group.isShareHunger()) {
                        player.setFoodLevel(owner.getFoodLevel());
                        player.setSaturation(20);
                    }

                    plugin.getSyncing().remove(playerId);
                    player.sendMessage(Component.text("Your data has been synchronized with the group!", NamedTextColor.GREEN));
                } else {
                    Player anyMember = null;
                    for (UUID memberId : group.getMembers()) {
                        if (!memberId.equals(playerId)) {
                            Player member = Bukkit.getPlayer(memberId);
                            if (member != null && member.isOnline()) {
                                anyMember = member;
                                break;
                            }
                        }
                    }

                    if (anyMember != null) {
                        plugin.getSyncing().add(playerId);

                        if (group.isShareHealth()) {
                            player.setHealth(anyMember.getHealth());
                        }
                        if (group.isShareInventory()) {
                            player.getInventory().setContents(anyMember.getInventory().getContents());
                            player.getInventory().setArmorContents(anyMember.getInventory().getArmorContents());
                        }
                        if (group.isShareHunger()) {
                            player.setFoodLevel(anyMember.getFoodLevel());
                            player.setSaturation(20);
                        }

                        plugin.getSyncing().remove(playerId);
                        player.sendMessage(Component.text("Your data has been synchronized with the group!", NamedTextColor.GREEN));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!plugin.isInGroup(player.getUniqueId())) return;
        if (plugin.getSyncing().contains(player.getUniqueId())) return;

        double newHealth = player.getHealth() - event.getFinalDamage();
        if (newHealth < 0) newHealth = 0;

        plugin.getSyncManager().syncGroupHealth(player.getUniqueId(), newHealth);
    }

    @EventHandler
    public void onRegainHealth(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!plugin.isInGroup(player.getUniqueId())) return;
        if (plugin.getSyncing().contains(player.getUniqueId())) return;

        double newHealth = player.getHealth() + event.getAmount();
        if (newHealth > player.getMaxHealth()) newHealth = player.getMaxHealth();

        plugin.getSyncManager().syncGroupHealth(player.getUniqueId(), newHealth);
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!plugin.isInGroup(player.getUniqueId())) return;
        if (plugin.getSyncing().contains(player.getUniqueId())) return;

        int newFoodLevel = event.getFoodLevel();
        plugin.getSyncManager().syncGroupFood(player.getUniqueId(), newFoodLevel);
    }

    @EventHandler
    public void onInventoryChange(PlayerInventorySlotChangeEvent event) {
        Player player = event.getPlayer();
        if (!plugin.isInGroup(player.getUniqueId())) return;
        if (plugin.getSyncing().contains(player.getUniqueId())) return;

        plugin.getSyncManager().syncGroupInventory(player.getUniqueId());
    }

    @EventHandler
    public void onItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (!plugin.isInGroup(player.getUniqueId())) return;
        if (plugin.getSyncing().contains(player.getUniqueId())) return;

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            plugin.getSyncManager().syncGroupHealth(player.getUniqueId(), player.getHealth());
            plugin.getSyncManager().syncGroupFood(player.getUniqueId(), player.getFoodLevel());
            plugin.getSyncManager().syncGroupInventory(player.getUniqueId());
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        plugin.getInvitations().entrySet().removeIf(entry -> entry.getValue().equals(playerId));
        plugin.getInvitations().remove(playerId);
        plugin.getInviteCooldown().remove(playerId);

        if (plugin.isInGroup(playerId)) {
            LifeShareGroup group = plugin.getGroup(playerId);

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

                    plugin.getGroups().remove(playerId);
                    plugin.getGroups().put(newOwnerId, group);

                    for (UUID memberId : group.getMembers()) {
                        plugin.getPlayerGroups().put(memberId, newOwnerId);
                    }

                    plugin.getDataManager().saveData();

                    for (UUID memberId : group.getMembers()) {
                        Player member = Bukkit.getPlayer(memberId);
                        if (member != null && member.isOnline() && !member.getUniqueId().equals(playerId)) {
                            member.sendMessage(Component.text("New group owner: ", NamedTextColor.YELLOW)
                                    .append(Component.text(newOwner.getName(), NamedTextColor.GREEN)));
                        }
                    }
                }
            }
        }
    }
}