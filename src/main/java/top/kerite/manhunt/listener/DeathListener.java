package top.kerite.manhunt.listener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import top.kerite.manhunt.ManHuntMatch;
import top.kerite.manhunt.MatchStatus;
import top.kerite.manhunt.PlayerRole;

public class DeathListener implements Listener {
    private final ManHuntMatch match;

    public DeathListener(ManHuntMatch match) {
        this.match = match;
    }

    @EventHandler
    public void onRunnerDeath(PlayerDeathEvent event) {
        String playerName = event.getEntity().getName();
        if (match.getMatchStatus() != MatchStatus.RUNNING) {
            return;
        }
        if (match.isRunner(event.getEntity().getName())) {
            Bukkit.broadcastMessage(ChatColor.RED + "Runner " + event.getEntity().getDisplayName() + " has slain");
            if (match.eliminate(playerName)) {
                // Successfully eliminate player
                if (match.isAllEliminated()) {
                    Bukkit.broadcastMessage(ChatColor.GREEN + "All runners were eliminated!!!");
                    match.stop(null, PlayerRole.RUNNER);
                }
            }
        }
    }
}
