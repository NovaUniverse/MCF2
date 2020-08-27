package xyz.mcfridays.base.tracker;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.zeeraa.novacore.spigot.NovaCore;
import net.zeeraa.novacore.spigot.module.modules.compass.CompassTarget;
import net.zeeraa.novacore.spigot.module.modules.compass.CompassTrackerTarget;
import net.zeeraa.novacore.spigot.module.modules.game.GameManager;
import xyz.mcfridays.base.team.MCFTeam;

public class MCFCompassTraker implements CompassTrackerTarget {
	@Override
	public CompassTarget getCompassTarget(Player player) {
		if (GameManager.getInstance().hasGame()) {
			@SuppressWarnings("unchecked")
			List<UUID> players = (List<UUID>) GameManager.getInstance().getActiveGame().getPlayers().clone();

			players.remove(player.getUniqueId());

			double closestDistance = Double.MAX_VALUE;
			CompassTarget result = null;

			MCFTeam team = null;
			if (NovaCore.getInstance().hasTeamManager()) {
				team = (MCFTeam) NovaCore.getInstance().getTeamManager().getPlayerTeam(player);
			}

			for (UUID uuid : players) {
				Player p = Bukkit.getServer().getPlayer(uuid);

				if (p != null) {
					if (p.isOnline()) {
						if (GameManager.getInstance().hasGame()) {
							if (!GameManager.getInstance().getActiveGame().getPlayers().contains(p.getUniqueId())) {
								//Log.trace(player.getName() + " Ignoring player not in game " + p.getName());
								continue;
							}
						}
						if (p.getLocation().getWorld() == player.getLocation().getWorld()) {
							if (team != null) {
								MCFTeam p2team = (MCFTeam) NovaCore.getInstance().getTeamManager().getPlayerTeam(p);

								if (p2team != null) {
									if (team.getTeamUuid().toString().equalsIgnoreCase(p2team.getTeamUuid().toString())) {
										//Log.trace(player.getName() + " Ignoring same team player " + p.getName());
										continue;
									}
								}
							}

							double dist = player.getLocation().distance(p.getLocation());

							if (dist < closestDistance) {
								closestDistance = dist;
								result = new CompassTarget(p.getLocation(), "Tracking player " + p.getName());
							}
						}
					} else {
						//Log.trace(player.getName() + " Ignoring offline " + uuid.toString());
					}
				} else {
					//Log.trace(player.getName() + " Ignoring missing " + uuid.toString());
				}
			}

			return result;
		}
		return null;
	}
}