package top.kerite.manhunt;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface IManhuntMatch {
    boolean isMember(@NotNull String player, @Nullable PlayerRole role);

    /**
     * get team of entity
     *
     * @param entityName entityName
     * @return null - entity is not exist or is not a player or is not in this match
     */
    @Nullable Team getTeam(@NotNull String entityName);

    @NotNull Team getTeam(@NotNull PlayerRole role);

    void join(Player player, PlayerRole role, CommandSender sender);

    Set<String> getMembers(@NotNull PlayerRole role);

    void start(@Nullable CommandSender sender);

    void stop(@Nullable CommandSender sender, @Nullable PlayerRole winner);

    @NotNull MatchStatus getMatchStatus();

    /**
     * eliminate a player
     *
     * @param player eliminated player name
     * @return successfully eliminated player (player exists and is a runner)
     */
    boolean eliminate(String player);

    boolean isEliminated(String player);

    boolean isAllEliminated();

    void processAllHunters();

    void reload();
}
