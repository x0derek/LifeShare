package x0derek.lifeShare;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import x0derek.lifeShare.subcommands.*;

import java.util.List;
import java.util.stream.Collectors;

public class LifeShareCommand implements CommandExecutor, TabCompleter {

    private final LifeShare plugin;

    public LifeShareCommand(LifeShare plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        var players = Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toList());

        if (args.length == 1) {
            return List.of("invite", "accept", "deny", "quit", "options", "admin");
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("invite")) {
            return players;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("options")) {
            return List.of("health", "hunger", "inventory");
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("admin")) {
            return List.of("setownerperms", "reload");
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("admin") && args[1].equalsIgnoreCase("setownerperms")) {
            return List.of("true", "false");
        }

        return List.of();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can execute this command!", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(Component.text("Usage: /lifeshare <invite|accept|deny|quit|options|admin>", NamedTextColor.RED));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "invite":
                if (args.length < 2) {
                    player.sendMessage(Component.text("Usage: /lifeshare invite <player>", NamedTextColor.RED));
                    break;
                }

                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
                    break;
                }

                new Invitation(plugin).execute(player, target, args);
                break;

            case "accept":
                new Accept(plugin).execute(player, null, args);
                break;

            case "deny":
                new Deny(plugin).execute(player, null, args);
                break;

            case "quit":
                new Quit(plugin).execute(player, null, args);
                break;

            case "options":
                new Options(plugin).execute(player, null, args);
                break;

            case "admin":
                new Admin(plugin).execute(player, null, args);
                break;

            default:
                player.sendMessage(Component.text("Unknown subcommand! Use: invite, accept, deny, quit, options, admin", NamedTextColor.RED));
                break;
        }

        return true;
    }
}