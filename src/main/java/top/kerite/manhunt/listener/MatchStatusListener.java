package top.kerite.manhunt.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import top.kerite.manhunt.IManhuntMatch;
import top.kerite.manhunt.event.MatchStoppedEvent;

import java.util.logging.Logger;

import static top.kerite.manhunt.I18n.tl;

public class MatchStatusListener implements Listener {
    private static final Logger LOGGER = Logger.getLogger("Manhunt");
    private final transient IManhuntMatch match;

    public MatchStatusListener(IManhuntMatch match) {
        this.match = match;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMatchStopped(MatchStoppedEvent event) {
        Bukkit.getServer().broadcastMessage(tl("matchFinished"));
        if (event.getWinner() != null) {
            Bukkit.getServer().broadcastMessage(tl("matchFinishedWinner", event.getWinner().getDisplayedName()));
        } else {
            Bukkit.getServer().broadcastMessage(tl("matchFinishedNoWinner"));
        }
    }
}
