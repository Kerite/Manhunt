package top.kerite.manhunt;

import org.bukkit.configuration.file.FileConfiguration;

public interface IManhunt {
    void reload();
    FileConfiguration getFileConfig();
}
