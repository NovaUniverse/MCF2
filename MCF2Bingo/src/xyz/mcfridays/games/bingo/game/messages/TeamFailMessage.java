package xyz.mcfridays.games.bingo.game.messages;

import net.zeeraa.novacore.teams.Team;

public interface TeamFailMessage {
	/**
	 * Show a message when a team failed the game
	 * 
	 * @param team The {@link Team} that failed the game
	 */
	public void showTeamFailMessage(Team team);
}