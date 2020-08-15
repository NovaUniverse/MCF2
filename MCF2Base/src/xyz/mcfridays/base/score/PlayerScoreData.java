package xyz.mcfridays.base.score;

import java.util.UUID;

import org.bukkit.ChatColor;

import net.zeeraa.novacore.NovaCore;
import net.zeeraa.novacore.teams.Team;
import xyz.mcfridays.base.misc.MCFPlayerNameCache;

public class PlayerScoreData extends ScoreData {
	private UUID uuid;

	public PlayerScoreData(UUID uuid, int score) {
		super(score);
		this.uuid = uuid;
	}

	public UUID getUuid() {
		return uuid;
	}

	@Override
	public String toString() {
		ChatColor color = ChatColor.AQUA;
		
		if(NovaCore.getInstance().hasTeamManager()) {
			Team team = NovaCore.getInstance().getTeamManager().getPlayerTeam(uuid);
			
			if(team != null) {
				color=team.getTeamColor();
			}
		}
		
		return color + MCFPlayerNameCache.getInstance().getPlayerName(uuid) + ChatColor.GOLD + " : " + ChatColor.AQUA + this.getScore();
	}
}