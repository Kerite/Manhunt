package top.kerite.manhunt;

import org.bukkit.ChatColor;

public enum PlayerRole {
    HUNTER("hunter", ChatColor.RED + "hunter" + ChatColor.RESET),
    RUNNER("runner", ChatColor.AQUA + "runner" + ChatColor.RESET);

    private final String name;
    private final String displayedName;

    PlayerRole(String name, String displayedName) {
        this.name = name;
        this.displayedName = displayedName;
    }

    public static PlayerRole getRole(String name) {
        if (name.equalsIgnoreCase(HUNTER.getName())) {
            return HUNTER;
        } else if (name.equalsIgnoreCase(RUNNER.getName())) {
            return RUNNER;
        } else {
            return null;
        }
    }

    public String getName() {
        return name;
    }

    public String getDisplayedName() {
        return displayedName;
    }
}
