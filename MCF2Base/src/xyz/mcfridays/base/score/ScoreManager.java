package xyz.mcfridays.base.score;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import net.zeeraa.novacore.log.Log;
import net.zeeraa.novacore.module.NovaModule;
import net.zeeraa.novacore.module.modules.game.events.PlayerEliminatedEvent;
import xyz.mcfridays.base.MCF;
import xyz.mcfridays.base.team.MCFTeam;

public class ScoreManager extends NovaModule implements Listener {
	private static ScoreManager instance;
	private HashMap<UUID, Integer> playerScoreCache;

	private int taskId;

	public static ScoreManager getInstance() {
		return instance;
	}
	
	@Override
	public void onLoad() {
		ScoreManager.instance = this;
		this.playerScoreCache = new HashMap<UUID, Integer>();

		this.taskId = -1;
	}

	@Override
	public void onEnable() {
		if (taskId == -1) {
			taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(MCF.getInstance(), new Runnable() {

				@Override
				public void run() {
					for (UUID uuid : playerScoreCache.keySet()) {
						updatePlayerScore(uuid);
					}
				}
			}, 100L, 100L);
		}
	}

	@Override
	public void onDisable() {
		if (taskId != -1) {
			Bukkit.getScheduler().cancelTask(taskId);
			taskId = -1;
		}
	}

	@Override
	public String getName() {
		return "MCFScoreManager";
	}

	public int updatePlayerScore(UUID uuid) {
		int score = 0;
		try {
			String sql = "SELECT score FROM players WHERE uuid = ?";
			PreparedStatement ps = MCF.getDBConnection().getConnection().prepareStatement(sql);

			ps.setString(1, uuid.toString());

			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				score = rs.getInt("score");
				playerScoreCache.put(uuid, score);
			}

			rs.close();
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
			Log.warn("ScoreManager", "Failed to fetch the score of player with uuid: " + uuid.toString());
		}
		return score;
	}

	public HashMap<UUID, Integer> getPlayerScoreCache() {
		return playerScoreCache;
	}
	
	public int getPlayerScore(OfflinePlayer player) {
		return getPlayerScore(player.getUniqueId());
	}

	public int getPlayerScore(UUID uuid) {
		if (playerScoreCache.containsKey(uuid)) {
			return playerScoreCache.get(uuid);
		}

		return updatePlayerScore(uuid);
	}

	public boolean addPlayerScore(OfflinePlayer player, int score) {
		return this.addPlayerScore(player, score, true);
	}

	public boolean addPlayerScore(OfflinePlayer player, int score, boolean addToTeam) {
		return addPlayerScore(player.getUniqueId(), score, addToTeam);
	}

	public boolean addPlayerScore(UUID uuid, int score) {
		return this.addPlayerScore(uuid, score, true);
	}

	public int getTeamScore(MCFTeam team) {
		if (team == null) {
			return 0;
		}

		return team.getScore();
	}

	public int getTeamScore(int teamId) {
		return getTeamScore(MCF.getInstance().getTeamManager().getTeam(teamId));
	}

	public boolean addPlayerScore(UUID uuid, int score, boolean addToTeam) {
		try {
			String sql = "UPDATE players SET score = score + ? WHERE uuid = ?";
			PreparedStatement ps = MCF.getDBConnection().getConnection().prepareStatement(sql);

			ps.setInt(1, score);
			ps.setString(2, uuid.toString());

			if (playerScoreCache.containsKey(uuid)) {
				int oldScore = playerScoreCache.get(uuid);

				playerScoreCache.put(uuid, oldScore + score);
			}

			int count = ps.executeUpdate();

			ps.close();

			if (count == 0) {
				return false;
			}

			if (addToTeam) {
				MCFTeam team = (MCFTeam) MCF.getInstance().getTeamManager().getPlayerTeam(uuid);
				if (team != null) {
					this.addTeamScore(team, score);
				}
			}

			return true;
		} catch (Exception ee) {
			ee.printStackTrace();

			String message = "!!!Score update failure!!! Player with uuid: " + uuid.toString() + " failed to add " + score + " score";
			String query = "UPDATE players SET score = score + " + score + " WHERE uuid = '" + uuid.toString() + "';";

			Log.error("Failed to add score to a player. Please check the sql_fix.sql file");
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + message);

			logFailedQuery(query);
		}
		return false;
	}

	public boolean addTeamScore(MCFTeam team, int score) {
		return this.addTeamScore(team.getTeamNumber(), score);
	}

	public boolean addTeamScore(int teamId, int score) {
		try {
			String sql = "UPDATE teams SET score = score + ? WHERE team_number = ?";
			PreparedStatement ps = MCF.getDBConnection().getConnection().prepareStatement(sql);

			ps.setInt(1, score);
			ps.setInt(2, teamId);

			int count = ps.executeUpdate();

			ps.close();

			if (count == 0) {
				return false;
			}
			return true;
		} catch (Exception ee) {
			ee.printStackTrace();
			String message = "!!!Score update failure!!! Team with id: " + teamId + " failed to add " + score + " score";
			String query = "UPDATE teams SET score = score + " + score + " WHERE team_number = " + teamId + ";";

			Log.error("Failed to add score to a team. Please check the sql_fix.sql file");
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + message);

			logFailedQuery(query);
		}

		return false;
	}

	private void logFailedQuery(String query) {
		try (BufferedWriter writer = Files.newBufferedWriter(MCF.getInstance().getSqlFixFile().toPath(), StandardOpenOption.APPEND)) {
			writer.write(query + System.lineSeparator());
		} catch (IOException ioe) {
			System.err.format("IOException: %s%n", ioe);
			Log.error("Emergency score error", "Failled to write score log to sql_fix.sql! Please run this query to fix the score: " + query);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerEliminated(PlayerEliminatedEvent e) {

	}
}