package xyz.mcfridays.base.team;

import org.bukkit.ChatColor;

import net.zeeraa.novacore.teams.Team;
import xyz.mcfridays.base.misc.MCFPlayerNameCache;

public class MCFTeam extends Team {
	private int teamNumber;
	private int score;

	public MCFTeam(int teamNumber, int score) {
		this.teamNumber = teamNumber;
		this.score = score;
	}

	public int getTeamNumber() {
		return teamNumber;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public String getMemberString() {
		String result = "";

		for (int i = members.size(); i > 0; i--) {
			result += MCFPlayerNameCache.getInstance().getPlayerName(members.get(i - 1)) + (i == 1 ? "" : (i == 2 ? " and " : ", "));
		}

		return result;
	}

	@Override
	public ChatColor getTeamColor() {
		switch (((teamNumber - 1) % 12) + 1) {
		case 1:
			return ChatColor.DARK_BLUE;

		case 2:
			return ChatColor.DARK_GREEN;

		case 3:
			return ChatColor.DARK_AQUA;

		case 4:
			return ChatColor.DARK_RED;

		case 5:
			return ChatColor.DARK_PURPLE;

		case 6:
			return ChatColor.GOLD;

		case 7:
			return ChatColor.GRAY;

		case 8:
			return ChatColor.BLUE;

		case 9:
			return ChatColor.GREEN;

		case 10:
			return ChatColor.AQUA;

		case 11:
			return ChatColor.RED;

		case 12:
			return ChatColor.LIGHT_PURPLE;

		default:
			return ChatColor.YELLOW;
		}
	}

	@Override
	public String getDisplayName() {
		return "Team " + this.getTeamNumber();
	}
}