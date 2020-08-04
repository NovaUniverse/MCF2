package xyz.mcfridays.base.deathmessage;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import xyz.mcfridays.base.team.MCFTeam;
import xyz.zeeraa.novacore.module.modules.game.eliminationmessage.TeamEliminationMessage;
import xyz.zeeraa.novacore.teams.Team;
import xyz.zeeraa.novacore.utils.TextUtils;

public class MCFTeamEliminationMessage implements TeamEliminationMessage {
	@Override
	public void showTeamEliminatedMessage(Team team, int placement) {
		if (team instanceof MCFTeam) {
			MCFTeam mcfTeam = (MCFTeam) team;

			Bukkit.getServer().broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "Team Eliminated> " + mcfTeam.getTeamColor() + ChatColor.BOLD + ("Team " + mcfTeam.getTeamNumber()) + ChatColor.GOLD + ChatColor.BOLD + " " + TextUtils.ordinal(placement) + " place");
		}
	}
}