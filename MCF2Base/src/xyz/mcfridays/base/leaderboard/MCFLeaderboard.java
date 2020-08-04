package xyz.mcfridays.base.leaderboard;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;

import xyz.mcfridays.base.MCF;
import xyz.mcfridays.base.score.PlayerScoreData;
import xyz.mcfridays.base.score.TeamScoreData;
import xyz.mcfridays.base.score.TopScore;
import xyz.zeeraa.novacore.module.NovaModule;

public class MCFLeaderboard extends NovaModule {
	private static MCFLeaderboard instance;
	
	private Hologram playerHologram;
	private Hologram teamHologram;

	private int lines;

	private int taskId;

	public static MCFLeaderboard getInstance() {
		return instance;
	}
	
	@Override
	public void onLoad() {
		MCFLeaderboard.instance = this;
		
		this.playerHologram = null;
		this.teamHologram = null;

		this.lines = 5;

		this.taskId = -1;
	}

	@Override
	public void onEnable() {
		if (taskId == -1) {
			taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(MCF.getInstance(), new Runnable() {
				@Override
				public void run() {
					update();
				}
			}, 40L, 40L);
		}
	}

	@Override
	public void onDisable() {
		if (taskId != -1) {
			Bukkit.getScheduler().cancelTask(taskId);
			taskId = -1;
		}
		
		if (teamHologram != null) {
			teamHologram.clearLines();
			teamHologram.delete();
		}

		if (playerHologram != null) {
			playerHologram.clearLines();
			playerHologram.delete();
		}
	}

	public void update() {
		if (playerHologram != null) {
			int lineIndex = 0;

			if (playerHologram.size() <= lineIndex) {
				playerHologram.appendTextLine(ChatColor.GREEN + "" + ChatColor.BOLD + "Top player scores");
			}

			ArrayList<PlayerScoreData> scores = TopScore.getPlayerTopScore(lines);

			for (PlayerScoreData scoreData : scores) {
				lineIndex++;

				if (playerHologram.size() <= lineIndex) {
					playerHologram.appendTextLine("-----------");
				}
				((TextLine) playerHologram.getLine(lineIndex)).setText(ChatColor.YELLOW + "" + lineIndex + ChatColor.GOLD + " : " + scoreData.toString());
			}

			while (playerHologram.size() > lineIndex + 1) {
				playerHologram.removeLine(playerHologram.size() - 1);
			}
		}

		if (teamHologram != null) {
			int lineIndex = 0;

			if (teamHologram.size() <= lineIndex) {
				teamHologram.appendTextLine(ChatColor.GREEN + "" + ChatColor.BOLD + "Top team scores");
			}

			ArrayList<TeamScoreData> scores = TopScore.getTeamTopScore(lines);

			for (TeamScoreData scoreData : scores) {
				lineIndex++;

				if (teamHologram.size() <= lineIndex) {
					teamHologram.appendTextLine("-----------");
				}
				((TextLine) teamHologram.getLine(lineIndex)).setText(ChatColor.YELLOW + "" + lineIndex + ChatColor.GOLD + " : " + scoreData.toString());
			}

			while (teamHologram.size() > lineIndex + 1) {
				teamHologram.removeLine(teamHologram.size() - 1);
			}
		}
	}
	
	public void setTeamHologramLocation(Location location) {
		if (teamHologram != null) {
			teamHologram.delete();
		}

		teamHologram = HologramsAPI.createHologram(MCF.getInstance(), location);
	}

	public void setPlayerHologramLocation(Location location) {
		if (playerHologram != null) {
			playerHologram.delete();
		}

		playerHologram = HologramsAPI.createHologram(MCF.getInstance(), location);
	}

	public int getLines() {
		return lines;
	}

	public void setLines(int lines) {
		this.lines = lines;
	}

	@Override
	public String getName() {
		return "MCFLeaderboard";
	}
}