package xyz.mcfridays.base.deathmessage;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;

import net.zeeraa.novacore.spigot.NovaCore;
import net.zeeraa.novacore.spigot.module.modules.game.elimination.PlayerEliminationReason;
import net.zeeraa.novacore.spigot.module.modules.game.messages.PlayerEliminationMessage;
import net.zeeraa.novacore.spigot.teams.Team;

public class MCFPlayerEliminationMessage implements PlayerEliminationMessage {
	@Override
	public void showPlayerEliminatedMessage(OfflinePlayer player, Entity killer, PlayerEliminationReason reason, int placement) {
		ChatColor playerColor = ChatColor.AQUA;

		String extra = "";

		if (NovaCore.getInstance().getTeamManager() != null) {
			Team playerTeam = NovaCore.getInstance().getTeamManager().getPlayerTeam(player);
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

		case COMMAND:
			extra += "was elimanated by an admin";
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
			} else if (killer.getUniqueId().toString().equalsIgnoreCase("1a3d3d3c-7083-4db5-85cb-4046160152a1") || killer.getUniqueId().toString().equalsIgnoreCase("83c3d18d-3b17-4e9e-ba7b-8887d5fc5183") || killer.getUniqueId().toString().equalsIgnoreCase("39e0dea8-43f7-4983-9399-fc0048b4c556")) {
				extra += "called hax on " + killer.getName();
			} else {
				extra += "was killed " + killerName;
			}
			break;

		default:
			extra += "went out to buy some milk but never returned";
			break;
		}

		Bukkit.getServer().broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "Player Eliminated> " + playerColor + ChatColor.BOLD + player.getName() + " " + ChatColor.GOLD + ChatColor.BOLD + extra);
	}
}