package xyz.mcfridays.base.lobby.duels;

public enum InviteResult {
	/**
	 * Invalid invite code
	 */
	INVALID,
	/**
	 * Valid code and the duel was started
	 */
	OK,
	/**
	 * The player that sent the request is busy
	 */
	PLAYER_BUSY,
	/**
	 * Duels failed to start for some reason
	 */
	START_FAILURE;
}