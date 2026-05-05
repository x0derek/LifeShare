package x0derek.lifeShare;

import org.bukkit.entity.Player;

public interface Subcommand {
    void execute(Player player, Player secondPlayer, String[] args);
}
