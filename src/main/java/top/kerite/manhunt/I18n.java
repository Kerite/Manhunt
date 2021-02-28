package top.kerite.manhunt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class I18n {
    private static final String BUNDLE_NAME = "message";
    private static final ResourceBundle EMPTY_BUNDLE = new ResourceBundle() {
        @NotNull
        @Override
        public Enumeration<String> getKeys() {
            return new Vector<String>().elements();
        }

        @Override
        protected Object handleGetObject(@NotNull String key) {
            return null;
        }
    };
    private static I18n instance;
    private final ResourceBundle defaultBundle;
    private final Locale defaultLocale = Locale.getDefault();
    private final ManHunt plugin;
    private final Map<String, MessageFormat> messageCache = new HashMap<>();
    private ResourceBundle currentBundle;
    private ResourceBundle additionBundle;
    private Locale currentLocale = defaultLocale;

    public I18n(final ManHunt plugin) {
        this.plugin = plugin;
        this.defaultBundle = ResourceBundle.getBundle(BUNDLE_NAME, Locale.ENGLISH);
        this.currentBundle = defaultBundle;
        this.additionBundle = EMPTY_BUNDLE;
    }

    public static String tl(final String key, final Object... objects) {
        if (instance == null) {
            return "";
        }
        return instance.format(key, objects);
    }

    public String format(final String key, Object... objects) {
        String format = translate(key);
        MessageFormat messageFormat = messageCache.get(key);
        if (messageFormat == null) {
            try {
                messageFormat = new MessageFormat(format);
            } catch (final IllegalArgumentException ex) {
                Logger.getLogger("Manhunt").log(
                        Level.WARNING,
                        "Invalid translation key for " + key + ":" + ex.getMessage()
                );
                format = format.replaceAll("\\{(\\D*?)\\}", "\\[$1\\]");
                messageFormat = new MessageFormat(format);
            }
            messageCache.put(format, messageFormat);
        }
        return messageFormat.format(objects);
    }

    public void updateLocale(final String locCode) {
        if (locCode != null && !locCode.isEmpty()) {
            final String[] locParts = locCode.split("[_\\.]");
            if (locParts.length == 1) {
                currentLocale = new Locale(locParts[0]);
            } else if (locParts.length == 2) {
                currentLocale = new Locale(locParts[0], locParts[1]);
            } else if (locParts.length == 3) {
                currentLocale = new Locale(locParts[0], locParts[1], locParts[2]);
            }
        }
        ResourceBundle.clearCache();
        messageCache.clear();
        Logger.getLogger("Manhunt").log(Level.INFO, "Using locale " + currentLocale.toString());

        try {
            currentBundle = ResourceBundle.getBundle(BUNDLE_NAME, currentLocale);
        } catch (final MissingResourceException ex) {
            Logger.getLogger("Manhunt").log(
                    Level.WARNING,
                    "Missing translation file for " + currentLocale.toString()
            );
            currentBundle = EMPTY_BUNDLE;
        }

        try {
            additionBundle = ResourceBundle.getBundle(BUNDLE_NAME, currentLocale, new FileResClassLoader(I18n.class.getClassLoader(), plugin));
        } catch (final MissingResourceException e) {
            additionBundle = EMPTY_BUNDLE;
        }
    }

    public void onEnable() {
        instance = this;
    }

    public void onDisable() {
        instance = null;
    }

    private String translate(final String key) {
        try {
            try {
                // from file in data folder
                return additionBundle.getString(key);
            } catch (MissingResourceException ex) {
                // from resource bundled
                return currentBundle.getString(key);
            }
        } catch (final MissingResourceException ex) {
            Logger.getLogger("Manhunt").log(
                    Level.WARNING,
                    String.format("Missing translations key %s in translation file %s", ex.getKey(), currentBundle.getLocale()),
                    ex
            );
            // from resource default
            return defaultBundle.getString(key);
        }
    }

    private static class FileResClassLoader extends ClassLoader {
        private final File dataFolder;

        FileResClassLoader(final ClassLoader classLoader, final ManHunt plugin) {
            super(classLoader);
            this.dataFolder = plugin.getDataFolder();
        }

        @Nullable
        @Override
        public URL getResource(String name) {
            final File file = new File(dataFolder, name);
            if (file.exists()) {
                try {
                    return file.toURI().toURL();
                } catch (final MalformedURLException ignored) {
                }
            }
            return null;
        }

        @Nullable
        @Override
        public InputStream getResourceAsStream(String name) {
            final File file = new File(dataFolder, name);
            if (file.exists()) {
                try {
                    return new FileInputStream(file);
                } catch (final FileNotFoundException ignored) {
                }
            }
            return null;
        }
    }
}
