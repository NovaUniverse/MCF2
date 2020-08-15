package xyz.mcfridays.games.bingo.game.messages;

import net.zeeraa.novacore.teams.Team;

public interface TeamCompleteGameMessage {
	/**
	 * Show a message when a team completes the game
	 * 
	 * @param team      The {@link Team} that completed the game
	 * @param placement The placement of the team
	 */
	public void showTeamCompleteGameMessage(Team team, int placement);
}