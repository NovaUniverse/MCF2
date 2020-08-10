package xyz.mcfridays.base.bingo;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;

import xyz.mcfridays.base.MCF;
import xyz.mcfridays.games.bingo.MCFBingo;
import xyz.mcfridays.games.bingo.game.Bingo;
import xyz.zeeraa.novacore.module.NovaModule;
import xyz.zeeraa.novacore.module.modules.scoreboard.NetherBoardScoreboard;

public class MCFBingoManger extends NovaModule implements Listener {
	public static final int LOADING_WORLD_LINE = 5;

	private int taskId;
	private boolean worldGenerationShown;

	@Override
	public String getName() {
		return "MCFBingoManger";
	}

	@Override
	public void onLoad() {
		this.taskId = -1;
		this.worldGenerationShown = false;
	}

	@Override
	public void onEnable() {
		if (taskId == -1) {
			taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(MCF.getInstance(), new Runnable() {
				@Override
				public void run() {
					Bingo bingo = MCFBingo.getInstance().getGame();

					if (!bingo.getWorldPreGenerator().isFinished()) {
						worldGenerationShown = true;

						NetherBoardScoreboard.getInstance().setGlobalLine(LOADING_WORLD_LINE, ChatColor.AQUA + "Generating world: " + ((int) (bingo.getWorldPreGenerator().getProgressValue() * 100)) + "%");
					} else if (worldGenerationShown) {
						worldGenerationShown = false;

						NetherBoardScoreboard.getInstance().clearGlobalLine(LOADING_WORLD_LINE);
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