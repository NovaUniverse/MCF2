package xyz.mcfridays.base.listeners;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import xyz.mcfridays.base.score.ScoreManager;
import xyz.zeeraa.novacore.module.modules.game.GameManager;
import xyz.zeeraa.novacore.module.modules.game.events.PlayerEliminatedEvent;

public class ScoreListener implements Listener {
	private boolean killScoreEnabled;
	private int killScore;

	private boolean winScoreEnabled;
	private int[] winScore;

	private boolean participationScoreEnabled;
	private int participationScore;

	public ScoreListener(boolean killScoreEnabled, int killScore, boolean winScoreEnabled, int[] winScore, boolean participationScoreEnabled, int participationScore) {
		this.killScoreEnabled = killScoreEnabled;
		this.killScore = killScore;

		this.winScoreEnabled = winScoreEnabled;
		this.winScore = winScore;

		this.participationScoreEnabled = participationScoreEnabled;
		this.participationScore = participationScore;
	}

	public boolean isKillScoreEnabled() {
		return killScoreEnabled;
	}

	public boolean isWinScoreEnabled() {
		return winScoreEnabled;
	}

	public boolean isParticipationScoreEnabled() {
		return participationScoreEnabled;
	}

	public int getKillScore() {
		return killScore;
	}

	public int[] getWinScore() {
		return winScore;
	}

	public int getParticipationScore() {
		return participationScore;
	}

	public void setKillScore(int killScore) {
		this.killScore = killScore;
	}

	public void setWinScore(int[] winScore) {
		this.winScore = winScore;
	}

	public void setParticipationScore(int participationScore) {
		this.participationScore = participationScore;
	}

	
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerEliminated(PlayerEliminatedEvent e) {
		if (participationScoreEnabled) {
			if (GameManager.getInstance().hasGame()) {
				if (GameManager.getInstance().getActiveGame().hasStarted()) {
					for (UUID uuid : GameManager.getInstance().getActiveGame().getPlayers()) {
						if (uuid == e.getPlayer().getUniqueId()) {
							continue;
						}

						Player player = Bukkit.getServer().getPlayer(uuid);
						if (player != null) {
							if (player.isOnline()) {
								ScoreManager.getInstance().addPlayerScore(player, participationScore, true);
								player.sendMessage(ChatColor.GRAY + "" + ChatColor.BOLD + "+" + participationScore + " Participation score");
							}
						}
					}
				}
			}
		}
	}
}