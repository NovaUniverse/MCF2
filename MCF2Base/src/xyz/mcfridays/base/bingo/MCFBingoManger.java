package xyz.mcfridays.base.bingo;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;

import net.novauniverse.games.bingo.NovaBingo;
import net.novauniverse.games.bingo.game.Bingo;
import net.zeeraa.novacore.commons.utils.TextUtils;
import net.zeeraa.novacore.spigot.module.NovaModule;
import net.zeeraa.novacore.spigot.module.modules.scoreboard.NetherBoardScoreboard;
import xyz.mcfridays.base.MCF;

public class MCFBingoManger extends NovaModule implements Listener {
	public static final int BINGO_TIMER_AND_GENERATION_LINE = 6;

	private int taskId;
	private boolean worldGenerationShown;
	private boolean bingoTimerShown;

	@Override
	public String getName() {
		return "MCFBingoManger";
	}

	@Override
	public void onLoad() {
		this.taskId = -1;
		this.worldGenerationShown = false;
		this.bingoTimerShown = false;
	}

	@Override
	public void onEnable() {
		if (taskId == -1) {
			taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(MCF.getInstance(), new Runnable() {
				@Override
				public void run() {
					Bingo bingo = NovaBingo.getInstance().getGame();

					if (!bingo.getWorldPreGenerator().isFinished()) {
						worldGenerationShown = true;

						NetherBoardScoreboard.getInstance().setGlobalLine(BINGO_TIMER_AND_GENERATION_LINE, ChatColor.GOLD + "Generating world: " + ChatColor.AQUA + "" + ((int) (bingo.getWorldPreGenerator().getProgressValue() * 100)) + "%");
					} else if (worldGenerationShown) {
						worldGenerationShown = false;

						NetherBoardScoreboard.getInstance().clearGlobalLine(BINGO_TIMER_AND_GENERATION_LINE);
					}
					
					if (bingo.getGameTimer().isRunning()) {
						bingoTimerShown = true;

						int timeLeft = bingo.getGameTimer().getTimeLeft();
						
						ChatColor color;
						
						if(timeLeft < 300) {
							color = ChatColor.RED;
						} else if(timeLeft < 600){
							color = ChatColor.YELLOW;
						} else {
							color = ChatColor.GREEN;
						}
						
						NetherBoardScoreboard.getInstance().setGlobalLine(BINGO_TIMER_AND_GENERATION_LINE, ChatColor.GOLD + "Time left: " + color+ "" + TextUtils.secondsToHoursMinutes(timeLeft));
					} else if (bingoTimerShown) {
						bingoTimerShown = false;

						NetherBoardScoreboard.getInstance().clearGlobalLine(BINGO_TIMER_AND_GENERATION_LINE);
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

	// TODO: Add score
}