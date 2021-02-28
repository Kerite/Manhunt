package top.kerite.manhunt.listener;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import top.kerite.manhunt.IManhuntMatch;
import top.kerite.manhunt.PlayerRole;
import top.kerite.manhunt.util.ItemUtil;
import top.kerite.manhunt.MatchStatus;

public class CompassDropListener implements Listener {
    private final IManhuntMatch match;

    public CompassDropListener(final IManhuntMatch match) {
        this.match = match;
    }

    @EventHandler
    public void onCompassDropped(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Item item = event.getItemDrop();
        if (item.getItemStack().getType() == Material.COMPASS) {
            if (match.getMatchStatus() != MatchStatus.RUNNING) {
                item.remove();
            } else if (match.isMember(player.getName(), PlayerRole.HUNTER)) {
                if (ItemUtil.isTrackingDevice(item.getItemStack())) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
