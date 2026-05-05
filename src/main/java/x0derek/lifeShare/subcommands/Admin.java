package x0derek.lifeShare.subcommands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import x0derek.lifeShare.LifeShare;
import x0derek.lifeShare.Subcommand;

public class Admin implements Subcommand {

    private final LifeShare plugin;

    public Admin(LifeShare plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Player player, Player secondPlayer, String[] args) {
        if (!player.hasPermission("lifeshare.admin")) {
            player.sendMessage(Component.text("You don't have permission to use this command!", NamedTextColor.RED));
            return;
        }

        if (args.length < 2) {
            sendAdminHelp(player);
            return;
        }

        switch (args[1].toLowerCase()) {
            case "setownerperms":
                if (args.length < 3) {
                    player.sendMessage(Component.text("Usage: /lifeshare admin setownerperms <true/false>", NamedTextColor.RED));
                    return;
                }

                boolean value = Boolean.parseBoolean(args[2]);
                plugin.getConfig().set("owner-can-change-options", value);
                plugin.saveConfig();

                player.sendMessage(Component.text("Owner permissions set to: ", NamedTextColor.GREEN)
                        .append(Component.text(value ? "OWNERS CAN CHANGE OPTIONS" : "ONLY ADMINS CAN CHANGE OPTIONS",
                                value ? NamedTextColor.GREEN : NamedTextColor.RED)));
                break;

            case "reload":
                plugin.reloadConfig();
                plugin.getDataManager().loadData();
                player.sendMessage(Component.text("LifeShare configuration reloaded!", NamedTextColor.GREEN));
                break;

            default:
                sendAdminHelp(player);
                break;
        }
    }

    private void sendAdminHelp(Player player) {
        player.sendMessage(Component.text("===== LifeShare Admin Commands =====", NamedTextColor.GOLD));
        player.sendMessage(Component.text("/lifeshare admin setownerperms <true/false>", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("  - true: Owners can change options", NamedTextColor.GRAY));
        player.sendMessage(Component.text("  - false: Only admins can change options", NamedTextColor.GRAY));
        player.sendMessage(Component.text("/lifeshare admin reload", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("  - Reload configuration", NamedTextColor.GRAY));
        player.sendMessage(Component.text("===================================", NamedTextColor.GOLD));
    }
}