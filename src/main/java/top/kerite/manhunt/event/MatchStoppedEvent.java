package top.kerite.manhunt.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.kerite.manhunt.PlayerRole;

public class MatchStoppedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final PlayerRole winner;

    public MatchStoppedEvent(@Nullable PlayerRole winner) {
        this.winner = winner;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public PlayerRole getWinner() {
        return winner;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
