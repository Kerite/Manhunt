package top.kerite.manhunt;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

import static top.kerite.manhunt.I18n.tl;

public class ManHunt extends JavaPlugin implements IManhunt {
    private static final Logger LOGGER = Logger.getLogger("Manhunt");
    private static ManHunt INSTANCE;
    private static Scoreboard sb;
    private static Permission perms = null;
    private I18n i18n;
    private IManhuntMatch match;

    public static ManHunt getInstance() {
        return INSTANCE;
    }

    @Override
    public void onDisable() {
        INSTANCE = null;
        i18n.onDisable();
        LOGGER.info("ManHunt disabled");
    }

    @Override
    public void onEnable() {
        INSTANCE = this;
        if (LOGGER != this.getLogger()) {
            LOGGER.setParent(this.getLogger());
        }

        i18n = new I18n(this);
        i18n.onEnable();

        saveDefaultConfig();
        setupPermissions();

        match = new ManHuntMatch(this);

        // Setup command
        ManhuntCommand jc = new ManhuntCommand(perms, LOGGER, this, match);
        PluginCommand manhuntCommand = getCommand("manhunt");
        if (manhuntCommand != null) {
            manhuntCommand.setExecutor(jc);
            manhuntCommand.setTabCompleter(jc);
        }

        LOGGER.info(tl("pluginEnabled"));
    }

    @Override
    public void reload() {
        reloadConfig();
        i18n.updateLocale(getConfig().getString("locale"));
        match.reload();
    }

    @Override
    public FileConfiguration getFileConfig() {
        return getConfig();
    }

    private void setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp == null) {
            LOGGER.warning(tl("missingPermsProvider"));
            return;
        }
        perms = rsp.getProvider();
    }
}
