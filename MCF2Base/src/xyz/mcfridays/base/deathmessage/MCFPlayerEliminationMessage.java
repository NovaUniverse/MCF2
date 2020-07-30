package xyz.mcfridays.base.deathmessage;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;

import xyz.zeeraa.ezcore.EZCore;
import xyz.zeeraa.ezcore.module.game.PlayerEliminationMessage;
import xyz.zeeraa.ezcore.module.game.PlayerEliminationReason;
import xyz.zeeraa.ezcore.teams.Team;

public class MCFPlayerEliminationMessage implements PlayerEliminationMessage {
	@Override
	public void showPlayerEliminatedMessage(OfflinePlayer player, Entity killer, PlayerEliminationReason reason) {
		ChatColor playerColor = ChatColor.AQUA;

		String extra = "";

		if (EZCore.getInstance().getTeamManager() != null) {
			Team playerTeam = EZCore.getInstance().getTeamManager().getPlayerTeam(player);
			if (playerTeam != null) {
				playerColor = playerTeam.getTeamColor();
			}
		}

		switch (reason) {
		case DEATH:
			extra += "died";
			break;

		case DID_NOT_RECONNECT:
			extra += "did not reconnect in time";
			break;

		case QUIT:
			extra += "quit";
			break;

		case KILLED:
			String killerName = "";
			if (killer != null) {
				if (killer instanceof Projectile) {
					Entity theBoiWhoFirered = (Entity) ((Projectile) killer).getShooter();

					if (theBoiWhoFirered != null) {
						killerName = "by " + theBoiWhoFirered.getName();
					} else {
						killerName = "by " + killer.getName();
					}
				} else {
					killerName = "by " + killer.getName();
				}
			}

			if (killer.getUniqueId().toString().equalsIgnoreCase("ca2e347b-025a-4e7b-8019-752b83661f7f")) {
				extra += "took the L to " + killer.getName();
			} else if (killer.getUniqueId().toString().equalsIgnoreCase("5203face-89ca-49b7-a5a0-f2cf0fe230e7")) {
				extra += "was sent to the gulag by " + killer.getName();
			} else if (killer.getUniqueId().toString().equalsIgnoreCase("22a9eca8-2221-4bc9-b463-de0f3a0cc652")) {
				extra += "was deleted by " + killer.getName();
			} else {
				extra += "was killed " + killerName;
			}
			break;

		case OTHER:
			extra += "went out to buy some milk but never returned";
			break;
		default:
			extra += "went out to buy some milk but never returned";
			break;
		}

		Bukkit.getServer().broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "Player Eliminated> " + playerColor + ChatColor.BOLD + player.getName() + " " + net.md_5.bungee.api.ChatColor.GOLD + ChatColor.BOLD + extra);
	}
}