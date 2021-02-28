package top.kerite.manhunt;

public class ManhuntConfig {
    public static final String CONFIG_LOCALE = "locale";
    public static final String CONFIG_GLOW_DISTANCE = "glow-distance";
    public static final String CONFIG_RUNNER_START_TIME = "start-time";
    private static ManhuntConfig INSTANCE;

    void onEnable() {
        INSTANCE = this;
    }

    void onDisable() {
        INSTANCE = null;
    }
}
