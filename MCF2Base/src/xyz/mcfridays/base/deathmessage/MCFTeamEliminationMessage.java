package xyz.mcfridays.base.deathmessage;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import net.zeeraa.novacore.commons.utils.TextUtils;
import net.zeeraa.novacore.spigot.module.modules.game.messages.TeamEliminationMessage;
import net.zeeraa.novacore.spigot.teams.Team;
import xyz.mcfridays.base.team.MCFTeam;

public class MCFTeamEliminationMessage implements TeamEliminationMessage {
	@Override
	public void showTeamEliminatedMessage(Team team, int placement) {
		if (team instanceof MCFTeam) {
			MCFTeam mcfTeam = (MCFTeam) team;

			Bukkit.getServer().broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "Team Eliminated> " + mcfTeam.getTeamColor() + ChatColor.BOLD + ("Team " + mcfTeam.getTeamNumber()) + ChatColor.GOLD + ChatColor.BOLD + " " + TextUtils.ordinal(placement) + " place");
		}
	}
}