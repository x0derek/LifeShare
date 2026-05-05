package x0derek.lifeShare.subcommands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import x0derek.lifeShare.LifeShare;
import x0derek.lifeShare.Subcommand;

import java.util.UUID;

public class Deny implements Subcommand {

    private final LifeShare plugin;

    public Deny(LifeShare plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Player player, Player secondPlayer, String[] args) {
        UUID inviterUUID = plugin.getInvitations().get(player.getUniqueId());

        if (inviterUUID == null) {
            player.sendMessage(Component.text("You don't have any pending invitations!", NamedTextColor.RED));
            return;
        }

        Player inviter = Bukkit.getPlayer(inviterUUID);
        plugin.getInvitations().remove(player.getUniqueId());

        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        player.sendMessage(Component.text("You denied the LifeShare invitation.", NamedTextColor.RED));

        if (inviter != null) {
            inviter.playSound(inviter.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            inviter.sendMessage(
                    Component.text(player.getName(), NamedTextColor.YELLOW)
                            .append(Component.text(" denied your LifeShare invitation!", NamedTextColor.RED))
            );
        }
    }
}