package top.kerite.manhunt;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class ManhuntConfig {
    public static final String CONFIG_LOCALE = "locale";
    public static final String CONFIG_GLOW_DISTANCE = "glow-distance";
    public static final String CONFIG_RUNNER_START_TIME = "start-time";
    private static List<String> configList = null;

    private static String getConstValue(String fieldName) {
        try {
            return (String) ManhuntConfig.class.getField(fieldName).get(null);
        } catch (Exception e) {
            return null;
        }
    }

    public static List<String> getConfigList() {
        if (configList == null) {
            Class<ManhuntConfig> clazz = ManhuntConfig.class;
            Field[] fields = clazz.getFields();
            configList = Arrays.stream(fields).map(item -> getConstValue(item.getName())).collect(Collectors.toList());
        }
        return configList;
    }
}
