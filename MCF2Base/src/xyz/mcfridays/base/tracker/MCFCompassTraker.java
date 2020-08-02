package xyz.mcfridays.base.tracker;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import xyz.zeeraa.ezcore.module.compass.CompassTrackerTarget;
import xyz.zeeraa.ezcore.module.game.GameManager;

public class MCFCompassTraker implements CompassTrackerTarget {
	@Override
	public Location getCompassTarget(Player player) {
		if(GameManager.getInstance().hasGame()) {
			List<UUID> players = GameManager.getInstance().getActiveGame().getPlayers();
			
			players.remove(player.getUniqueId());
			
			double closestDistance = Double.MAX_VALUE;
			Location result = null;
			
			for(UUID uuid : players) {
				Player p = Bukkit.getServer().getPlayer(uuid);
				
				if(p != null) {
					if(p.isOnline()) {
						if(p.getLocation().getWorld() == player.getLocation().getWorld()) {
							double dist = player.getLocation().distance(p.getLocation());
							
							if(dist < closestDistance) {
								closestDistance = dist;
								result = p.getLocation();
							}
						}
					}
				}
			}
			
			return result;
		}
		return null;
	}
}