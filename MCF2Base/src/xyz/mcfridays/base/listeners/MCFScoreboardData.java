package xyz.mcfridays.base.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import xyz.mcfridays.base.MCF;
import xyz.mcfridays.base.score.ScoreManager;
import xyz.mcfridays.base.team.MCFTeam;
import xyz.mcfridays.base.team.MCFTeamManager;
import xyz.zeeraa.novacore.NovaCore;
import xyz.zeeraa.novacore.module.NovaModule;
import xyz.zeeraa.novacore.module.modules.scoreboard.NetherBoardScoreboard;

public class MCFScoreboardData extends NovaModule implements Listener {
	private int taskId;

	@Override
	public String getName() {
		return "MCFScoreboardData";
	}

	@Override
	public void onLoad() {
		taskId = -1;
	}

	@Override
	public void onEnable() {
		if (taskId == -1) {
			taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(MCF.getInstance(), new Runnable() {

				@Override
				public void run() {
					for (Player player : Bukkit.getServer().getOnlinePlayers()) {
						int playerScore = ScoreManager.getInstance().getPlayerScore(player);
						int teamScore = 0;

						MCFTeam team = (MCFTeam) MCFTeamManager.getInstance().getPlayerTeam(player);

						if (team != null) {
							teamScore = team.getScore();
						}

						NetherBoardScoreboard.getInstance().setPlayerLine(2, player, ChatColor.GOLD + "Score: " + ChatColor.AQUA + playerScore);
						NetherBoardScoreboard.getInstance().setPlayerLine(3, player, ChatColor.GOLD + "Team score: " + ChatColor.AQUA + teamScore);
					}
					double[] recentTps = NovaCore.getInstance().getVersionIndependentUtils().getRecentTps();

					if (recentTps.length > 0) {
						double tps = recentTps[0];
						NetherBoardScoreboard.getInstance().setGlobalLine(13, ChatColor.GOLD + "Average TPS: " + formatTps(tps));
					}
				}
			}, 10L, 10L);
		}
	}

	@Override
	public void onDisable() {
		if (taskId != -1) {
			Bukkit.getScheduler().cancelTask(taskId);
			taskId = -1;
		}
	}

	private String formatTps(double tps) {
		return ((tps > 18.0) ? ChatColor.GREEN : (tps > 16.0) ? ChatColor.YELLOW : ChatColor.RED).toString() + ((tps > 20.0) ? "*" : "") + Math.min(Math.round(tps * 100.0) / 100.0, 20.0);
	}
}