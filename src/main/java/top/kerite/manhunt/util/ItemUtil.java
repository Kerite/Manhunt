package top.kerite.manhunt.util;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;
import top.kerite.manhunt.ManHunt;

import static top.kerite.manhunt.I18n.tl;

public class ItemUtil {
    public static final String NBT_COMPASS_TYPE_KEY_NAME = "compass_type";
    public static final Byte NBT_COMPASS_TYPE_VALUE_TRACKING = 0x39; // 39 for miku
    public static final Byte NBT_COMPASS_TYPE_VALUE_DEFAULT = 0x11;
    public static NamespacedKey nbtCompassTypeKey = new NamespacedKey(ManHunt.getInstance(), NBT_COMPASS_TYPE_KEY_NAME);

    public static ItemStack getCompass(@Nullable Player trackedPlayer) {
        ItemStack compass = new ItemStack(Material.COMPASS, 1);
        compass.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
        compass.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 1);

        CompassMeta meta = (CompassMeta) compass.getItemMeta();
        assert meta != null;
        meta.setLodestoneTracked(false);
        if (trackedPlayer != null) {
            meta.setLodestone(trackedPlayer.getLocation());
            meta.setDisplayName(tl("compassDisplayName", trackedPlayer.getName()));
        } else {
            meta.setLodestone(null);
            meta.setDisplayName(tl("compassNoTrackingDisplayName"));
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(nbtCompassTypeKey, PersistentDataType.BYTE, NBT_COMPASS_TYPE_VALUE_TRACKING);

        compass.setItemMeta(meta);

        return compass;
    }

    public static boolean isTrackingDevice(ItemStack itemStack) {
        if (itemStack.getType() != Material.COMPASS) {
            return false;
        }
        CompassMeta compassMeta = (CompassMeta) itemStack.getItemMeta();
        if (compassMeta == null) {
            return false;
        }
        if (compassMeta.getPersistentDataContainer().has(nbtCompassTypeKey, PersistentDataType.BYTE)) {
            return compassMeta.getPersistentDataContainer()
                    .getOrDefault(nbtCompassTypeKey, PersistentDataType.BYTE, NBT_COMPASS_TYPE_VALUE_DEFAULT)
                    .equals(NBT_COMPASS_TYPE_VALUE_TRACKING);
        }
        return false;
    }
}
