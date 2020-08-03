package xyz.mcfridays.base.tracker;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import xyz.zeeraa.novacore.module.modules.compass.CompassTarget;
import xyz.zeeraa.novacore.module.modules.compass.CompassTrackerTarget;
import xyz.zeeraa.novacore.module.modules.game.GameManager;


public class MCFCompassTraker implements CompassTrackerTarget {
	@Override
	public CompassTarget getCompassTarget(Player player) {
		if(GameManager.getInstance().hasGame()) {
			List<UUID> players = GameManager.getInstance().getActiveGame().getPlayers();
			
			players.remove(player.getUniqueId());
			
			double closestDistance = Double.MAX_VALUE;
			CompassTarget result = null;
			
			for(UUID uuid : players) {
				Player p = Bukkit.getServer().getPlayer(uuid);
				
				if(p != null) {
					if(p.isOnline()) {
						if(p.getLocation().getWorld() == player.getLocation().getWorld()) {
							double dist = player.getLocation().distance(p.getLocation());
							
							if(dist < closestDistance) {
								closestDistance = dist;
								result = new CompassTarget(p.getLocation(), "Tracking player " + p.getName());
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