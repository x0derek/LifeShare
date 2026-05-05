package x0derek.lifeShare.subcommands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import x0derek.lifeShare.LifeShare;
import x0derek.lifeShare.LifeShareGroup;
import x0derek.lifeShare.Subcommand;

import java.util.UUID;

public class Quit implements Subcommand {

    private final LifeShare plugin;

    public Quit(LifeShare plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Player player, Player secondPlayer, String[] args) {
        if (!plugin.isInGroup(player.getUniqueId())) {
            player.sendMessage(Component.text("You are not in a LifeShare group!", NamedTextColor.RED));
            return;
        }

        LifeShareGroup group = plugin.getGroup(player.getUniqueId());
        group.removeMember(player.getUniqueId());
        plugin.getPlayerGroups().remove(player.getUniqueId());

        if (group.getOwner().equals(player.getUniqueId())) {
            plugin.getGroups().remove(player.getUniqueId());
            for (UUID memberUUID : group.getMembers()) {
                plugin.getPlayerGroups().remove(memberUUID);
                Player member = Bukkit.getPlayer(memberUUID);
                if (member != null) {
                    member.sendMessage(Component.text("The LifeShare group has been disbanded!", NamedTextColor.RED));
                }
            }
        }

        plugin.getDataManager().saveData();
        player.sendMessage(Component.text("You left the LifeShare group!", NamedTextColor.GREEN));
    }
}