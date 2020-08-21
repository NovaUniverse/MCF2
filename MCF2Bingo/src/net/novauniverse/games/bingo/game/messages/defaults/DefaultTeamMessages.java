package net.novauniverse.games.bingo.game.messages.defaults;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import net.novauniverse.games.bingo.game.messages.TeamCompleteGameMessage;
import net.novauniverse.games.bingo.game.messages.TeamFailMessage;
import net.novauniverse.games.bingo.game.messages.TeamFindItemMessage;
import net.zeeraa.novacore.teams.Team;
import net.zeeraa.novacore.utils.TextUtils;

public class DefaultTeamMessages implements TeamFindItemMessage, TeamCompleteGameMessage, TeamFailMessage {
	@Override
	public void showTeamFindItemMessage(Team team, int totalItems) {
		Bukkit.getServer().broadcastMessage(team.getTeamColor() + "" + ChatColor.BOLD + team.getDisplayName() + ChatColor.GREEN + "" + ChatColor.BOLD + " found " + totalItems + " / 9 items");
	}

	@Override
	public void showTeamCompleteGameMessage(Team team, int placement) {
		Bukkit.getServer().broadcastMessage(team.getTeamColor() + "" + ChatColor.BOLD + team.getDisplayName() + ChatColor.GREEN + "" + ChatColor.BOLD + " found all the items. " + ChatColor.AQUA + ChatColor.BOLD + TextUtils.ordinal(placement) + " place");
	}

	@Override
	public void showTeamFailMessage(Team team) {
		Bukkit.getServer().broadcastMessage(team.getTeamColor() + "" + ChatColor.BOLD + team.getDisplayName() + ChatColor.RED + ChatColor.BOLD + " did not find all items");
	}
}