package top.kerite.manhunt;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Logger;

public class TrackRunnable extends BukkitRunnable {
    private static final Logger LOGGER = Logger.getLogger("Manhunt");
    private final ManHuntMatch match;

    public TrackRunnable(ManHuntMatch match) {
        this.match = match;
        LOGGER.info("TrackRunnable initialized");
    }

    @Override
    public void run() {
        if (match.getMatchStatus() == MatchStatus.RUNNING) {
            match.processAllHunters();
        } else {
            LOGGER.info("TrackRunnable canceling");
            cancel();
        }
    }
}
