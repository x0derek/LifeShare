package x0derek.lifeShare.subcommands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import x0derek.lifeShare.LifeShare;
import x0derek.lifeShare.Subcommand;

public class Invitation implements Subcommand {
    private final LifeShare plugin;

    public Invitation(LifeShare plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Player player, Player secondPlayer, String[] args) {
        if (secondPlayer == null) {
            player.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
            return;
        }

        if (player.getUniqueId().equals(secondPlayer.getUniqueId())) {
            player.sendMessage(Component.text("You cannot invite yourself!", NamedTextColor.RED));
            return;
        }

        if (plugin.hasInviteCooldown(player.getUniqueId())) {
            player.sendMessage(Component.text("Please wait 5 seconds before sending another invitation!", NamedTextColor.RED));
            return;
        }

        if (plugin.getInvitations().containsKey(secondPlayer.getUniqueId())) {
            player.sendMessage(Component.text("This player already has a pending invitation!", NamedTextColor.RED));
            return;
        }

        if (plugin.isInGroup(secondPlayer.getUniqueId())) {
            player.sendMessage(Component.text("This player is already in a LifeShare group!", NamedTextColor.RED));
            return;
        }

        plugin.setInviteCooldown(player.getUniqueId());
        plugin.getInvitations().put(secondPlayer.getUniqueId(), player.getUniqueId());

        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        secondPlayer.playSound(secondPlayer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 2.0f);

        player.sendMessage(
                Component.text("You invited ", NamedTextColor.GREEN)
                        .append(Component.text(secondPlayer.getName(), NamedTextColor.YELLOW))
                        .append(Component.text(" to LifeShare!", NamedTextColor.GREEN))
        );

        secondPlayer.sendMessage(
                Component.text("", NamedTextColor.GRAY)
                        .append(Component.text(player.getName(), NamedTextColor.YELLOW))
                        .append(Component.text(" invited you to ", NamedTextColor.GRAY))
                        .append(Component.text("LifeShare", NamedTextColor.GOLD))
                        .append(Component.text("! ", NamedTextColor.GRAY))
                        .append(Component.text("[✔ ACCEPT]", NamedTextColor.GREEN)
                                .clickEvent(ClickEvent.runCommand("/lifeshare accept"))
                                .hoverEvent(HoverEvent.showText(Component.text("Click to accept!", NamedTextColor.GREEN))))
                        .append(Component.text(" ", NamedTextColor.GRAY))
                        .append(Component.text("[✘ DENY]", NamedTextColor.RED)
                                .clickEvent(ClickEvent.runCommand("/lifeshare deny"))
                                .hoverEvent(HoverEvent.showText(Component.text("Click to deny!", NamedTextColor.RED))))
        );
    }
}