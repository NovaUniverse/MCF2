package xyz.mcfridays.base.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import xyz.mcfridays.base.MCF;
import xyz.mcfridays.base.kills.MCFPlayerKillCache;
import xyz.mcfridays.base.score.ScoreManager;
import xyz.mcfridays.base.team.MCFTeam;
import xyz.mcfridays.base.team.MCFTeamManager;
import xyz.zeeraa.novacore.NovaCore;
import xyz.zeeraa.novacore.module.NovaModule;
import xyz.zeeraa.novacore.module.modules.scoreboard.NetherBoardScoreboard;
import xyz.zeeraa.novacore.utils.TextUtils;

public class MCFScoreboard extends NovaModule implements Listener {
	private int taskId;

	@Override
	public String getName() {
		return "MCFScoreboard";
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
						NetherBoardScoreboard.getInstance().setPlayerLine(3, player, ChatColor.GOLD + "Kills: " + ChatColor.AQUA + MCFPlayerKillCache.getInstance().getPlayerKills(player.getUniqueId()));
						NetherBoardScoreboard.getInstance().setPlayerLine(4, player, ChatColor.GOLD + "Team score: " + ChatColor.AQUA + teamScore);

						int ping = NovaCore.getInstance().getVersionIndependentUtils().getPlayerPing(player);

						NetherBoardScoreboard.getInstance().setPlayerLine(12, player, ChatColor.GOLD + "Your ping: " + formatPing(ping) + "ms " + (ping > 800 ? ChatColor.YELLOW + TextUtils.ICON_WARNING : ""));
					}

					double[] recentTps = NovaCore.getInstance().getVersionIndependentUtils().getRecentTps();

					if (recentTps.length > 0) {
						double tps = recentTps[0];
						NetherBoardScoreboard.getInstance().setGlobalLine(13, ChatColor.GOLD + "Average TPS: " + formatTps(tps) + (tps < 18 ? " " + ChatColor.RED + TextUtils.ICON_WARNING : ""));
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

	private String formatPing(int ping) {
		ChatColor color = ChatColor.DARK_RED;

		if (ping < 200) {
			color = ChatColor.GREEN;
		} else if (ping < 400) {
			color = ChatColor.DARK_GREEN;
		} else if (ping < 600) {
			color = ChatColor.YELLOW;
		} else if (ping < 800) {
			color = ChatColor.RED;
		}

		return color + "" + ping;
	}

	private String formatTps(double tps) {
		return ((tps > 18.0) ? ChatColor.GREEN : (tps > 16.0) ? ChatColor.YELLOW : ChatColor.RED).toString() + ((tps > 20.0) ? "*" : "") + Math.min(Math.round(tps * 100.0) / 100.0, 20.0);
	}
}