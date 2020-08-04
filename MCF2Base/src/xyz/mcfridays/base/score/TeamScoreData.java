package xyz.mcfridays.base.score;

import org.bukkit.ChatColor;

import xyz.mcfridays.base.team.MCFTeam;

public class TeamScoreData extends ScoreData {
	private MCFTeam team;

	public TeamScoreData(MCFTeam team) {
		super(team.getScore());
		this.team = team;
	}
	
	public TeamScoreData(MCFTeam team, int score) {
		super(score);
		this.team = team;
	}

	public Integer getTeamNumber() {
		return team.getTeamNumber();
	}

	public MCFTeam getTeam() {
		return team;
	}

	@Override
	public String toString() {
		String teamName;
		
		if(team.getMembers().size() > 0) {
			teamName = team.getTeamColor() + team.getMemberString();
		} else {
			teamName = team.getTeamColor() + "Team " + team.getTeamNumber();
		}

		return teamName + ChatColor.GOLD + " : " + ChatColor.AQUA + this.getScore();
	}
}