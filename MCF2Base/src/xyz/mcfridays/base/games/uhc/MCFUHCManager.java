package xyz.mcfridays.base.games.uhc;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;

import net.novauniverse.games.uhc.NovaUHC;
import net.novauniverse.games.uhc.game.UHC;
import net.zeeraa.novacore.commons.utils.TextUtils;
import net.zeeraa.novacore.spigot.module.NovaModule;
import net.zeeraa.novacore.spigot.module.modules.scoreboard.NetherBoardScoreboard;
import xyz.mcfridays.base.MCF;

public class MCFUHCManager extends NovaModule implements Listener {
	public static final int UHC_TIMER_AND_GENERATION_LINE = 6;

	private int taskId;
	private boolean worldGenerationShown;
	private boolean timeLineShown;

	@Override
	public String getName() {
		return "MCFUHCManager";
	}

	@Override
	public void onLoad() {
		this.taskId = -1;
		this.worldGenerationShown = false;
		this.timeLineShown = false;
	}

	@Override
	public void onEnable() {
		if (taskId == -1) {
			taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(MCF.getInstance(), new Runnable() {
				@Override
				public void run() {
					UHC uhc = NovaUHC.getInstance().getGame();

					if (!uhc.getWorldPreGenerator().isFinished()) {
						worldGenerationShown = true;

						NetherBoardScoreboard.getInstance().setGlobalLine(UHC_TIMER_AND_GENERATION_LINE, ChatColor.GOLD + "Generating world: " + ChatColor.AQUA + "" + ((int) (uhc.getWorldPreGenerator().getProgressValue() * 100)) + "%");
					} else if (worldGenerationShown) {
						worldGenerationShown = false;

						NetherBoardScoreboard.getInstance().clearGlobalLine(UHC_TIMER_AND_GENERATION_LINE);
					}

					if (uhc.getGracePeriodTimer().isRunning()) {
						timeLineShown = true;

						int timeLeft = uhc.getGracePeriodTimer().getTimeLeft();

						ChatColor color = ChatColor.GREEN;

						NetherBoardScoreboard.getInstance().setGlobalLine(UHC_TIMER_AND_GENERATION_LINE, ChatColor.GOLD + "Grace period end: " + color +TextUtils.secondsToHoursMinutes(timeLeft));
					} else if (timeLineShown) {
						timeLineShown = false;

						NetherBoardScoreboard.getInstance().clearGlobalLine(UHC_TIMER_AND_GENERATION_LINE);
					}

				}
			}, 5L, 5L);
		}
	}

	@Override
	public void onDisable() {
		if (taskId != -1) {
			Bukkit.getScheduler().cancelTask(taskId);
			taskId = -1;
		}
	}
}