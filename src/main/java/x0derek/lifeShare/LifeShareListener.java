package x0derek.lifeShare;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;


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
}
