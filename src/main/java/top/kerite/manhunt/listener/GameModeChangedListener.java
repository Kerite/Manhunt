package top.kerite.manhunt.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import top.kerite.manhunt.IManhuntMatch;
import top.kerite.manhunt.MatchStatus;

public class GameModeChangedListener implements Listener {
    private final IManhuntMatch match;

    public GameModeChangedListener(IManhuntMatch match) {
        this.match = match;
    }

    @EventHandler
    public void onGameModeChanged(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        if (match.isMember(player.getName(), null)) {
            if (match.getMatchStatus() == MatchStatus.RUNNING) {
                event.setCancelled(true);
                player.sendMessage("You can't be other game mode while match running");
            }
        }
    }
}
