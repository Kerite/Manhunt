package top.kerite.manhunt;

import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.kerite.manhunt.event.MatchStartedEvent;
import top.kerite.manhunt.event.MatchStoppedEvent;
import top.kerite.manhunt.listener.CompassDropListener;
import top.kerite.manhunt.listener.DeathListener;
import top.kerite.manhunt.listener.GameModeChangedListener;
import top.kerite.manhunt.listener.MatchStatusListener;
import top.kerite.manhunt.util.ItemUtil;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import static top.kerite.manhunt.I18n.tl;

public class ManHuntMatch implements IManhuntMatch {
    public static final String TEAM_NAME_RUNNER = "MANHUNT_RUNNER";
    public static final String TEAM_NAME_HUNTER = "MANHUNT_HUNTER";
    public final NamespacedKey eliminatedFlagKey;
    public final byte eliminatedFlag = 0x39;
    private final Logger log;
    private final Team teamHunter;
    private final Team teamRunner;
    private final Scoreboard scoreboard;
    private final List<Listener> listeners = new LinkedList<>();
    private final Plugin plugin;
    private TrackRunnable trackRunnable;
    private MatchStatus status = MatchStatus.PENDING;
    private Integer glowDistance = 15;
    private Integer prepareTime = 30;

    public ManHuntMatch(Plugin plugin) {
        this.plugin = plugin;

        log = plugin.getLogger();

        eliminatedFlagKey = new NamespacedKey(plugin, "eliminated");

        this.scoreboard = Objects.requireNonNull(plugin.getServer().getScoreboardManager()).getNewScoreboard();

        this.teamHunter = scoreboard.registerNewTeam(TEAM_NAME_HUNTER);
        this.teamRunner = scoreboard.registerNewTeam(TEAM_NAME_RUNNER);

        teamHunter.setColor(ChatColor.AQUA);
        teamRunner.setColor(ChatColor.RED);

        teamHunter.setAllowFriendlyFire(true);
        teamRunner.setAllowFriendlyFire(false);

        plugin.getServer().getPluginManager().registerEvents(new CompassDropListener(this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new MatchStatusListener(this), plugin);
    }

    public void resetEliminated(String player) {
        Player playerObj = Bukkit.getPlayerExact(player);
        if (playerObj == null) {
            return;
        }
        PersistentDataContainer container = playerObj.getPersistentDataContainer();
        container.remove(eliminatedFlagKey);
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    public boolean isHunter(String entity) {
        Team team = getScoreboard().getEntryTeam(entity);
        if (team == null) {
            return false;
        }
        return teamHunter.getName().equals(team.getName());
    }

    public boolean isRunner(String entity) {
        Team team = getScoreboard().getEntryTeam(entity);
        if (team == null) {
            return false;
        }
        return teamRunner.getName().equals(team.getName());
    }

    public Team getTeamByRole(PlayerRole role) {
        if (role == PlayerRole.HUNTER) {
            return teamHunter;
        } else if (role == PlayerRole.RUNNER) {
            return teamRunner;
        } else {
            return null;
        }
    }

    public String getTeamName(@NotNull String entityName) {
        Team team = scoreboard.getEntryTeam(entityName);
        if (team == null) {
            return "NO_TEAM";
        }
        return team.getName();
    }

    @Override
    public boolean eliminate(String player) {
        if (isRunner(player)) {
            Player player1 = Bukkit.getPlayerExact(player);
            if (player1 == null) {
                return false;
            }
            player1.getPersistentDataContainer().set(eliminatedFlagKey, PersistentDataType.BYTE, eliminatedFlag);
            return true;
        }
        return false;
    }

    @NotNull
    @Override
    public MatchStatus getMatchStatus() {
        return status;
    }

    @NotNull
    @Override
    public Set<String> getMembers(@NotNull PlayerRole role) {
        switch (role) {
            case HUNTER:
                return getHunters();
            case RUNNER:
                return getRunners();
            default:
                return new HashSet<>();
        }
    }

    @Override
    public @Nullable Team getTeam(@NotNull String entityName) {
        return scoreboard.getTeam(entityName);
    }

    @Override
    public @NotNull Team getTeam(@NotNull PlayerRole role) {
        if (role == PlayerRole.RUNNER) {
            return teamRunner;
        } else if (role == PlayerRole.HUNTER) {
            return teamHunter;
        } else {
            throw new IllegalArgumentException("Invalid role " + role.getName());
        }
    }

    public boolean isAllEliminated() {
        AtomicBoolean ret = new AtomicBoolean(true);
        teamRunner.getEntries().forEach(playerName -> {
            if (!isEliminated(playerName)) {
                ret.set(false);
            }
        });
        return ret.get();
    }

    public boolean isEliminated(String player) {
        if (!isRunner(player)) {
            return false;
        }
        Player player1 = Bukkit.getPlayerExact(player);
        if (player1 == null) {
            return false;
        }
        PersistentDataContainer container = player1.getPersistentDataContainer();
        return container.has(eliminatedFlagKey, PersistentDataType.BYTE);
    }

    @Override
    public boolean isMember(@NotNull String player, @Nullable PlayerRole role) {
        if (role == PlayerRole.HUNTER) {
            return isHunter(player);
        } else if (role == PlayerRole.RUNNER) {
            return isRunner(player);
        } else if (role == null) {
            return isHunter(player) || isRunner(player);
        } else {
            return false;
        }
    }

    @Override
    public void join(Player player, PlayerRole role, CommandSender sender) {
        if (!checkStatus(MatchStatus.PENDING, sender)) {
            return;
        }
        log.info(tl("statusCheckSuccess"));
        if (!player.isOnline()) {
            if (sender != null) {
                sender.sendMessage(tl("playerOffline", player.getName()));
            }
            return;
        }
        log.info(tl("playerOnline", player.getName()));
        Team team = getTeamByRole(role);
        if (team == null) {
            return;
        }
        team.addEntry(player.getName());
        Bukkit.getServer().broadcastMessage(tl("playerJoined", player.getName(), role.getDisplayedName()));
    }

    @Override
    public void processAllHunters() {
        List<Player> runners = new LinkedList<>();
        teamRunner.getEntries().forEach(runnerName -> {
            Player runner = Bukkit.getPlayerExact(runnerName);
            if (runner != null) {
                runners.add(runner);
            }
        });
        teamHunter.getEntries().forEach(playerName -> {
            Player hunter = Bukkit.getPlayerExact(playerName);
            Player trackedRunner = null;
            double distance = 0;
            if (hunter == null) {
                return;
            }
            Location hunterLocation = hunter.getLocation();
            // 更新每个 hunter 的追踪目标
            for (Player runner : runners) {
                if (trackedRunner == null || hunterLocation.distance(runner.getLocation()) < distance) {
                    trackedRunner = runner;
                    distance = hunterLocation.distance(runner.getLocation());
                }
            }

            if (trackedRunner == null) {
                hunter.sendMessage(tl("missingRunner"));
            }
            hunter.getInventory().setItem(8, ItemUtil.getCompass(trackedRunner));

            if (distance < glowDistance) {
                hunter.addPotionEffect(PotionEffectType.GLOWING.createEffect(20, 1));
            }
        });
    }

    @Override
    public void reload() {
        this.updateConfig();
        this.stop(null, null);
    }

    @Override
    public void start(CommandSender sender) {
        if (!checkStatus(MatchStatus.PENDING, sender)) {
            return;
        }

        this.status = MatchStatus.STARTING;
        teamHunter.getEntries().forEach(e -> {
            Player player = Bukkit.getPlayerExact(e);
            if (player != null) {
                initHunter(player);
            }
        });
        teamRunner.getEntries().forEach(e -> {
            Player player = Bukkit.getPlayerExact(e);
            initRunner(player);
        });
        initListeners();

        this.status = MatchStatus.RUNNING;
        Bukkit.getServer().getPluginManager().callEvent(new MatchStartedEvent());
        log.info(tl("matchStarted"));

        if (trackRunnable != null && !trackRunnable.isCancelled()) {
            stop(sender, null);
            sendIfNotNull(sender, tl("stoppingLastMatch"));
            return;
        }
        trackRunnable = new TrackRunnable(this);
        trackRunnable.runTaskTimer(plugin, 10L, 10L);
    }

    @Override
    public void stop(CommandSender sender, PlayerRole winner) {
        if (!checkStatus(MatchStatus.RUNNING, sender)) {
            return;
        }
        this.status = MatchStatus.STOPPING;

        unloadListeners();

        this.status = MatchStatus.PENDING;
        Bukkit.getServer().getPluginManager().callEvent(new MatchStoppedEvent(winner));
    }

    void updateConfig() {
        this.glowDistance = plugin.getConfig().getInt(ManhuntConfig.CONFIG_GLOW_DISTANCE, 15);
        this.prepareTime = plugin.getConfig().getInt(ManhuntConfig.CONFIG_RUNNER_START_TIME, 30);
    }

    @Override
    public String toString() {
        return "ManHuntMatch{" +
                "status=" + status +
                '}';
    }

    private boolean checkStatus(MatchStatus requiredStatus, CommandSender sender) {
        if (requiredStatus != status) {
            sendIfNotNull(sender, tl("statusWrong", status.name()));
            return false;
        }
        return true;
    }

    private Set<String> getHunters() {
        return teamHunter.getEntries();
    }

    private Set<String> getRunners() {
        return teamRunner.getEntries();
    }

    private void initHunter(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, prepareTime * 20, 255));
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, prepareTime * 20, 255));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, prepareTime * 20, 255));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, prepareTime * 20, 255));
        initPlayer(player);
    }

    private void initListeners() {
        if (listeners.size() == 0) {
            listeners.add(new GameModeChangedListener(this));
            listeners.add(new DeathListener(this));
        }
        for (Listener listener : listeners) {
            Bukkit.getPluginManager().registerEvents(listener, plugin);
        }
    }

    private void initPlayer(Player player) {
        if (player == null) {
            return;
        }
        resetEliminated(player.getName());
        player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setSaturation(5);
        player.setExhaustion(0);
        player.getInventory().clear();
        revokeAllAdvancement(player);
    }

    private void initRunner(Player player) {
        initPlayer(player);
    }

    private void revokeAllAdvancement(Player player) {
        Iterator<Advancement> advancementIterator = Bukkit.getServer().advancementIterator();
        while (advancementIterator.hasNext()) {
            AdvancementProgress progress = player.getAdvancementProgress(advancementIterator.next());
            for (String s : progress.getAwardedCriteria()) {
                progress.revokeCriteria(s);
            }
        }
    }

    private void sendIfNotNull(CommandSender sender, String message) {
        if (sender != null) {
            sender.sendMessage(message);
        }
    }

    private void unloadListeners() {
        for (Listener listener : listeners) {
            HandlerList.unregisterAll(listener);
        }
    }
}
