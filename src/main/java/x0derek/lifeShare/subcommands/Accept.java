package x0derek.lifeShare.subcommands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import x0derek.lifeShare.LifeShare;
import x0derek.lifeShare.LifeShareGroup;
import x0derek.lifeShare.Subcommand;

import java.util.UUID;

public class Accept implements Subcommand {

    private final LifeShare plugin;

    public Accept(LifeShare plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Player player, Player secondPlayer, String[] args) {
        UUID inviterUUID = plugin.getInvitations().get(player.getUniqueId());

        if (inviterUUID == null) {
            player.sendMessage(Component.text("You don't have any pending invitations!", NamedTextColor.RED));
            return;
        }

        plugin.getInvitations().remove(player.getUniqueId());

        if (plugin.isInGroup(player.getUniqueId())) {
            player.sendMessage(Component.text("You are already in a group!", NamedTextColor.RED));
            return;
        }

        Player inviter = Bukkit.getPlayer(inviterUUID);
        if (inviter == null) {
            player.sendMessage(Component.text("The player who invited you left the server!", NamedTextColor.RED));
            return;
        }

        LifeShareGroup group;
        UUID inviterOwnerUUID = plugin.getPlayerGroups().get(inviterUUID);

        if (inviterOwnerUUID == null) {
            group = new LifeShareGroup(inviterUUID);
            plugin.getGroups().put(inviterUUID, group);
            plugin.getPlayerGroups().put(inviterUUID, inviterUUID);
        } else {
            group = plugin.getGroups().get(inviterOwnerUUID);
        }

        group.addMember(player.getUniqueId());
        plugin.getPlayerGroups().put(player.getUniqueId(), group.getOwner());
        plugin.getDataManager().saveData();

        if (group.isShareHealth()) {
            player.setHealth(inviter.getHealth());
        }
        if (group.isShareInventory()) {
            player.getInventory().setContents(inviter.getInventory().getContents());
            player.getInventory().setArmorContents(inviter.getInventory().getArmorContents());
        }
        if (group.isShareHunger()) {
            player.setFoodLevel(inviter.getFoodLevel());
            player.setSaturation(inviter.getSaturation());
        }

        plugin.getSyncManager().syncAllGroupData(inviter.getUniqueId());

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        inviter.playSound(inviter.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

        inviter.sendMessage(
                Component.text(player.getName(), NamedTextColor.YELLOW)
                        .append(Component.text(" accepted your LifeShare invitation!", NamedTextColor.GREEN))
        );

        player.sendMessage(
                Component.text("You joined LifeShare with ", NamedTextColor.GREEN)
                        .append(Component.text(inviter.getName(), NamedTextColor.YELLOW))
                        .append(Component.text("!", NamedTextColor.GREEN))
        );
    }
}