package x0derek.lifeShare;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Options implements Subcommand {

    private final LifeShare plugin;

    public Options(LifeShare plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Player player, Player secondPlayer, String[] args) {
        if (!plugin.isInGroup(player.getUniqueId())) {
            player.sendMessage(Component.text("You are not in a LifeShare group!", NamedTextColor.RED));
            return;
        }

        LifeShareGroup group = plugin.getGroup(player.getUniqueId());

        boolean ownerCanChange = plugin.getConfig().getBoolean("owner-can-change-options", true);

        if (!group.getOwner().equals(player.getUniqueId()) && !player.hasPermission("lifeshare.admin")) {
            player.sendMessage(Component.text("Only the group owner can change options!", NamedTextColor.RED));
            return;
        }

        if (group.getOwner().equals(player.getUniqueId()) && !ownerCanChange && !player.hasPermission("lifeshare.admin")) {
            player.sendMessage(Component.text("Owner option changes are disabled by an admin!", NamedTextColor.RED));
            return;
        }

        if (args.length < 2) {
            sendOptionsMenu(player, group);
            return;
        }

        Player owner = Bukkit.getPlayer(group.getOwner());
        if (owner == null) {
            player.sendMessage(Component.text("Owner is not online!", NamedTextColor.RED));
            return;
        }

        switch (args[1].toLowerCase()) {
            case "health":
                boolean newHealth = !group.isShareHealth();
                group.setShareHealth(newHealth);
                plugin.getDataManager().saveData();

                if (newHealth) {
                    plugin.getSyncManager().syncGroupHealth(group.getOwner(), owner.getHealth());
                }

                player.sendMessage(Component.text("Health sharing: ", NamedTextColor.GREEN)
                        .append(Component.text(newHealth ? "ENABLED" : "DISABLED", newHealth ? NamedTextColor.GREEN : NamedTextColor.RED)));
                sendOptionsMenu(player, group);
                break;

            case "hunger":
                boolean newHunger = !group.isShareHunger();
                group.setShareHunger(newHunger);
                plugin.getDataManager().saveData();

                if (newHunger) {
                    plugin.getSyncManager().syncGroupFood(group.getOwner(), owner.getFoodLevel());
                }

                player.sendMessage(Component.text("Hunger sharing: ", NamedTextColor.GREEN)
                        .append(Component.text(newHunger ? "ENABLED" : "DISABLED", newHunger ? NamedTextColor.GREEN : NamedTextColor.RED)));
                sendOptionsMenu(player, group);
                break;

            case "inventory":
                boolean newInventory = !group.isShareInventory();
                group.setShareInventory(newInventory);
                plugin.getDataManager().saveData();

                if (newInventory) {
                    plugin.getSyncManager().syncGroupInventory(group.getOwner());
                }

                player.sendMessage(Component.text("Inventory sharing: ", NamedTextColor.GREEN)
                        .append(Component.text(newInventory ? "ENABLED" : "DISABLED", newInventory ? NamedTextColor.GREEN : NamedTextColor.RED)));
                sendOptionsMenu(player, group);
                break;

            default:
                sendOptionsMenu(player, group);
                break;
        }
    }

    private void sendOptionsMenu(Player player, LifeShareGroup group) {
        boolean ownerCanChange = plugin.getConfig().getBoolean("owner-can-change-options", true);

        player.sendMessage(Component.text("===== LifeShare Options =====", NamedTextColor.GOLD));

        if (!group.getOwner().equals(player.getUniqueId())) {
            player.sendMessage(Component.text("Only the owner can change options!", NamedTextColor.RED));
        } else if (!ownerCanChange && !player.hasPermission("lifeshare.admin")) {
            player.sendMessage(Component.text("Owner changes are disabled by admin!", NamedTextColor.RED));
        }

        Component healthOption = Component.text("Health: ", NamedTextColor.GRAY)
                .append(Component.text(group.isShareHealth() ? "✔ ENABLED" : "✘ DISABLED",
                        group.isShareHealth() ? NamedTextColor.GREEN : NamedTextColor.RED));

        if (canChangeOptions(player, group)) {
            healthOption = healthOption.append(Component.text(" [CLICK]", NamedTextColor.YELLOW)
                    .clickEvent(ClickEvent.runCommand("/lifeshare options health"))
                    .hoverEvent(HoverEvent.showText(Component.text("Toggle health sharing", NamedTextColor.GREEN))));
        }

        Component hungerOption = Component.text("Hunger: ", NamedTextColor.GRAY)
                .append(Component.text(group.isShareHunger() ? "✔ ENABLED" : "✘ DISABLED",
                        group.isShareHunger() ? NamedTextColor.GREEN : NamedTextColor.RED));

        if (canChangeOptions(player, group)) {
            hungerOption = hungerOption.append(Component.text(" [CLICK]", NamedTextColor.YELLOW)
                    .clickEvent(ClickEvent.runCommand("/lifeshare options hunger"))
                    .hoverEvent(HoverEvent.showText(Component.text("Toggle hunger sharing", NamedTextColor.GREEN))));
        }

        Component inventoryOption = Component.text("Inventory: ", NamedTextColor.GRAY)
                .append(Component.text(group.isShareInventory() ? "✔ ENABLED" : "✘ DISABLED",
                        group.isShareInventory() ? NamedTextColor.GREEN : NamedTextColor.RED));

        if (canChangeOptions(player, group)) {
            inventoryOption = inventoryOption.append(Component.text(" [CLICK]", NamedTextColor.YELLOW)
                    .clickEvent(ClickEvent.runCommand("/lifeshare options inventory"))
                    .hoverEvent(HoverEvent.showText(Component.text("Toggle inventory sharing", NamedTextColor.GREEN))));
        }

        player.sendMessage(healthOption);
        player.sendMessage(hungerOption);
        player.sendMessage(inventoryOption);
        player.sendMessage(Component.text("===========================", NamedTextColor.GOLD));
    }

    private boolean canChangeOptions(Player player, LifeShareGroup group) {
        boolean ownerCanChange = plugin.getConfig().getBoolean("owner-can-change-options", true);

        if (player.hasPermission("lifeshare.admin")) return true;
        if (group.getOwner().equals(player.getUniqueId()) && ownerCanChange) return true;

        return false;
    }
}