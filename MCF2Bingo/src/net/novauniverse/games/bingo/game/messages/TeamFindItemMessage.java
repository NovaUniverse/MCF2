package net.novauniverse.games.bingo.game.messages;

import net.zeeraa.novacore.teams.Team;

public interface TeamFindItemMessage {
	/**
	 * Show a message when a team finds an item
	 * 
	 * @param team       The {@link Team} that found the item
	 * @param totalItems The amount of items the team has found
	 */
	public void showTeamFindItemMessage(Team team, int totalItems);
}