package xyz.mcfridays.base.games.bingo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import net.novauniverse.games.bingo.NovaBingo;
import net.novauniverse.games.bingo.game.Bingo;
import net.novauniverse.games.bingo.game.event.TeamCompleteGameEvent;
import net.novauniverse.games.bingo.game.event.TeamCompleteItemEvent;
import net.zeeraa.novacore.commons.utils.TextUtils;
import net.zeeraa.novacore.spigot.module.NovaModule;
import net.zeeraa.novacore.spigot.module.modules.scoreboard.NetherBoardScoreboard;
import net.zeeraa.novacore.spigot.teams.Team;
import xyz.mcfridays.base.MCF;
import xyz.mcfridays.base.score.ScoreManager;
import xyz.mcfridays.base.scoreboard.MCFScoreboard;
import xyz.mcfridays.base.team.MCFTeam;

public class MCFBingoManger extends NovaModule implements Listener {
	public static final int BINGO_TIMER_AND_GENERATION_LINE = 6;

	private int taskId;
	private boolean worldGenerationShown;
	private boolean bingoTimerShown;

	@Override
	public String getName() {
		return "MCFBingoManager";
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

						if (timeLeft < 300) {
							color = ChatColor.RED;
						} else if (timeLeft < 600) {
							color = ChatColor.YELLOW;
						} else {
							color = ChatColor.GREEN;
						}

						NetherBoardScoreboard.getInstance().setGlobalLine(BINGO_TIMER_AND_GENERATION_LINE, ChatColor.GOLD + "Time left: " + color + "" + TextUtils.secondsToHoursMinutes(timeLeft));
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

	private void addTeamScoreToPlayers(Team team, int score) {
		List<Player> players = new ArrayList<Player>();

		for (UUID uuid : team.getMembers()) {
			Player player = Bukkit.getServer().getPlayer(uuid);

			if (player != null) {
				if (player.isOnline()) {
					players.add(player);
				}
			}
		}
		
		if(players.size() > 0) {
		
		int scoreToAdd = (int) Math.ceil(((double) score ) / players.size());
		
		for(Player player : players) {
			ScoreManager.getInstance().addPlayerScore(player, scoreToAdd, false);
		}}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onTeamCompleteItem(TeamCompleteItemEvent e) {
		MCFTeam team = (MCFTeam) e.getTeam();

		int score = 20;

		ScoreManager.getInstance().addTeamScore(team.getTeamNumber(), score);
		addTeamScoreToPlayers(team, score);

		team.sendMessage(ChatColor.GRAY + "+" + score + " team score");

	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onTeamCompleteGame(TeamCompleteGameEvent e) {
		MCFTeam team = (MCFTeam) e.getTeam();

		int score = 100 - (e.getPlacement() * 10);

		if (score <= 50) {
			score = 50;
		}

		ScoreManager.getInstance().addTeamScore(team.getTeamNumber(), score);
		addTeamScoreToPlayers(team, score);

		team.sendMessage(ChatColor.GRAY + "+" + score + " team score");
	}
}