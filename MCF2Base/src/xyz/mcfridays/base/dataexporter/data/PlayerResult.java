package xyz.mcfridays.base.dataexporter.data;

import java.util.UUID;

public class PlayerResult {
	private UUID uuid;

	private int teamId;
	private int score;
	private int kills;

	public PlayerResult(UUID uuid, int teamId, int score, int kills) {
		this.uuid = uuid;
		this.teamId = teamId;
		this.score = score;
		this.kills = kills;
	}

	public UUID getUuid() {
		return uuid;
	}

	public int getTeamId() {
		return teamId;
	}

	public int getScore() {
		return score;
	}

	public int getKills() {
		return kills;
	}
}