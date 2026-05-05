package x0derek.lifeShare;

import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;


public class LifeShareListener implements org.bukkit.event.Listener {

    private final LifeShare plugin;

    public LifeShareListener(LifeShare plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (!plugin.isInGroup(player.getUniqueId())) {
            return;
        }
        var getDamage = event.getDamage();
        LifeShareGroup group = plugin.getGroup(player.getUniqueId());
        group.getMembers().forEach(member -> {
            Player playerToDamage = Bukkit.getPlayer(member);
            if (player.getUniqueId().equals(member)) {
                return;
            }
            if (plugin.getSyncing().contains(player.getUniqueId())) {
                return;
            }
            if (playerToDamage == null) {
                return;
            }
            plugin.getSyncing().add(playerToDamage.getUniqueId());
            playerToDamage.damage(event.getDamage());
            plugin.getSyncing().remove(playerToDamage.getUniqueId());
        });
    }

    @EventHandler
    public void onInventoryClick(PlayerInventorySlotChangeEvent event) {
        Player player = event.getPlayer();
        if (!plugin.isInGroup(player.getUniqueId())) {
            return;
        }
        LifeShareGroup group = plugin.getGroup(player.getUniqueId());
        group.getMembers().forEach(member -> {
            Player playertoInv = Bukkit.getPlayer(member);
            if(player.getUniqueId().equals(member)) {
                return;
            }
            if (plugin.getSyncing().contains(player.getUniqueId())) {
                return;
            }
            if (playertoInv == null) {
                return;
            }
            plugin.getSyncing().add(playertoInv.getUniqueId());
            playertoInv.getInventory().setContents(player.getInventory().getContents());
            plugin.getSyncing().remove(playertoInv.getUniqueId());
        });
    }
}
